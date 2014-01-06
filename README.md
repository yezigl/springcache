springcache
===========

A simple implement of spring cache by redis or memcached.

Usage:

    <cache:annotation-driven cache-manager="springcacheManager" />

    <!-- for redis -->
    <bean id="springcacheManager" class="com.sohu.cache.springcache.jedis.JedisCacheManager">
        <property name="namedClients">
            <map>
                <entry key="default" value="127.0.0.1:6379 127.0.0.1:6380" /><!-- separate by space -->
            </map>
        </property>
        <property name="cacheStoreJedisHashRouter">
            <bean class="com.sohu.cache.springcache.CacheStoreJedisHashRouter" />
        </property>
        <property name="serializer">
            <bean class="com.sohu.cache.common.JsonSerializer" />
        </property>
        <property name="expires" value="604800" /><!--7 days, second -->
        <property name="maxActive" value="100" />
        <property name="testOnBorrow" value="true" />
    </bean>

    <!-- for memcached -->
    <bean id="springcacheManager" class="com.sohu.cache.springcache.xmemcached.XmemCacheManager">
        <property name="namedClients">
            <map>
                <entry key="default" value="127.0.0.1:11211 127.0.0.1:11212" /><!-- separate by space -->
            </map>
        </property>
        <property name="serializer">
            <bean class="com.sohu.cache.common.JsonSerializer" />
        </property>
        <property name="expires" value="604800" /><!--7 days, second -->
        <property name="maxConn" value="8" />
        <property name="consistent" value="true" /><!-- nginx consitent hash -->
    </bean>
    
Dependency:

    <!-- redis -->
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <!-- memcached -->
    <dependency>
        <groupId>com.googlecode.xmemcached</groupId>
        <artifactId>xmemcached</artifactId>
        <version>1.3.9</version>
    </dependency>
