package com.song.middleware.client.util

import org.junit.Test

/**
 * Created by song on 2017/8/6.
 */
class InetUtilsTest {

    @Test
    @Throws(Exception::class)
    fun findFirstNonLoopbackAddress() {
        val address = InetUtils.findFirstNonLoopbackAddress()
        println(address!!.hostAddress)
        println(address.hostName)
    }

}