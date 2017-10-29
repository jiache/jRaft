package io.jiache.util;

public class StringGenerator {
    private static final String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int length = s.length();
    private StringGenerator() {}

    public static String randomGenerate(int len) {
        StringBuilder builder = new StringBuilder();
        for(; len>0; --len) {
            builder.append(s.charAt(Random.randomInt(length)));
        }
        return builder.toString();
    }
}
