package com.song.general.gossip.net.handler

import com.song.general.gossip.Gossip
import com.song.general.gossip.MessageHandler
import com.song.general.gossip.message.GossipDigestSynMessage
import com.song.general.gossip.net.Message
import com.song.general.gossip.utils.JsonUtils
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/9/3.
 */
class GossipDigestSynMessageHandler : MessageHandler {

    override fun handleMessage(message: Message) {
        logger.trace("Received message ${JsonUtils.toJson(message)}")
        val from = message.from
        if (Gossip.getInstance().localSocketAddress == from) {
            logger.warn("Ignore message{${JsonUtils.toJson(message)}} sending from itself.")
            return
        }
        val digestSynMessage = message.payload as GossipDigestSynMessage
        for (gossipDigest in digestSynMessage.gossipDigests) {

        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GossipDigestSynMessage::class.java)
    }
}