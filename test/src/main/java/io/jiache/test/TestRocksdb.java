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
        try(
                Options options = new Options().setCreateIfMissing(true);
                RocksDB db = RocksDB.open(options, "testDb")
        ){
            for(int i=1; i<100; ++i) {
                db.put(("key"+i).getBytes(),("value"+i).getBytes());
            }
            for(int i=1; i<100; ++i) {
                System.out.println(new String(db.get(("key"+i).getBytes())));
            }
        }
    }
}
