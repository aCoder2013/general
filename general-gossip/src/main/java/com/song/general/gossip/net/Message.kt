package com.song.general.gossip.net

import com.song.general.gossip.GossipAction
import com.song.general.gossip.net.utils.NetUtils
import java.io.Serializable
import java.net.InetAddress

/**
 * Created by song on 2017/8/19.
 */
class Message : Serializable {
    val from: InetAddress
    var action: GossipAction
    var payload: Any
    var createTime: Long

    constructor(action: GossipAction, payload: Any) : this(NetUtils.getLocalHost(), action, payload, System.currentTimeMillis())

    constructor(from: InetAddress = NetUtils.getLocalHost(), action: GossipAction, payload: Any, createTime: Long = System.currentTimeMillis()) {
        this.from = from
        this.action = action
        this.payload = payload
        this.createTime = createTime
    }

    companion object {

        private const val serialVersionUID = 2161395957955391220L
    }

}
