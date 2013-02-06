package com.sohu.cache.springcache.jedis;

import com.sohu.cache.common.User;
import com.sohu.cache.springcache.UserDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class JedisCacheManagerTest {
    @Resource
    CacheManager cacheManager;

    @Resource
    UserDao userDao;
    String name = "test";
    User user = new User(name, "f", 14, "aaaaaa");

    @Test
    public void testGet() {
        userDao.saveUser(user);
        userDao.getUser(name);
    }
    
    @Test
    public void testDeleteGet() throws Exception {
        userDao.deleteUser(name);
        userDao.saveUser(user);
        User retrievedUser = userDao.getUser(name);
        Assert.assertTrue(user == retrievedUser);
        User cachedRetrievedUser = userDao.getUser(name);
        Assert.assertTrue(user != cachedRetrievedUser);
        assertUser(user, cachedRetrievedUser);
    }

    @Test
    public void testUpdate() throws Exception {
        userDao.deleteUser(name);
        userDao.saveUser(user);
        userDao.getUser(name);
        user.setAge(81);
        userDao.updateUser(user);
        User retrievedUser = userDao.getUser(name);
        Assert.assertTrue(user == retrievedUser);
        User cachedRetrievedUser = userDao.getUser(name);
        Assert.assertTrue(user != cachedRetrievedUser);
        assertUser(user, cachedRetrievedUser);
    }

    @Test
    public void testClear() throws Exception {
        Cache cache = cacheManager.getCache("user");
        userDao.saveUser(user);
        userDao.getUser(name);
        Cache.ValueWrapper valueWrapper = cache.get(name);
        Assert.assertNotNull(valueWrapper.get());
        cache.clear();
        valueWrapper = cache.get(name);
        Assert.assertNull(valueWrapper);
    }

    void assertUser(User accept, User actual) {
        Assert.assertEquals(accept.getName(), actual.getName());
        Assert.assertEquals(accept.getSex(), actual.getSex());
        Assert.assertEquals(accept.getAge(), actual.getAge());
        Assert.assertEquals(accept.getAddress(), actual.getAddress());
    }

    @Test
    public void testNull() throws Exception {
        Assert.assertNull(userDao.getUser("null"));
    }
}
