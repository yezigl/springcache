package com.sohu.cache.common;

/**
 * 序列化
 * 
 * @author xiaobinghan 
 * @version 1.0.0 12-5-4
 */
public interface Serializer {
    String toString(Object object);

    Object toObject(String string) throws ClassNotFoundException;
}
