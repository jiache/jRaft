package io.jiache.common;



public interface Server extends Runnable {
    long HEART_BEAT_INTERVAL = 30;
    long OFFLOAD_TO_SECRETARY_INTERVAL = 3;
    long OFFLOAD_SECRETARY_TO_FOLLOWER_INTERVAL = 10;
    long OFFLOAD_LEADER_HEARD_BEAT_INTERVAL = 50;
}
