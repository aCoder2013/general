package com.song.general.gossip.support

import com.song.general.gossip.exception.ConfigurationException
import org.junit.Test

/**
 * Created by song on 2017/9/2.
 */
class DefaultSeedProviderTest {

    @Test
    fun addSeed() {
        val seedProvider = DefaultSeedProvider()
        seedProvider.addSeed("localhost")
        println(seedProvider.getSeeds())
    }

    @Test(expected = ConfigurationException::class)
    fun addInvalidSeed() {
        val seedProvider = DefaultSeedProvider()
        seedProvider.addSeed("invalid seed")
        seedProvider.getSeeds()
    }

    @Test
    fun getSeeds() {
    }

}