package io.jiache.util;

/**
 * Created by jiacheng on 17-9-24.
 */
public class Assert {
    private Assert(){}

    public static <T> void checkNull(T obj, final String name) {
        if(obj == null) {
            throw new NullPointerException(name + " cannot be null.");
        }
    }

    public static void check(boolean expression, final String errorMessage) {
        if(!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
