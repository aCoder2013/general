package com.song.general.gossip.net

import com.song.general.gossip.GossipAction
import com.song.general.gossip.net.support.GossipSocketAddress
import java.io.Serializable
import java.net.SocketAddress

/**
 * Created by song on 2017/8/19.
 */
class Message : Serializable {
    val from: SocketAddress
    var action: GossipAction
    var payload: Any
    var createTime: Long

    constructor(from: SocketAddress, action: GossipAction, payload: Any, createTime: Long = System.currentTimeMillis()) {
        this.from = from
        this.action = action
        this.payload = payload
        this.createTime = createTime
    }

    companion object {

        private const val serialVersionUID = 2161395957955391220L
    }

}
