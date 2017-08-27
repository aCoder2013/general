package com.song.general.gossip

import java.io.Serializable
import java.net.InetAddress

/**
 * Created by song on 2017/8/13.
 */
class GossipDigest(private val inetAddress: InetAddress, private val version: Int) : Serializable {

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
        return inetAddress == that.inetAddress
    }

    override fun hashCode(): Int {
        var result = inetAddress.hashCode()
        result = 31 * result + version
        return result
    }

    override fun toString(): String {
        return "GossipDigest{" +
                "inetAddress=" + inetAddress +
                ", version=" + version +
                '}'
    }

    companion object {

        private const val serialVersionUID = 557834866830039832L
    }
}
