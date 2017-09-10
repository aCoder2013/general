package com.song.general.gossip.net.handler

import com.song.general.gossip.EndpointState
import com.song.general.gossip.Gossip
import com.song.general.gossip.GossipAction
import com.song.general.gossip.message.GossipDigestAck2Message
import com.song.general.gossip.message.GossipDigestAckMessage
import com.song.general.gossip.net.Message
import com.song.general.gossip.utils.GsonUtils
import org.slf4j.LoggerFactory
import java.net.SocketAddress

/**
 * Created by song on 2017/9/9.
 */
class GossipDigestAckMessageHandler(g: Gossip) : AbstractMessageHandler(g) {

    override fun handleMessage(message: Message) {
        logger.trace("Received message from ${message.from}")
        /*
            Ignore ack message before we send the first syn message.
         */
        if (gossip.firstSynSendAt == 0L
                || (System.nanoTime() - gossip.firstSynSendAt < 0L)) {
            logger.info("Ignore invalid ack message ${GsonUtils.toJson(message)}")
            return
        }
        val gossipDigestAckMessage = message.payload as GossipDigestAckMessage
        gossip.applyStateLocally(gossipDigestAckMessage.endpointStateMap)
        val requestEndpointStateMap = HashMap<SocketAddress, EndpointState>()
        gossipDigestAckMessage.gossipDigests.forEach({
            val endpointState = gossip.endpointsMap[it.socketAddress]
            if (endpointState != null) {
                requestEndpointStateMap.put(it.socketAddress, endpointState)
            }
        })
        logger.trace("Send ack2 message to ${message.from}")
        gossip.messageClient.sendOneWay(message.from, Message(gossip.localSocketAddress,
                GossipAction.GOSSIP_ACK_2, GossipDigestAck2Message(requestEndpointStateMap)))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GossipDigestAckMessageHandler::class.java)
    }
}