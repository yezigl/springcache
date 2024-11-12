package com.sohu.cache.springcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * @author xiaobinghan 
 * @version 1.0.0 12-5-4
 */
public class CacheStoreJedisHashRouter implements CacheStoreRouter<JedisPool> {
    private static final Logger logger = LoggerFactory.getLogger(CacheStoreJedisHashRouter.class);

    @Override
    public JedisPool pickUp(List<JedisPool> cacheStores, String cacheName, Object key) {
        int hashCode = (cacheName + key).hashCode();
        logger.debug("cacheName={}, key={}, hashCode={}", cacheName, key, hashCode);
        return cacheStores.get(Math.abs(hashCode) % cacheStores.size());
    }
}
