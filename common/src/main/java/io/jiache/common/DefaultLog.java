package io.jiache.common;

import io.jiache.util.Assert;
import org.rocksdb.RocksDBException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * Created by jiacheng on 17-9-24.
 */
public class DefaultLog implements Log{
    private Wal wal;
    private AtomicLong lastIndex;
    private AtomicLong lastTerm;

    private void updateFromWal() {
        this.lastIndex = new AtomicLong(wal.getLastIndex());
        this.lastTerm = new AtomicLong(wal.getLastTerm());
    }

    private DefaultLog() {
    }

    public DefaultLog(String walPath) throws RocksDBException {
        wal = new DefaultWal(walPath);
        updateFromWal();
    }

    private void setWal(Wal wal) {
        this.wal = wal;
    }

    private void setLastTerm(long lastTerm) {
        this.lastTerm.set(lastTerm);
    }

    private void setLastIndex(long lastIndex) {
        this.lastIndex.set(lastIndex);
    }

    public static Log newInstance(String walPath) throws RocksDBException {
        DefaultLog log = new DefaultLog();
        Wal wal = DefaultWal.newInstance(walPath);
        log.setLastIndex(wal.getLastIndex());
        log.setLastTerm(wal.getLastTerm());
        log.setWal(wal);
        return log;
    }

    @Override
    public long getLastTerm() {
        return lastTerm.get();
    }

    @Override
    public long getLastIndex() {
        return lastIndex.get();
    }

    @Override
    public Entry get(long index) {
        return wal.get(index);
    }

    @Override
    public Entry[] get(long[] index) {
        return wal.get(index);
    }

    @Override
    public synchronized long append(Entry entry) {
        long index = lastIndex.incrementAndGet();
        wal.put(index, entry);
        wal.setLastIndex(index);
        wal.setLastTerm(lastTerm.get());
        return index;
    }

    @Override
    public synchronized void append(Entry[] entries) {
        Arrays.stream(entries).forEach(this::append);
    }

    @Override
    public boolean match(long lastTerm, long lastIndex) {
        return this.getLastIndex()==lastIndex && this.getLastTerm()==lastTerm;
    }
}
