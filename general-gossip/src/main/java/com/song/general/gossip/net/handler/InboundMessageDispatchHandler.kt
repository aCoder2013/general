package com.song.general.gossip.net.handler

import com.song.general.gossip.GossipAction
import com.song.general.gossip.MessageHandler
import com.song.general.gossip.net.Message
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/8/19.
 */
class InboundMessageDispatchHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val message = msg as Message<*>
        message.action?.let {
            val handler = getHandler(it)
            handler?.handleMessage(message)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("read message failed", cause)
    }

    private fun getHandler(gossipAction: GossipAction): MessageHandler?
            = messageHandlers.getOrDefault(gossipAction, PrintMessageHandler())

    companion object {

        private val logger = LoggerFactory
                .getLogger(InboundMessageDispatchHandler::class.java)

        private val messageHandlers = HashMap<GossipAction, MessageHandler>()

    }
}
