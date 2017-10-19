package io.jiache.originalServer;

import io.jiache.common.RaftConf;

public class Follower extends BaseServer{

    public Follower(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
    }

    @Override
    public void run() {

    }
}
