package io.jiache.common;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryLog implements Log{
    private Map<Long, Entry> wal;
    private AtomicLong lastIndex;
    private AtomicLong lastTerm;

    public MemoryLog() {
        this(-1, 0);
    }

    public MemoryLog(long baseLastIndex, long baseTerm) {
        wal = new ConcurrentHashMap<>();
        lastIndex = new AtomicLong(baseLastIndex);
        lastTerm = new AtomicLong(baseTerm);
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
        Entry[] entries = new Entry[index.length];
        for(int i=0; i<index.length; ++i) {
            entries[i] = wal.get(i);
        }
        return entries;
    }

    @Override
    public long append(Entry entry) {
        long index = lastIndex.incrementAndGet();
        wal.put(index, entry);
        return index;
    }

    @Override
    public void append(Entry... entries) {
        Arrays.stream(entries).forEach(this::append);
    }

    @Override
    public boolean match(long lastTerm, long lastIndex) {
        return this.getLastIndex()==lastIndex && this.getLastTerm()==lastTerm;
    }

    public void put(Long index, Entry entry) {
        wal.put(index, entry);
        if(index == lastIndex.get()+1) {
            long i = index;
            for(; wal.get(index)==null; ++i) {}
            lastIndex.set(i);
        }
    }
}
