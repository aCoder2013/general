package com.song.general.gossip

import com.song.general.gossip.support.DefaultSeedProvider
import org.junit.Before
import org.junit.Test

/**
 * Created by song on 2017/9/3.
 */
class GossipTest {

    @Before
    fun setUp() {
    }

    @Test
    fun send() {
        val seedProvider = DefaultSeedProvider()
        seedProvider.addSeed("127.0.0.1:7006")
        seedProvider.addSeed("127.0.0.1:7005")
        val gossip1 = Gossip("127.0.0.1", 7000, seedProvider)
        gossip1.payload = "gossip1"
        val gossip2 = Gossip("127.0.0.1", 7006, seedProvider)
        gossip2.payload = "gossip2"
        val gossip3 = Gossip("127.0.0.1", 7005, seedProvider)
        gossip2.payload = "gossip3"
        gossip1.start()
        gossip2.start()
        gossip3.start()
        Thread.sleep(100000L)
    }
}