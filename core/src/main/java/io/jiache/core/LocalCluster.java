package io.jiache.core;

public interface LocalCluster {
    void start();
    void shutdown();
    void put(String key, String value);
    String get(String key);
}
