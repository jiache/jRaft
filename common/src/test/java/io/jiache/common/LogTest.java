package io.jiache.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class LogTest {
    @Test
    public void mainTest() {
        Log log = DefaultLog.newInstance("testLog");
        log.append(new Entry("key1", "value1", 20));
        List<Entry> entries = new ArrayList<>();
        for(int i=0; i<100; ++i) {
            entries.add(new Entry("key"+i, "value"+i,20));
        }
        Entry[] entryArray = entries.toArray(new Entry[entries.size()]);
        log.append(entryArray);
        System.out.printf("lastIndex: %d, lastTerm: %d\n",log.getLastIndex(), log.getLastTerm());
//        for(int i=0; i<=log.getLastIndex(); ++i) {
//            System.out.println(log.get(i));
//        }
        long[] indexArray = LongStream.range(0, log.getLastIndex()+1).toArray();
        Entry[] resultEntries = log.get(indexArray);
        Arrays.stream(resultEntries).forEach(System.out::println);
    }
}
