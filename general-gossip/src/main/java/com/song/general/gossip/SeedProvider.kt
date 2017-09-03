package com.song.general.gossip

import java.net.SocketAddress

/**
 * Created by song on 2017/9/2.
 */
interface SeedProvider {

    /**
     * get all seeds
     *
     */
    fun getSeeds(): List<SocketAddress>

}