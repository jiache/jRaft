package io.jiache.common;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryLog implements Log{
    private List<Entry> wal;
    private AtomicLong lastIndex;

    public MemoryLog() {
        this(-1);
    }

    public MemoryLog(long baseLastIndex) {
        wal = new CopyOnWriteArrayList<>();
        lastIndex = new AtomicLong(baseLastIndex);
    }

    @Override
    public long getLastTerm() {
        if(wal.size()>0) {
            return wal.get(wal.size() - 1).getTerm();
        }
        return 0;
    }

    @Override
    public long getLastIndex() {
        return lastIndex.get();
    }

    @Override
    public Entry get(long index) {
        return wal.get((int) index);
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
        wal.add(entry);
        return lastIndex.incrementAndGet();
    }

    @Override
    public void append(Entry... entries) {
        Arrays.stream(entries).forEach(this::append);
    }

    @Override
    public boolean match(long lastTerm, long lastIndex) {
        return this.getLastIndex()==lastIndex && this.getLastTerm()==lastTerm;
    }
}
