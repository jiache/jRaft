package io.jiache.test;

import org.rocksdb.*;


/**
 * Created by jiacheng on 17-9-24.
 */
public class TestRocksdb {
    static {
        RocksDB.loadLibrary();
    }

    public static void main(String[] args) throws RocksDBException {

            Options options = new Options().setCreateIfMissing(true);
            RocksDB db = RocksDB.open(options, "/home/jiacheng/Desktop/jRaft/common/testLog");
//            for(int i=0; i<1000; ++i) {
//                db.put(("hahaha"+(i%105)).getBytes(), ("value"+i).getBytes());
//            }
//            for(int i=0; i<100; ++i) {
//                String value = new String(db.get(("key"+i).getBytes()));
//                System.out.println(value);
//            }
        db.compactRange();
        System.out.println(db.getLatestSequenceNumber());


    }
}
