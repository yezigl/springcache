package com.sohu.cache.springcache;

import java.util.List;

/**
 * 分配到cache的某个节点
 * 
 * @author xiaobinghan 
 * @version 1.0.0 12-5-4
 */
public interface CacheStoreRouter<CacheStore> {
    CacheStore pickUp(List<CacheStore> cacheStores, String cacheName, Object key);
}
