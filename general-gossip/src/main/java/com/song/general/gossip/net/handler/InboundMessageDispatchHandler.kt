package com.song.general.gossip.net.handler

import com.song.general.gossip.Gossip
import com.song.general.gossip.GossipAction
import com.song.general.gossip.net.Message
import com.song.general.gossip.utils.GsonUtils
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/8/19.
 */
class InboundMessageDispatchHandler(val gossip: Gossip) : ChannelInboundHandlerAdapter() {

    init {
        /*
            register message handlers.
         */
        messageHandlers.put(GossipAction.GOSSIP_SYN, GossipDigestSynMessageHandler(gossip))
        messageHandlers.put(GossipAction.GOSSIP_ACK, GossipDigestAckMessageHandler(gossip))
        messageHandlers.put(GossipAction.GOSSIP_ACK_2, GossipDigestAck2MessageHandler(gossip))
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val message = msg as Message
        message.action.let {
            getHandler(it)?.handleMessage(message) ?: logger.warn("Ignore unknown message type : ${GsonUtils.toJson(message)}")
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = logger.error("Process incoming message failed", cause)

    private fun getHandler(gossipAction: GossipAction): MessageHandler?
            = messageHandlers[gossipAction]

    companion object {

        private val logger = LoggerFactory
                .getLogger(InboundMessageDispatchHandler::class.java)

        private val messageHandlers = HashMap<GossipAction, MessageHandler>()
    }
}
