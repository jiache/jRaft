package io.jiache.common;

import io.jiache.util.Assert;

import java.util.stream.LongStream;

/**
 * Created by jiacheng on 17-9-24.
 */
public class DefaultLog implements Log{
    private volatile Wal wal;
    private volatile long lastIndex;
    private volatile long lastTerm;

    private DefaultLog() {
    }

    private void setWal(Wal wal) {
        this.wal = wal;
    }

    private void setLastTerm(long lastTerm) {
        this.lastTerm = lastTerm;
    }

    private void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public static Log newInstance(String walPath) {
        DefaultLog log = new DefaultLog();
        log.setLastIndex(-1);
        log.setLastTerm(-1);
        log.setWal(DefaultWal.newInstance(walPath));
        return log;
    }

    @Override
    public long getLastTerm() {
        return lastTerm;
    }

    @Override
    public long getLastIndex() {
        return lastIndex;
    }

    @Override
    public Entry get(long index) {
        return wal.get(index);
    }

    @Override
    public synchronized void append(Entry entry) {
        Assert.checkNull(entry, "entry");
        Assert.check(!(entry.getTerm()<getLastTerm()), "entry term error");
        setLastTerm(entry.getTerm());
        ++lastIndex;
        wal.put(lastIndex, entry);
    }

    @Override
    public synchronized void append(Entry... entries) {
        Assert.checkNull(entries, "entries");
        Assert.check(!(entries[0].getTerm()<getLastTerm()), "entries term error");
        setLastTerm(entries[entries.length-1].getTerm());
        setLastIndex(lastIndex+entries.length);
        long[] indexArray = LongStream.range(lastIndex+1, lastIndex+1+entries.length).toArray();
        wal.put(indexArray, entries);
    }

    @Override
    public boolean match(long lastTerm, long lastIndex) {
        return this.lastIndex==lastIndex && this.lastTerm==lastTerm;
    }
}
