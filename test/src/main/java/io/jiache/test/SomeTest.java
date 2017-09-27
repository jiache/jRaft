package io.jiache.test;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SomeTest {

    private static <T> T get(T a) {
        return a;
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException, UnknownHostException {
        SomeTest.<Integer>get(1);
    }
}
