package com.song.general.gossip.support

import com.song.general.gossip.SeedProvider
import com.song.general.gossip.exception.ConfigurationException
import com.song.general.gossip.net.utils.NetUtils
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by song on 2017/9/2.
 */
class DefaultSeedProvider : SeedProvider {

    private val seeds = ArrayList<SocketAddress>()

    fun addSeed(host: String): DefaultSeedProvider {
        try {
            this.seeds.add(NetUtils.string2SocketAddress(host.trim()))
        } catch (e: UnknownHostException) {
            logger.warn("Seed provider can't parse host : $host")
        }
        return this
    }

    override fun getSeeds(): List<SocketAddress> {
        if (seeds.isEmpty()) {
            throw ConfigurationException("Seed provider is empty ,please check.")
        }
        return Collections.unmodifiableList(seeds)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSeedProvider::class.java)
    }
}