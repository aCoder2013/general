package com.song.general.gossip.message

import com.song.general.gossip.GossipDigest
import com.song.general.gossip.utils.JsonUtils
import java.io.Serializable

/**
 * Created by song on 2017/9/2.
 */
class GossipDigestSynMessage(val gossipDigests: List<GossipDigest>) : Serializable {

    override fun toString(): String {
        return JsonUtils.toJson(this)
    }
}