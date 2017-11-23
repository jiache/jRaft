package io.jiache.client;

public interface TestStrategy {
    void runBenchmark(Client client, String token, Integer benchmarkSize);
}
