package com.sohu.cache.springcache.xmemcached;

import static org.junit.Assert.fail;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sohu.cache.common.User;
import com.sohu.cache.springcache.UserDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class XmemCacheManagerTest {
    
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
        userDao.saveUser(user);
        userDao.getUser(name);
    }
    
    void assertUser(User accept, User actual) {
        Assert.assertEquals(accept.getName(), actual.getName());
        Assert.assertEquals(accept.getSex(), actual.getSex());
        Assert.assertEquals(accept.getAge(), actual.getAge());
        Assert.assertEquals(accept.getAddress(), actual.getAddress());
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
