package io.jiache.common;

public interface Storage {
    void put(byte[] key, byte[] value);
    void get(byte[] key);
}
