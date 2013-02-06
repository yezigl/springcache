package com.sohu.cache.springcache.jedis;

import com.sohu.cache.common.JsonSerializer;
import com.sohu.cache.common.Serializer;
import com.sohu.cache.springcache.CacheStoreJedisHashRouter;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于jedis的spring cacheManager的实现
 * 
 * @author xiaobinghan
 * @version 1.0.0 2012-5-4
 */
public class JedisCacheManager extends AbstractCacheManager implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(JedisCacheManager.class);
    
    private static final int DEFAULT_TIME_OUT = 2000;
    private static final int DEFAULT_EXPIRES = 604800; // 7 days, second
    
    private Map<String, String> namedClients;
    private CacheStoreJedisHashRouter cacheStoreJedisHashRouter;
    private Serializer serializer;
    private int expires = DEFAULT_EXPIRES;
    private int timeout = DEFAULT_TIME_OUT;
    
    private List<Cache> cacheList;
    private List<JedisPool> jedisPoolList;
    private GenericObjectPool.Config config = new GenericObjectPool.Config();

    @Override
    public void afterPropertiesSet() {
        if (this.namedClients == null || this.namedClients.size() == 0) {
            throw new NullPointerException("Property namedClients must be set! Like \"default->127.0.0.1:6379\"");
        }
        this.cacheList = new ArrayList<Cache>();
        this.jedisPoolList = new ArrayList<JedisPool>();
        if (this.cacheStoreJedisHashRouter == null) {
            this.cacheStoreJedisHashRouter = new CacheStoreJedisHashRouter();
        }
        if (this.serializer == null) {
            this.serializer = new JsonSerializer();
        }
        for (Map.Entry<String, String> namedClient : namedClients.entrySet()) {
            List<JedisPool> jedisPoolList = createJedisList(namedClient.getValue());
            this.jedisPoolList.addAll(jedisPoolList);
            this.cacheList.add(new JedisCache(namedClient.getKey(), jedisPoolList, cacheStoreJedisHashRouter, serializer, this.expires));
        }
        if (!namedClients.keySet().contains("default")) {
            this.cacheList.add(new JedisCache("default", this.jedisPoolList, cacheStoreJedisHashRouter, serializer, this.expires));
        }
        logger.debug("cacheList:{}", this.cacheList);
        super.afterPropertiesSet();
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = super.getCache(name);
        if (cache == null) {
            cache = new JedisCache(name, this.jedisPoolList, this.cacheStoreJedisHashRouter, this.serializer, this.expires);
            addCache(cache);
            this.cacheList.add(cache);
        }
        return cache;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.cacheList;
    }

    private List<JedisPool> createJedisList(String servers) {
        List<JedisPool> jedisList = new ArrayList<JedisPool>();
        String[] hostPortPairArray = servers.split(" ");
        for (String hostPortPair : hostPortPairArray) {
            try {
                String[] hostAndPort = hostPortPair.split(":");
                if (hostAndPort.length == 2) {
                    jedisList.add(new JedisPool(config, hostAndPort[0], Integer.parseInt(hostAndPort[1]), timeout));
                } else if (hostAndPort.length == 1) {
                    jedisList.add(new JedisPool(config, hostAndPort[0], 6379, timeout));
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return jedisList;
    }
    
    @Override
    public void destroy() throws Exception {
        
    }

    public void setNamedClients(Map<String, String> namedClients) {
        this.namedClients = namedClients;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public void setCacheStoreJedisHashRouter(CacheStoreJedisHashRouter cacheStoreJedisHashRouter) {
        this.cacheStoreJedisHashRouter = cacheStoreJedisHashRouter;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public int getMaxActive() {
        return this.config.maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.config.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return this.config.maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.config.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return this.config.minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.config.minIdle = minIdle;
    }

    public long getMaxWait() {
        return this.config.maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.config.maxWait = maxWait;
    }

    public byte getWhenExhaustedAction() {
        return this.config.whenExhaustedAction;
    }

    public void setWhenExhaustedAction(byte whenExhaustedAction) {
        this.config.whenExhaustedAction = whenExhaustedAction;
    }

    public boolean isTestOnBorrow() {
        return this.config.testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.config.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return this.config.testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.config.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return this.config.testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.config.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return this.config.timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.config.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getNumTestsPerEvictionRun() {
        return this.config.numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.config.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public long getMinEvictableIdleTimeMillis() {
        return this.config.minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.config.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getSoftMinEvictableIdleTimeMillis() {
        return this.config.softMinEvictableIdleTimeMillis;
    }

    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.config.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    public boolean isLifo() {
        return this.config.lifo;
    }

    public void setLifo(boolean lifo) {
        this.config.lifo = lifo;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
