package io.jiache.util;

public class PortAllocator {
    int begin = 9700;
    public synchronized int allocatePort() {
        return ++begin;
    }
}
