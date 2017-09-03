package com.song.general.gossip

import java.io.Serializable
import java.net.SocketAddress

/**
 * Created by song on 2017/8/13.
 */
class GossipDigest(val socketAddress: SocketAddress, val generation: Long, val version: Int) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as GossipDigest?

        if (version != that!!.version) {
            return false
        }
        return socketAddress == that.socketAddress
    }

    override fun hashCode(): Int {
        var result = socketAddress.hashCode()
        result = 31 * result + version
        return result
    }

    override fun toString(): String {
        return "GossipDigest{" +
                "inetAddress=" + socketAddress +
                ", version=" + version +
                '}'
    }

    companion object {

        private const val serialVersionUID = 557834866830039832L
    }
}
