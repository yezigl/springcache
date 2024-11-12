package com.sohu.cache.springcache.xmemcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import com.sohu.cache.common.JsonSerializer;
import com.sohu.cache.common.Serializer;

/**
 * 基于xmemcached的spring cacheManager的实现
 * 
 * @author xiaobinghan
 * @version 1.1.0 2013-2-5
 */
public class XmemCacheManager extends AbstractCacheManager implements DisposableBean {
    
    private static final Logger logger = LoggerFactory.getLogger(XmemCacheManager.class);
    
    private Map<String, String> namedClients;
    private int maxConn = 8;
    private int timeout = 1000;
    private boolean consistent; // use consistent hash or not
    private int expires = 604800; // 7 days
    private Serializer serializer;
    
    private List<Cache> cacheList;
    private List<InetSocketAddress> serverList;

    @Override
    public void afterPropertiesSet() {
        if (this.namedClients == null || this.namedClients.isEmpty()) {
            throw new NullPointerException("Property namedClients must be set! Like \"default->127.0.0.1:11211\"");
        }
        this.cacheList = new ArrayList<>();
        this.serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : namedClients.entrySet()) {
            try {
                List<InetSocketAddress> server = AddrUtil.getAddresses(entry.getValue());
                Cache cache = new XmemCache(entry.getKey(), getMemcachedClient(server), serializer, expires);
                this.cacheList.add(cache);
                this.serverList.addAll(server);
            } catch (IOException e) {
                logger.error("create cache error, name = {}", entry.getKey(), e);
            }
        }
        if (!namedClients.keySet().contains("default")) {
            try {
                Cache cache = new XmemCache("default", getMemcachedClient(serverList), serializer, expires);
                this.cacheList.add(cache);
            } catch (IOException e) {
                logger.error("create cache error, name = {}", "default", e);
            }
        }
        if (this.serializer == null) {
            this.serializer = new JsonSerializer();
        }
        logger.debug("cacheList: {}", cacheList);
        super.afterPropertiesSet();
    }    

    @Override
    public Cache getCache(String name) {
        Cache cache = super.getCache(name);
        if (cache == null) {
            try {
                cache = new XmemCache(name, getMemcachedClient(serverList), serializer, expires);
                addCache(cache);
                this.cacheList.add(cache);
            } catch (IOException e) {
                logger.error("create cache error, name = {}", name, e);
            }
        }
        return cache;
    }
    
    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.cacheList;
    }
    
    @Override
    public void destroy() throws Exception {
        if (this.cacheList != null) {
            for (Cache cache : cacheList) {
                ((XmemCache) cache).getMemcachedClient().shutdown();
            }
        }
    }
    
    private MemcachedClient getMemcachedClient(List<InetSocketAddress> servers) throws IOException {
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(servers);
        builder.setConnectionPoolSize(maxConn);
        builder.setSessionLocator(new KetamaMemcachedSessionLocator(consistent));
        MemcachedClient memcachedClient = builder.build();
        memcachedClient.setOpTimeout(timeout);
        return memcachedClient;
    }

    public Map<String, String> getNamedClients() {
        return namedClients;
    }

    public void setNamedClients(Map<String, String> namedClients) {
        this.namedClients = namedClients;
    }

    public int getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public void setConsistent(boolean consistent) {
        this.consistent = consistent;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

}
