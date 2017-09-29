package io.jiache.common;

/**
 * Created by jiacheng on 17-9-24.
 */
public interface Log {
    long getLastTerm();
    long getLastIndex();
    Entry get(long index);
    void append(Entry entry);
    void append(Entry... entries);
    boolean match(long lastTerm, long lastIndex);
}
