package com.song.general.gossip.net.support

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import java.net.SocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by song on 2017/8/23.
 */
class ChannelAdapter(private val address: SocketAddress) {

    @Volatile var channelFuture: ChannelFuture? = null
        set(channelFuture) {
            field = channelFuture
            this.countDownLatch.countDown()
        }

    private val countDownLatch = CountDownLatch(1)

    val isOk: Boolean
        get() = this.channelFuture != null && this.channelFuture!!.channel() != null && this.channelFuture!!.channel()
                .isActive

    fun geChannel(): Channel {
        if (!isOk) {
            throw RuntimeException(
                    "Connection to node $address is currently not usable.")
        }
        return this.channelFuture!!.channel()
    }

    @Throws(InterruptedException::class)
    fun await() {
        this.countDownLatch.await()
    }

    @Throws(InterruptedException::class)
    fun await(timeout: Long, timeUnit: TimeUnit): Boolean {
        return this.countDownLatch.await(timeout, timeUnit)
    }

}
