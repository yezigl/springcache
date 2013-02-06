package com.sohu.cache.springcache;

import com.sohu.cache.common.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository("userDAO")
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
    private final Map<String, User> innerStore = new ConcurrentHashMap<String, User>();

    @Override
    public void saveUser(User user) {
        innerStore.put(user.getName(), user);
        logger.warn("name={}, user={}", user.getName(), user);
    }

    @Override
    @Cacheable(value = "user", key = "#name")
    public User getUser(String name) {
        User user = innerStore.get(name);
        logger.warn("name={}, user={}", name, user);
        return user;
    }

    @Override
    @CacheEvict(value = "user", key = "#user.name")
    public void updateUser(User user) {
        innerStore.put(user.getName(), user);
        logger.warn("name={}, user={}", user.getName(), user);
    }

    @Override
    @CacheEvict(value = "user", key = "#name")
    public void deleteUser(String name) {
        innerStore.remove(name);
        logger.warn("name={}", name);
    }
}
