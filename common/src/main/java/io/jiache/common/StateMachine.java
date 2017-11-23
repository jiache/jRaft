package io.jiache.common;

/**
 * Created by jiacheng on 17-9-24.
 */
public interface StateMachine {
    void commit(Entry entry);

    void commit(Entry... entries);

    String get(String key);
}
