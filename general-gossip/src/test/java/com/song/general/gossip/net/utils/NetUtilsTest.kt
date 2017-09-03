package com.song.general.gossip.net.utils

import org.junit.Test

/**
 * Created by song on 2017/9/2.
 */
class NetUtilsTest {

    @Test
    fun string2SocketAddress() {

    }

    @Test
    fun getLocalAddress() {
        val localAddress = NetUtils.getLocalHost()
        println(localAddress)
        println(localAddress.hostAddress)
    }

}