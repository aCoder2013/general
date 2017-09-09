package com.song.general.gossip.message

import com.song.general.gossip.EndpointState
import java.io.Serializable
import java.net.SocketAddress

/**
 * Created by song on 2017/9/9.
 */
class GossipDigestAck2Message(val endpointStateMap: Map<SocketAddress, EndpointState>) : Serializable