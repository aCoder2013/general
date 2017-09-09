package com.song.general.gossip.net.handler

import com.song.general.gossip.Gossip
import com.song.general.gossip.GossipAction
import com.song.general.gossip.message.GossipDigestSynMessage
import com.song.general.gossip.net.handler.MessageHandler
import com.song.general.gossip.net.Message
import com.song.general.gossip.utils.GsonUtils
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/9/3.
 */
class GossipDigestSynMessageHandler : MessageHandler {

    override fun handleMessage(message: Message) {
        logger.trace("Received message ${GsonUtils.toJson(message)}")
        val from = message.from
        if (Gossip.getInstance().localSocketAddress == from) {
            logger.warn("Ignore message {${GsonUtils.toJson(message)}} sending from itself.")
            return
        }
        if (message.createTime < Gossip.getInstance().firstSynSendAt) {
            logger.warn("Ignore message received before startup.")
            return
        }
        val digestSynMessage = message.payload as GossipDigestSynMessage
        val gossipDigestAckMessage = Gossip.getInstance().mergeDigest(digestSynMessage.gossipDigests)
        val msg = Message(Gossip.getInstance().localSocketAddress, GossipAction.GOSSIP_ACK, gossipDigestAckMessage)
        Gossip.getInstance().messageClient.sendOneWay(from, msg)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GossipDigestSynMessage::class.java)
    }
}