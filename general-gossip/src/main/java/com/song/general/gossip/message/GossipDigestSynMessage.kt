package com.song.general.gossip.message

import com.google.gson.GsonBuilder
import com.song.general.gossip.GossipDigest
import java.io.Serializable

/**
 * Created by song on 2017/9/2.
 */
class GossipDigestSynMessage(val gossipDigests: List<GossipDigest>) : Serializable{

    override fun toString(): String {
        return GsonBuilder().create().toJson(this)
    }
}