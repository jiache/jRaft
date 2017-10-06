package io.jiache.common;

public interface Wal {
    void put(long index, Entry entry);
    void put(long[] index, Entry[] entries);
    Entry get(long index);
    Entry[] get(long[] index);
    void delete(long index);
    void delete(long[] index);
    long getLastIndex();
    void setLastIndex(long lastIndex);
    long getLastTerm();
    void setLastTerm(long lastTerm);
}
