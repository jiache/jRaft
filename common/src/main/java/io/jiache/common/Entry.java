package io.jiache.common;

/**
 * Created by jiacheng on 17-9-24.
 */
public class Entry {
    private String key;
    private String value;
    private int term;
    private int index;

    public Entry() {
    }

    public Entry(String key, String value, int term, int index) {
        this.key = key;
        this.value = value;
        this.term = term;
        this.index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String
    toString() {
        return "Entry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", term=" + term +
                ", index=" + index +
                '}';
    }
}
