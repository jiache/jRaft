package io.jiache.util;

import com.alibaba.fastjson.JSON;

public class Serializer {
    private Serializer(){}

    public static <T> byte[] serialize(T object) {
        return JSON.toJSONBytes(object);
    }

    public static <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }
}
