package com.song.general.gossip

import com.song.general.gossip.utils.GlobalVersionGenerator

/**
 * Created by song on 2017/9/2.
 */
class HeartbeatState(val generation: Long = System.currentTimeMillis(), var version: Int = 0) {

    fun updateHeartbeat() {
        this.version = GlobalVersionGenerator.nextVersion()
    }

}