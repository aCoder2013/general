package com.song.general.gossip.net.handler

import com.song.general.gossip.Gossip
import com.song.general.gossip.message.GossipDigestAck2Message
import com.song.general.gossip.net.handler.MessageHandler
import com.song.general.gossip.net.Message
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/9/9.
 */
class GossipDigestAck2MessageHandler : MessageHandler {

    override fun handleMessage(message: Message) {
        val from = message.from
        val localSocketAddress = Gossip.getInstance().localSocketAddress
        if (localSocketAddress == from) {
            logger.info("Ignore ack2 message from itself.")
            return
        }
        val gossipDigestAck2Message = message.payload as GossipDigestAck2Message
        Gossip.getInstance().applyStateLocally(gossipDigestAck2Message.endpointStateMap)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GossipDigestAck2MessageHandler::class.java)
    }
}