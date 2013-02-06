package com.sohu.cache.springcache.xmemcached;

import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.sohu.cache.common.Serializer;
import com.sohu.cache.springcache.jedis.JedisCache;

/**
 * 基于xmemcached的spring cache的实现
 * 
 * @author lukeli
 * @version 1.0.1 2013-2-5
 */
public class XmemCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(JedisCache.class);
    private String name;
    private MemcachedClient memcachedClient;
    private Serializer serializer;
    private int expires;

    public XmemCache(String name, MemcachedClient memcachedClient, Serializer serializer, int expires) {
        this.name = name;
        this.memcachedClient = memcachedClient;
        this.serializer = serializer;
        this.expires = expires;
        if (this.memcachedClient == null) {
            logger.error("memcachedClient can not be null.");
            throw new NullPointerException("memcachedClient is null.");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return memcachedClient;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (key != null) {
            String uniqueKey = uniqueKey(key);
            try {
                String valueSerial = memcachedClient.get(uniqueKey);
                if (valueSerial != null) {
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
                }
            } catch (TimeoutException e) {
                logger.error("", e);
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (MemcachedException e) {
                logger.error("", e);
            }
        }
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        if (key != null) {
            String uniqueKey = uniqueKey(key);
            String valueSerial = serializer.toString(value);
            try {
                boolean result = memcachedClient.set(uniqueKey, expires, valueSerial);
                logger.debug("uniqueKey={}, expires={}, valueSerial={}, result={}", new Object[] { uniqueKey, expires,
                        valueSerial, result });
            } catch (TimeoutException e) {
                logger.error("", e);
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (MemcachedException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void evict(Object key) {
        if (key != null) {
            String uniqueKey = uniqueKey(key);
            try {
                boolean result = memcachedClient.delete(uniqueKey);
                logger.debug("uniqueKey={}, result={}", new Object[] { uniqueKey, result });
            } catch (TimeoutException e) {
                logger.error("", e);
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (MemcachedException e) {
                logger.error("", e);
            }
        }
    }

    @Override
    public void clear() {
        try {
            memcachedClient.flushAll();
            logger.debug("flushall");
        } catch (TimeoutException e) {
            logger.error("", e);
        } catch (InterruptedException e) {
            logger.error("", e);
        } catch (MemcachedException e) {
            logger.error("", e);
        }
    }

    public MemcachedClient getMemcachedClient() {
        return this.memcachedClient;
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
}
