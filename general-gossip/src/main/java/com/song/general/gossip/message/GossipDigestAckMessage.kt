package com.song.general.gossip.message

import com.song.general.gossip.EndpointState
import com.song.general.gossip.GossipDigest
import java.io.Serializable
import java.net.SocketAddress

/**
 * Created by song on 2017/9/9.
 */
class GossipDigestAckMessage(val gossipDigests: List<GossipDigest>, val endpointStateMap: Map<SocketAddress, EndpointState>) : Serializable