package com.song.general.gossip

import com.song.general.gossip.support.DefaultSeedProvider
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

/**
 * Created by song on 2017/9/3.
 */
class GossipTest {

    @Before
    fun setUp() {
    }

    @Test
    fun send() {
        getSeedProvider()
    }

    private fun getSeedProvider(): SeedProvider {
        val seedProvider = DefaultSeedProvider()
        seedProvider.addSeed("127.0.0.1:7006")
        seedProvider.addSeed("127.0.0.1:7005")
        return seedProvider
    }

    @Test
    fun multiThreadCreateInstance() {
        val latch = CountDownLatch(5)
        for (i in 0..5) {
            Thread({
                if (i % 2 == 0) {
                    val createInstance = Gossip.createInstance("127.0.0.1", 8080, getSeedProvider())
                    println("${Thread.currentThread().name} :  create $createInstance")
                } else {
                    println("instance is ${Gossip.getInstance()}")
                }
            }).start()
        }
        Thread.sleep(500000)
    }
}