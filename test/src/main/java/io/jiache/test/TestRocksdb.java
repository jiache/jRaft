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
        String dbPath = "rocksdb.db";
        Options options = new Options().setCreateIfMissing(true);
        RocksDB db = RocksDB.open(options, dbPath);
        ColumnFamilyHandle  columnFamilyHandle = db.createColumnFamily(
                new ColumnFamilyDescriptor("new_cf".getBytes(), new ColumnFamilyOptions())
        );
        
    }
}
