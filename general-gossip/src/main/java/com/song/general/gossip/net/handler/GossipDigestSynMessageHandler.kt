package com.song.general.gossip.net.handler

import com.song.general.gossip.MessageHandler
import com.song.general.gossip.message.GossipDigestSynMessage
import com.song.general.gossip.net.Message
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/9/3.
 */
class GossipDigestSynMessageHandler : MessageHandler {

    override fun handleMessage(message: Message) {
        val digestSynMessage = message.payload as GossipDigestSynMessage
        logger.trace("Received message $digestSynMessage")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GossipDigestSynMessage::class.java)
    }
}