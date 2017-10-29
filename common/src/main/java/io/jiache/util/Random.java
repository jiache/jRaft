package io.jiache.util;

public class Random {
    private static java.util.Random random = new java.util.Random();

    private Random() {
    }

    public static int randomInt(int upper) {
        return random.nextInt(upper);
    }
}
