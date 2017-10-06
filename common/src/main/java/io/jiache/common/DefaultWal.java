package io.jiache.common;

import io.jiache.util.Assert;
import io.jiache.util.ByteUtils;
import io.jiache.util.Serializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class DefaultWal implements Wal, AutoCloseable {
    private static final byte[] LAST_INDEX_KEY = "LAST_INDEX".getBytes();
    private static final byte[] LAST_TERM_KEY = "LAST_TERM".getBytes();

    static {
        RocksDB.loadLibrary();
    }

    private String walPath;

    private DefaultWal(String walPath) {
        this.walPath = walPath;
    }

    public static DefaultWal newInstance(String walPath) {
        return new DefaultWal(walPath);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void put(long index, Entry entry) {
        Assert.checkNull(walPath, "wal path");
        try(
                Options options = new Options().setCreateIfMissing(true);
                RocksDB wal = RocksDB.open(options, walPath)
        ) {
            wal.put(ByteUtils.longToBytes(index), Serializer.serialize(entry));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(long[] index, Entry[] entries) {
        Assert.checkNull(walPath, "wal path");
        Assert.check(index.length == entries.length, "length of index array and entry array is not same.");
        try(
                Options options = new Options().setCreateIfMissing(true);
                RocksDB wal = RocksDB.open(options, walPath)
        ) {
            for(int i=0; i<index.length; ++i) {
                wal.put(ByteUtils.longToBytes(index[i]), Serializer.serialize(entries[i]));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Entry get(long index) {
        Assert.checkNull(walPath, "wal path");
        byte[] byteEntry = null;
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            byteEntry = wal.get(ByteUtils.longToBytes(index));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        Assert.checkNull(byteEntry, "bytes entry");
        return Serializer.deSerialize(byteEntry, Entry.class);
    }

    @Override
    public Entry[] get(long[] index) {
        Assert.checkNull(walPath, "wal path");
        Assert.checkNull(index, "index array");
        Entry[] entries = new Entry[index.length];
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {

            for(int i=0; i<index.length; ++i) {
                entries[i] = Serializer.deSerialize(wal.get(ByteUtils.longToBytes(i)), Entry.class);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return entries;
    }

    @Override
    public void delete(long index) {
        Assert.checkNull(walPath, "wal path");
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            wal.delete(ByteUtils.longToBytes(index));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(long[] index) {
        Assert.checkNull(walPath, "wal path");
        Assert.checkNull(index, "wal path");
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            for(int i=0; i<index.length; ++i) {
                wal.delete(ByteUtils.longToBytes(index[i]));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getLastIndex() {
        Assert.checkNull(walPath, "wal path");
        long lastIndex = -1;
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            byte[] bytes = wal.get(LAST_INDEX_KEY);
            if(bytes != null) {
                lastIndex = ByteUtils.bytesToLong(bytes);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return lastIndex;
    }

    @Override
    public void setLastIndex(long lastIndex) {
        Assert.checkNull(walPath, "wal path");
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            wal.put(LAST_INDEX_KEY, ByteUtils.longToBytes(lastIndex));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getLastTerm() {
        Assert.checkNull(walPath, "wal path");
        long lastTerm = 0;
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            byte[] bytes = wal.get(LAST_TERM_KEY);
            if(bytes != null) {
                lastTerm = ByteUtils.bytesToLong(bytes);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return lastTerm;
    }

    @Override
    public void setLastTerm(long lastTerm) {
        Assert.checkNull(walPath, "wal path");
        try(
                RocksDB wal = RocksDB.open(walPath)
        ) {
            wal.put(LAST_TERM_KEY, ByteUtils.longToBytes(lastTerm));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
