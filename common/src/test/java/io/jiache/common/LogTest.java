package io.jiache.common;

import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class LogTest {
    @Test
    public void mulThreadRocksdbTest() throws RocksDBException {
        RocksDB.loadLibrary();
        RocksDB db = RocksDB.open(new Options().setCreateIfMissing(true), "testDB");
        ExecutorService executor = Executors.newCachedThreadPool();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()->{
            try {
                for(int i=0; i<10000; ++i) {
                    db.put((""+i).getBytes(), (i+" thread1").getBytes());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(()->{
            try {
                for(int i=0; i<10000; ++i) {
                    db.put((""+i).getBytes(), (i+"thread2").getBytes());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
        future1.join();
        future2.join();
        for(int i=0; i<10000; ++i) {
            String s = new String(db.get((""+i).getBytes()));
            System.out.println(s);
        }
    }

    @Test
    public void memoryLogTest() {
        Log log = new MemoryLog();
        List<Entry> entries = new ArrayList<>();
        int n = 10;
        for(int i=0; i<n; ++i) {
            entries.add(new Entry("Key"+i, "Value"+i, 0));
        }
        Entry[] entryArr = new Entry[entries.size()];
        log.append(entries.toArray(entryArr));
        Entry[] res = log.get(LongStream.range(0, n).toArray());
        Arrays.stream(res).forEach(System.out::println);
    }

}
