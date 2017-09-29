package io.jiache.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.LongStream;

public class SomeTest {

    private static <T> T get(T a) {
        return a;
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException, UnknownHostException {
        long[] array = LongStream.range(10 + 1, 10+11).toArray();
        Arrays.stream(array).forEach(System.out::println);
    }
}
