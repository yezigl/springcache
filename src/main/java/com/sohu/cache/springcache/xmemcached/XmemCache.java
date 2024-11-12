package com.sohu.cache.springcache.xmemcached;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import com.sohu.cache.common.Serializer;
import com.sohu.cache.springcache.jedis.JedisCache;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

/**
 * 基于xmemcached的spring cache的实现
 * 
 * @author lukeli
 * @version 1.1.0 2013-2-5
 */
public class XmemCache extends AbstractValueAdaptingCache {

    private static final Logger logger = LoggerFactory.getLogger(JedisCache.class);
    private String name;
    private MemcachedClient memcachedClient;
    private Serializer serializer;
    private int expires;

    public XmemCache(String name, MemcachedClient memcachedClient, Serializer serializer, int expires) {
        super(false);
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
    public <T> T get(Object key, Callable<T> callable) {
        Object lookup = lookup(key);
        if (lookup != null) {
            return (T) lookup;
        }
        if (callable == null) {
            return null;
        }

        try {
            return callable.call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object lookup(Object key) {
        if (key != null) {
            String uniqueKey = uniqueKey(key);
            try {
                String valueSerial = memcachedClient.get(uniqueKey);
                if (valueSerial != null) {
                    Object value = serializer.toObject(valueSerial);
                    logger.debug("uniqueKey={}, valueSerial={}", uniqueKey, valueSerial);
                    if (value != null) {
                        logger.debug("Cache {} key {} hit.", name, key);
                        return value;
                    } else {
                        logger.debug("Cache {} key {} miss.", name, key);
                    }
                }
            } catch (TimeoutException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (MemcachedException e) {
                logger.error("{}", e.getMessage(), e);
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
                logger.debug("uniqueKey={}, expires={}, valueSerial={}, result={}", uniqueKey, expires, valueSerial, result);
            } catch (TimeoutException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (MemcachedException e) {
                logger.error("{}", e.getMessage(), e);
            }
        }
    }
    
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        if (key != null) {
            String uniqueKey = uniqueKey(key);
            try {
                boolean result = memcachedClient.delete(uniqueKey);
                logger.debug("uniqueKey={}, result={}", uniqueKey, result);
            } catch (TimeoutException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error("{}", e.getMessage(), e);
            } catch (MemcachedException e) {
                logger.error("{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void clear() {
        try {
            memcachedClient.flushAll();
            logger.debug("flushall");
        } catch (TimeoutException e) {
            logger.error("{}", e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("{}", e.getMessage(), e);
        } catch (MemcachedException e) {
            logger.error("{}", e.getMessage(), e);
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
        return this.name + "#" + key;
    }
}
