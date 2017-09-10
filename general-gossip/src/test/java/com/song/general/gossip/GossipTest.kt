package com.song.general.gossip

import com.song.general.gossip.net.MessageClient
import com.song.general.gossip.support.DefaultSeedProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


/**
 * Created by song on 2017/9/3.
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class GossipTest {

    @Before
    fun setUp() {
        Mockito.mock(MessageClient::class.java)
    }

    @Test
    fun send() {
        val seedProvider = getSeedProvider()
    }

    private fun getSeedProvider(): SeedProvider {
        val seedProvider = DefaultSeedProvider()
        seedProvider.addSeed("127.0.0.1:7006")
        seedProvider.addSeed("127.0.0.1:7005")
        return seedProvider
    }
}