package com.song.general.gossip

import com.google.common.collect.ImmutableList
import com.song.general.gossip.concurrent.DefaultThreadFactory
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
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import kotlin.collections.ArrayList

/**
 * Created by song on 2017/8/13.
 */
class Gossip : LifeCycle {

    private val host: String
    private val port: Int
    private val localSocketAddress: SocketAddress
    private val seedProvider: SeedProvider

    private val intervalInMillis = 1000L

    private val scheduledGossipTaskExecutor: ScheduledExecutorService

    private val messageServer: DefaultMessageServer

    private val messageClient: DefaultMessageClient

    private val taskLock: ReentrantLock

    @Volatile
    private var firstSynSendAt = 0L

    private val random: ThreadLocalRandom

    /**
     * custom payload that will be shared among endpoints
     */
    var payload: String = ""

    private val seeds: ConcurrentSkipListSet<SocketAddress>

    private val liveEndpoints: ConcurrentSkipListSet<SocketAddress>

    private val deadEndpoints: ConcurrentSkipListSet<SocketAddress>

    private val endpointsMap: ConcurrentHashMap<SocketAddress, EndpointState>

    constructor(host: String, port: Int, seedProvider: SeedProvider) {
        this.host = host
        this.port = port
        this.localSocketAddress = NetUtils.string2SocketAddress("$host:$port")
        this.seedProvider = seedProvider
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

    override fun shutDown() {
        messageClient.shutDown()
        messageServer.shutDown()
    }

    inner class GossipTask : Runnable {

        override fun run() {
            try {
                taskLock.lock()
                endpointsMap[localSocketAddress]?.heartbeatState?.updateHeartbeat()
                logger.trace("Heartbeat of local endpoint is {}", endpointsMap[localSocketAddress]?.heartbeatState?.version ?: 0)
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

        private val INSTANCE = Gossip()
    }

}
