package io.jiache.common;

import io.jiache.util.Assert;
import io.jiache.util.ByteUtils;
import io.jiache.util.Serializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class DefaultWal implements Wal {
    private static final byte[] LAST_INDEX_KEY = "LAST_INDEX".getBytes();
    private static final byte[] LAST_TERM_KEY = "LAST_TERM".getBytes();

    static {
        RocksDB.loadLibrary();
    }

    private RocksDB rocksDB;

    private DefaultWal() {
    }

    public DefaultWal(String walPath) throws RocksDBException {
        Assert.checkNull(walPath, "wal path");
        Options options = new Options().setCreateIfMissing(true);
        rocksDB = RocksDB.open(options,walPath);
        this.rocksDB = rocksDB;
    }

    public static DefaultWal newInstance(String walPath) throws RocksDBException {
        Assert.checkNull(walPath, "wal path");
        Options options = new Options().setCreateIfMissing(true);
        RocksDB rocksDB = RocksDB.open(options,walPath);
        DefaultWal wal = new DefaultWal();
        wal.rocksDB = rocksDB;
        return wal;
    }

    @Override
    public void put(long index, Entry entry) {
        try {
            rocksDB.put(ByteUtils.longToBytes(index), Serializer.serialize(entry));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(long[] index, Entry[] entries) {
        try {
            for(int i=0; i<index.length; ++i) {
                rocksDB.put(ByteUtils.longToBytes(index[i]), Serializer.serialize(entries[i]));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Entry get(long index) {
        byte[] byteEntry = new byte[0];
        try {
            byteEntry = rocksDB.get(ByteUtils.longToBytes(index));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        Assert.checkNull(byteEntry, "bytes entry");
        return Serializer.deSerialize(byteEntry, Entry.class);
    }

    @Override
    public Entry[] get(long[] index) {
        Entry[] entries = new Entry[index.length];
        try {
            for(int i=0; i<index.length; ++i) {
                entries[i] = Serializer.deSerialize(rocksDB.get(ByteUtils.longToBytes(i)), Entry.class);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return entries;
    }

    @Override
    public void delete(long index) {
        try {
            rocksDB.delete(ByteUtils.longToBytes(index));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(long[] index) {
        try {
            for(int i=0; i<index.length; ++i) {
                rocksDB.delete(ByteUtils.longToBytes(index[i]));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getLastIndex() {
        long lastIndex = -1;
        byte[] bytes = null;
        try {
            bytes = rocksDB.get(LAST_INDEX_KEY);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        if(bytes != null) {
            lastIndex = ByteUtils.bytesToLong(bytes);
        }
        return lastIndex;
    }

    @Override
    public void setLastIndex(long lastIndex) {
        try {
            rocksDB.put(LAST_INDEX_KEY, ByteUtils.longToBytes(lastIndex));
        } catch (RocksDBException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public long getLastTerm() {
        long lastTerm = 0;
        byte[] bytes = null;
        try {
            bytes = rocksDB.get(LAST_TERM_KEY);
        } catch (RocksDBException e1) {
            e1.printStackTrace();
        }
        if(bytes != null) {
            lastTerm = ByteUtils.bytesToLong(bytes);
        }
        return lastTerm;
    }

    @Override
    public void setLastTerm(long lastTerm) {
        try {
            rocksDB.put(LAST_TERM_KEY, ByteUtils.longToBytes(lastTerm));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
