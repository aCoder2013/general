package com.song.general.gossip.net.support

import org.junit.Assert
import org.junit.Test

/**
 * Created by song on 2017/9/3.
 */
class GossipSocketAddressTest {

    @Test
    fun name() {
        val gossipSocketAddress = GossipSocketAddress("127.0.0.1", 8080)
        var gossipSocketAddressOther = GossipSocketAddress("127.0.0.1", 8080)
        Assert.assertTrue(gossipSocketAddress == gossipSocketAddressOther)
        gossipSocketAddressOther = GossipSocketAddress("127.0.0.1", 8081)
        Assert.assertFalse(gossipSocketAddress == gossipSocketAddressOther)
    }
}