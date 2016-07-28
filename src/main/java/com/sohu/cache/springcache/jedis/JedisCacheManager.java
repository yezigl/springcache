package com.sohu.cache.springcache.jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import com.sohu.cache.common.JsonSerializer;
import com.sohu.cache.common.Serializer;
import com.sohu.cache.springcache.CacheStoreJedisHashRouter;

import redis.clients.jedis.JedisPool;

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
    private GenericObjectPoolConfig config = new GenericObjectPoolConfig();

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
        return this.config.getMaxTotal();
    }

    public void setMaxActive(int maxActive) {
        this.config.setMaxTotal(maxActive);
    }

    public int getMaxIdle() {
        return this.config.getMaxIdle();
    }

    public void setMaxIdle(int maxIdle) {
        this.config.setMaxIdle(maxIdle);
    }

    public int getMinIdle() {
        return this.config.getMinIdle();
    }

    public void setMinIdle(int minIdle) {
        this.config.setMinIdle(minIdle);
    }

    public long getMaxWait() {
        return this.config.getMaxWaitMillis();
    }

    public void setMaxWait(long maxWait) {
        this.config.setMaxWaitMillis(maxWait);
    }
    
    public boolean getBlockWhenExhausted() {
        return this.config.getBlockWhenExhausted();
    }

    public void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.config.setBlockWhenExhausted(blockWhenExhausted);
    }

    @Deprecated
    public boolean getWhenExhaustedAction() {
        return this.config.getBlockWhenExhausted();
    }

    @Deprecated
    public void setWhenExhaustedAction(boolean blockWhenExhausted) {
        this.config.setBlockWhenExhausted(blockWhenExhausted);
    }

    public boolean isTestOnBorrow() {
        return this.config.getTestOnBorrow();
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.config.setTestOnBorrow(testOnBorrow);
    }

    public boolean isTestOnReturn() {
        return this.config.getTestOnReturn();
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.config.setTestOnReturn(testOnReturn);
    }

    public boolean isTestWhileIdle() {
        return this.config.getTestWhileIdle();
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.config.setTestWhileIdle(testWhileIdle);
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return this.config.getTimeBetweenEvictionRunsMillis();
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    }

    public int getNumTestsPerEvictionRun() {
        return this.config.getNumTestsPerEvictionRun();
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    }

    public long getMinEvictableIdleTimeMillis() {
        return this.config.getMinEvictableIdleTimeMillis();
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    }

    public long getSoftMinEvictableIdleTimeMillis() {
        return this.config.getSoftMinEvictableIdleTimeMillis();
    }

    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
    }

    public boolean isLifo() {
        return this.config.getLifo();
    }

    public void setLifo(boolean lifo) {
        this.config.setLifo(lifo);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
