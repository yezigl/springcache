package com.sohu.cache.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 使用fastjson实现
 * 
 * @author xiaobinghan 
 * @version 1.0.0 12-5-4
 */
public class JsonSerializer implements Serializer {

    private static final String CLASS_OBJECT_BREAKER = "@@";

    @Override
    public String toString(Object object) {
        return object == null ? null : new StringBuilder()
                .append(object.getClass().getName())
                .append(CLASS_OBJECT_BREAKER)
                .append(JSON.toJSONString(object, SerializerFeature.WriteClassName))
                .toString();
    }

    @Override
    public Object toObject(String string) throws ClassNotFoundException {
        if (string == null || string.trim().length() == 0) {
            return null;
        }
        String[] classObjectPairs = string.split(CLASS_OBJECT_BREAKER, 2);
        if (classObjectPairs.length != 2) {
            throw new ClassNotFoundException("classObjectPairs length is not 2\n" + string);
        }
        Class<?> clazz = Class.forName(classObjectPairs[0]);
        //noinspection unchecked
        return JSON.parseObject(classObjectPairs[1], clazz);
    }
}
