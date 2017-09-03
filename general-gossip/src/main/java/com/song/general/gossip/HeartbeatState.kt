package com.song.general.gossip

/**
 * Created by song on 2017/9/2.
 */
class HeartbeatState(val generation: Long = System.currentTimeMillis(), var version: Int = 0) {

    fun updateHeartbeat() {
        this.version = GlobalVersionGenerator.nextVersion()
    }

}