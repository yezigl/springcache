package com.sohu.cache.springcache.jedis;

import com.sohu.cache.common.Serializer;
import com.sohu.cache.springcache.CacheStoreRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Set;

/**
 * 基于jedis的spring cache的实现
 * 
 * @author xiaobinghan
 * @version 1.0.0 2012-5-4
 */
public class JedisCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(JedisCache.class);
    private String name;
    private List<JedisPool> jedisPoolList;
    private CacheStoreRouter<JedisPool> cacheStoreRouter;
    private Serializer serializer;
    private int expires;

    public JedisCache(String name, List<JedisPool> jedisList, CacheStoreRouter<JedisPool> cacheStoreRouter,
            Serializer serializer, int expires) {
        this.name = name;
        this.jedisPoolList = jedisList;
        this.cacheStoreRouter = cacheStoreRouter;
        this.serializer = serializer;
        this.expires = expires;
    }

    @Override
    public Object getNativeCache() {
        return jedisPoolList;
    }

    /**
     * 实现@Cacheable注解
     */
    @Override
    public ValueWrapper get(Object key) {
        if (key != null) {
            JedisPool jedisPool = cacheStoreRouter.pickUp(jedisPoolList, name, key);
            if (jedisPool != null) {
                Jedis jedis = null;
                boolean broken = false;
                try {
                    jedis = jedisPool.getResource();
                    String uniqueKey = uniqueKey(key);
                    String valueSerial = jedis.get(uniqueKey);
                    Object value = null;
                    try {
                        value = serializer.toObject(valueSerial);
                    } catch (ClassNotFoundException e) {
                        logger.error("", e);
                    }
                    logger.debug("uniqueKey={}, valueSerial={}", new Object[] { uniqueKey, valueSerial });
                    if (value != null) {
                        logger.info("Cache {} key {} hit.", name, key);
                        return new SimpleValueWrapper(value);
                    } else {
                        logger.warn("Cache {} key {} miss.", name, key);
                    }
                } catch (JedisConnectionException e) {
                    logger.error("key={}", key, e);
                    broken = true;
                } finally {
                    if (jedis != null) {
                        if (broken) {
                            jedisPool.returnBrokenResource(jedis);
                        } else {
                            jedisPool.returnResource(jedis);
                        }
                    }
                }
            } else {
                logger.error("Cache store route error.");
            }
        } else {
            logger.warn("Key is null.");
        }
        return null;
    }

    /**
     * 实现@CachePut注解
     */
    @Override
    public void put(Object key, Object value) {
        if (key != null && value != null) {
            JedisPool jedisPool = cacheStoreRouter.pickUp(jedisPoolList, name, key);
            if (jedisPool != null) {
                Jedis jedis = null;
                boolean broken = false;
                try {
                    jedis = jedisPool.getResource();
                    String uniqueKey = uniqueKey(key);
                    String valueSerial = serializer.toString(value);
                    String result = jedis.setex(uniqueKey, expires, valueSerial);
                    logger.debug("uniqueKey={}, expires={}, valueSerial={}, result={}", new Object[] { uniqueKey,
                            String.valueOf(expires), valueSerial, result });
                } catch (JedisConnectionException e) {
                    logger.error("key={}", key, e);
                    broken = true;
                } finally {
                    if (jedis != null) {
                        if (broken) {
                            jedisPool.returnBrokenResource(jedis);
                        } else {
                            jedisPool.returnResource(jedis);
                        }
                    }
                }
            } else {
                logger.error("Cache store route error.");
            }
        } else {
            logger.warn("Key or value is null. Key={}, value={}", key, value);
        }
    }

    /**
     * 实现@CacheEvict注解
     */
    @Override
    public void evict(Object key) {
        if (key != null) {
            JedisPool jedisPool = cacheStoreRouter.pickUp(jedisPoolList, name, key);
            if (jedisPool != null) {
                Jedis jedis = null;
                boolean broken = false;
                try {
                    jedis = jedisPool.getResource();
                    String uniqueKey = uniqueKey(key);
                    long removeCount = jedis.del(uniqueKey);
                    logger.debug("uniqueKey={}, removeCount={}",
                            new Object[] { uniqueKey, String.valueOf(removeCount) });
                } catch (JedisConnectionException e) {
                    logger.error("key={}", key, e);
                    broken = true;
                } finally {
                    if (jedis != null) {
                        if (broken) {
                            jedisPool.returnBrokenResource(jedis);
                        } else {
                            jedisPool.returnResource(jedis);
                        }
                    }
                }
            } else {
                logger.error("Cache store route error.");
            }
        } else {
            logger.warn("Key is null.");
        }
    }

    @Override
    public void clear() {
        long deleteCount = 0;
        for (JedisPool jedisPool : jedisPoolList) {
            if (jedisPool != null) {
                Jedis jedis = null;
                boolean broken = false;
                try {
                    jedis = jedisPool.getResource();
                    Set<String> keySet = jedis.keys(uniqueKey("*"));
                    String[] keyArray = keySet.toArray(new String[keySet.size()]);
                    if (keyArray.length > 0) {
                        deleteCount += jedis.del(keyArray);
                    }
                } catch (JedisConnectionException e) {
                    logger.error("", e);
                    broken = true;
                } finally {
                    if (jedis != null) {
                        if (broken) {
                            jedisPool.returnBrokenResource(jedis);
                        } else {
                            jedisPool.returnResource(jedis);
                        }
                    }
                }
            }
        }
        logger.debug("count={}", deleteCount);
    }

    /**
     * make unique key by prepend cache name
     * 
     * @param key
     *            logic key
     * @return the unique key with cache name
     */
    private String uniqueKey(Object key) {
        return new StringBuilder().append(this.name).append("#").append(String.valueOf(key)).toString();
    }

    @Override
    public String getName() {
        return name;
    }
}
