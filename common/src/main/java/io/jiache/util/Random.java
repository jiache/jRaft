package io.jiache.util;

public class Random {
    private static java.util.Random random = new java.util.Random();

    private Random() {
    }

    public static int randomInt(int upper) {
        if(upper<=0) {
            throw new RuntimeException("random upper is "+upper);
        }
        return random.nextInt(upper);
    }
}
