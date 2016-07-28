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
        return JSON.toJSONString(object, SerializerFeature.WriteClassName);
    }

    @Override
    public Object toObject(String string) {
        if (string == null || string.trim().length() == 0) {
            return null;
        }
        // 兼容1.1.0
        String[] classObjectPairs = string.split(CLASS_OBJECT_BREAKER, 2);
        if (classObjectPairs.length == 2) {
            string = classObjectPairs[1];
        }
        
        return JSON.parseObject(string);
    }
}
