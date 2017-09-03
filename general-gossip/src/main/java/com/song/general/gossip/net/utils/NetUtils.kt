package com.song.general.gossip.net.utils

import com.song.general.gossip.net.support.GossipSocketAddress
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Created by song on 2017/8/20.
 */
object NetUtils {

    @Volatile
    private var localHost: InetAddress = InetAddress.getLocalHost()

    fun string2SocketAddress(address: String): GossipSocketAddress {
        try {
            val s = address.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val inetSocketAddress = GossipSocketAddress(s[0], Integer.parseInt(s[1]))
            if (!inetSocketAddress.isUnresolved) {
                return inetSocketAddress
            } else {
                throw UnknownHostException(address)
            }
        } catch (e: Exception) {
            throw UnknownHostException(address)
        }
    }

    fun getLocalHost(): InetAddress = localHost

}
