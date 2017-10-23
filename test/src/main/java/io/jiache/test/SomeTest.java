package io.jiache.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public class SomeTest {

    private static <T> T get(T a) {
        return a;
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException, UnknownHostException {
        AtomicInteger a = new AtomicInteger(0);
        System.out.println(a.incrementAndGet());
        System.out.println(a.incrementAndGet());
    }
}
