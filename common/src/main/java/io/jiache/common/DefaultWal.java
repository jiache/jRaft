package io.jiache.common;

import io.jiache.util.Assert;
import io.jiache.util.Serializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class DefaultWal implements Wal {
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
    public void put(long index, Entry entry) {
        Assert.checkNull(walPath, "wal path");
        try(
                Options options = new Options().setCreateIfMissing(true);
                RocksDB wal = RocksDB.open(options, walPath)
        ) {
            wal.put(new byte[]{(byte) index}, Serializer.serialize(entry));
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
                wal.put(new byte[]{(byte) index[i]}, Serializer.serialize(entries[i]));
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
            byteEntry = wal.get(new byte[]{(byte) index});
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
                entries[i] = Serializer.deSerialize(wal.get(new byte[]{(byte) index[i]}), Entry.class);
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
            wal.delete(new byte[]{(byte) index});
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
                wal.delete(new byte[]{(byte) index[i]});
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
