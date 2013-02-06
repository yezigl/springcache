package com.sohu.cache.springcache;

import com.sohu.cache.common.User;

public interface UserDao {
    void saveUser(User user);

    User getUser(String name);

    void updateUser(User user);

    void deleteUser(String name);
}
