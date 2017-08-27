package com.song.general.gossip.net.handler

import com.song.general.gossip.net.Message
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/8/19.
 */
class OutboundMessageDispatchHandler : ChannelOutboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val message = msg as Message<*>
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }

    companion object {

        private val logger = LoggerFactory
                .getLogger(OutboundMessageDispatchHandler::class.java)
    }
}
