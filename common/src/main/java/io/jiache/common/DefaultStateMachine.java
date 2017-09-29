package io.jiache.common;

import io.jiache.util.Assert;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiacheng on 17-9-24.
 */
public class DefaultStateMachine implements StateMachine {
    private volatile Map<String, String> storage;

    private DefaultStateMachine() {
    }

    private void setStorage(Map<String, String> storage) {
        this.storage = storage;
    }

    public static StateMachine newInstance() {
        DefaultStateMachine stateMachine = new DefaultStateMachine();
        stateMachine.setStorage(new ConcurrentHashMap<>());
        return stateMachine;
    }

    public void commit(Entry entry) {
        Assert.checkNull(entry, "entry");
        storage.put(entry.getKey(), entry.getValue());
    }

    public void commit(Entry... entries) {
        Arrays.stream(entries).forEach(this::commit);
    }

    public String get(String key) {
        return storage.get(key);
    }

    public Map<String, String> getStorage() {
        return storage;
    }

}
