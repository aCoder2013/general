package com.song.general.gossip.net.support

import java.net.InetSocketAddress

/**
 * Created by song on 2017/9/3.
 */
class GossipSocketAddress(hostname: String, port: Int) : InetSocketAddress(hostname, port), Comparable<GossipSocketAddress> {

    override fun compareTo(other: GossipSocketAddress): Int {
        return "$hostName:$port".compareTo("${other.address}:${other.port}")
    }

    override fun toString(): String {
        return "$hostName:$port"
    }
}