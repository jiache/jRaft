package io.jiache.util;

public class Random {
    private Random() {
    }

    public static int randomInt(int upper) {
        return (int) (System.currentTimeMillis()%upper);
    }
}
