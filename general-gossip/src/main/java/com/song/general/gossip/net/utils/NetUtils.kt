package com.song.general.gossip.net.utils

import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Created by song on 2017/8/20.
 */
object NetUtils {

    fun string2SocketAddress(address: String): SocketAddress {
        val s = address.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        return InetSocketAddress(s[0], Integer.parseInt(s[1]))
    }

}
