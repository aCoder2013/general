package com.song.general.gossip.net

import com.song.general.gossip.GossipAction
import java.io.Serializable

/**
 * Created by song on 2017/8/19.
 */
class Message<T>(var version: Int, var action: GossipAction?, var payload: T, var createTime: Long) : Serializable {
    companion object {

        private const val serialVersionUID = 2161395957955391220L
    }

}
