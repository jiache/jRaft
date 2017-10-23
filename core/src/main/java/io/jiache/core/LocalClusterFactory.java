package io.jiache.core;

import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.util.Serializer;

public class LocalClusterFactory {
    public static LocalCluster createLocalCluster(Address local, RaftConf raftConf) {
        if(raftConf.getSecretaryAddressList() == null || raftConf.getSecretaryAddressList().size() == 0) {
            return new OriginalLocalCluster(local, raftConf);
        } else {
            return new OffloadLocalCluster(local, raftConf);
        }
    }
}
