package com.song.general.gossip

import com.google.common.collect.ImmutableList
import com.song.general.gossip.concurrent.DefaultThreadFactory
import com.song.general.gossip.message.GossipDigestAckMessage
import com.song.general.gossip.message.GossipDigestSynMessage
import com.song.general.gossip.net.LifeCycle
import com.song.general.gossip.net.Message
import com.song.general.gossip.net.support.DefaultMessageClient
import com.song.general.gossip.net.support.DefaultMessageServer
import com.song.general.gossip.net.utils.NetUtils
import org.slf4j.LoggerFactory
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by song on 2017/8/13.
 */
class Gossip private constructor(host: String, port: Int, private val seedProvider: SeedProvider) : LifeCycle {

    val localSocketAddress: SocketAddress

    val intervalInMillis = 1000L

    private val scheduledGossipTaskExecutor: ScheduledExecutorService

    private val messageServer: DefaultMessageServer

    val messageClient: DefaultMessageClient

    private val taskLock: ReentrantLock

    @Volatile
    var firstSynSendAt = 0L

    private val random: ThreadLocalRandom

    /**
     * custom payload that will be shared among endpoints
     */
    var payload: String = ""

    val seeds: ConcurrentSkipListSet<SocketAddress>

    val liveEndpoints: ConcurrentSkipListSet<SocketAddress>

    val deadEndpoints: ConcurrentSkipListSet<SocketAddress>

    val endpointsMap: ConcurrentHashMap<SocketAddress, EndpointState>

    init {
        this.localSocketAddress = NetUtils.string2SocketAddress("$host:$port")
        this.scheduledGossipTaskExecutor = Executors.newSingleThreadScheduledExecutor(DefaultThreadFactory("Scheduled-Gossip-Task"))
        this.messageServer = DefaultMessageServer(localSocketAddress)
        this.messageClient = DefaultMessageClient()
        this.taskLock = ReentrantLock()
        this.random = ThreadLocalRandom.current()
        this.seeds = ConcurrentSkipListSet<SocketAddress>()
        this.liveEndpoints = ConcurrentSkipListSet<SocketAddress>()
        this.deadEndpoints = ConcurrentSkipListSet<SocketAddress>()
        this.endpointsMap = ConcurrentHashMap<SocketAddress, EndpointState>()
    }

    override fun start() {
        initSeedList()
        initLocalEndpoint()
        initMessageService()
        initScheduledTask()
    }

    fun applyStateLocally(endpointStateMap: Map<SocketAddress, EndpointState>) {
        endpointStateMap.forEach { address, remoteEndpoint ->
            val endpointState = this.endpointsMap.getOrElse(address, {
                EndpointState(HeartbeatState(), ApplicationState(""))
            })
            if (remoteEndpoint.heartbeatState.generation > endpointState.heartbeatState.generation
                    && remoteEndpoint.heartbeatState.version > endpointState.heartbeatState.version) {
                if (remoteEndpoint.applicationState.status == ApplicationState.StatusEnum.UP) {
                    logger.info("Node $address now is up.")
                } else {
                    logger.info("Node $address now is down.")
                }
                this.endpointsMap.put(address, remoteEndpoint)
            }
        }
    }

    fun mergeDigest(gossipDigest: List<GossipDigest>): GossipDigestAckMessage {
        val requestGossipDigests = ArrayList<GossipDigest>()
        val requestEndpointMap = HashMap<SocketAddress, EndpointState>()
        val gossipAckMessage = GossipDigestAckMessage(requestGossipDigests, requestEndpointMap)
        if (gossipDigest.isEmpty()) {
            logger.trace("Ignore empty gossip digests.")
            return gossipAckMessage
        }
        gossipDigest.forEach { gossipDigest ->
            val remoteGeneration = gossipDigest.generation
            val remoteVersion = gossipDigest.version
            val endpointState = this.endpointsMap[gossipDigest.socketAddress]
            if (endpointState != null) {
                val localGeneration = endpointState.heartbeatState.generation
                val localVersion = endpointState.heartbeatState.version
                if (remoteGeneration == localGeneration && remoteVersion == localVersion) {
                    logger.trace("Remote node has the same info with local node, so ignore.")
                    return@forEach
                }
                if (remoteGeneration > localGeneration) {
                    requestFromRemote(gossipDigest.socketAddress, remoteGeneration, requestGossipDigests)
                } else if (remoteGeneration < localGeneration) {
                    sendToRemote(gossipDigest.socketAddress, endpointState, requestEndpointMap)
                } else if (remoteGeneration == localGeneration) {
                    if (remoteVersion > localVersion) {
                        requestFromRemote(gossipDigest.socketAddress, remoteGeneration, requestGossipDigests)
                    } else if (remoteVersion < localVersion) {
                        sendToRemote(gossipDigest.socketAddress, endpointState, requestEndpointMap)
                    }
                }
            } else {
                requestFromRemote(gossipDigest.socketAddress, remoteGeneration, requestGossipDigests)
            }
        }
        return gossipAckMessage
    }

    private fun requestFromRemote(remoteAddress: SocketAddress, generation: Long, requestGossipDigests: MutableList<GossipDigest>) {
        logger.trace("Request from remote for $remoteAddress")
        requestGossipDigests.add(GossipDigest(remoteAddress, generation, 0))
    }

    private fun sendToRemote(remoteAddress: SocketAddress, endpointState: EndpointState, requestEndpointStateMap: MutableMap<SocketAddress, EndpointState>) {
        logger.trace("Send endpoint to remote node $remoteAddress ")
        requestEndpointStateMap.put(remoteAddress, endpointState)
    }

    override fun shutDown() {
        messageClient.shutDown()
        messageServer.shutDown()
    }

    inner class GossipTask : Runnable {

        override fun run() {
            try {
                taskLock.lock()
                val localEndpointState = endpointsMap[localSocketAddress] ?: throw RuntimeException("Local endpoint is null,please check.")
                logger.trace("Heartbeat of local endpoint is {}", localEndpointState.heartbeatState.version)
                val gossipDigests = fetchRandomGossipDigests()
                if (gossipDigests.isNotEmpty()) {
                    val message = Message(localSocketAddress, GossipAction.GOSSIP_SYN, GossipDigestSynMessage(gossipDigests))
                    val sendToSeed = sendGossip2LiveEndpoints(message)
                    sendGossip2DeadEndpoints(message)
                    if (!sendToSeed || this@Gossip.liveEndpoints.size < this@Gossip.seeds.size) {
                        mayGossip2Seed(message)
                    }
                }
            } catch (e: Throwable) {
                logger.error("Unknown error _${e.message}", e)
            } finally {
                taskLock.unlock()
            }
        }
    }

    private fun sendGossip2LiveEndpoints(message: Message): Boolean {
        if (liveEndpoints.isEmpty()) {
            return false
        }
        return sendGossip(message, this.liveEndpoints)
    }

    private fun sendGossip2DeadEndpoints(message: Message) {
        val size = this.deadEndpoints.size
        if (size > 0) {
            if (this.liveEndpoints.size < this.deadEndpoints.size) {
                sendGossip(message, this.deadEndpoints)
            }
        }
    }

    private fun mayGossip2Seed(message: Message) {
        val size = this.seeds.size
        if (size > 0) {
            if (size == 1 && seeds.contains(localSocketAddress)) {
                logger.warn("No need to send gossip to itself,{}", NetUtils.getLocalHost())
                return
            }
            if (this.liveEndpoints.size == 0) {
                sendGossip(message, seeds)
            } else {
                if (this.liveEndpoints.size < this.seeds.size) {
                    sendGossip(message, seeds)
                }
            }
        }
    }

    private fun sendGossip(message: Message, endpointSet: Set<SocketAddress>): Boolean {
        val liveEndpoints = ImmutableList.copyOf(endpointSet)
        val size = liveEndpoints.size
        if (size > 0) {
            val index = if (size == 1) 0 else random.nextInt(size)
            val to = liveEndpoints[index]
            if (firstSynSendAt == 0L) {
                firstSynSendAt = System.nanoTime()
            }
            this.messageClient.sendOneWay(to, message)
            return this.seeds.contains(to)
        }
        return false
    }

    private fun fetchRandomGossipDigests(): List<GossipDigest> {
        val gossipDigests = ArrayList<GossipDigest>(this.endpointsMap.size)
        val endpoints = ArrayList<SocketAddress>(this.endpointsMap.keys)
        Collections.shuffle(endpoints)
        endpoints.forEach(Consumer {
            var generation = 0L
            var version = 0
            val endpointState = this.endpointsMap[it]
            if (endpointState != null) {
                generation = endpointState.heartbeatState.generation
                version = endpointState.applicationState.version
            }
            gossipDigests.add(GossipDigest(it, generation, version))
        })
        return gossipDigests
    }

    private fun initScheduledTask() {
        scheduledGossipTaskExecutor
                .scheduleAtFixedRate(GossipTask(), intervalInMillis, intervalInMillis, TimeUnit.MILLISECONDS)
    }

    private fun initSeedList() {
        seedProvider.getSeeds()
                .filter { it != localSocketAddress }
                .forEach(Consumer {
                    this.seeds.add(it)
                })
    }

    private fun initLocalEndpoint() {
        val localEndpoint = EndpointState(HeartbeatState(), ApplicationState(this.payload, 0))
        localEndpoint.applicationState.markAlive()
        this.endpointsMap.putIfAbsent(localSocketAddress, localEndpoint)
    }

    private fun initMessageService() {
        messageServer.start()
        messageClient.start()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Gossip::class.java)

        @Volatile
        private var INSTANCE: Gossip? = null

        private val instanceLock = ReentrantLock()

        val instanceInitialized: Condition = instanceLock.newCondition()

        fun createInstance(host: String, port: Int, seedProvider: SeedProvider): Gossip {
            if (INSTANCE == null) {
                val lock = this.instanceLock
                try {
                    lock.lock()
                    if (INSTANCE == null) {
                        INSTANCE = Gossip(host, port, seedProvider)
                        instanceInitialized.signalAll()
                    }
                } finally {
                    lock.unlock()
                }
            }
            return INSTANCE as Gossip
        }

        fun getInstance(): Gossip {
            if (INSTANCE == null) {
                val lock = this.instanceLock
                try {
                    lock.lock()
                    instanceInitialized.await()
                } finally {
                    lock.unlock()
                }
            }
            return INSTANCE as Gossip
        }
    }

}
