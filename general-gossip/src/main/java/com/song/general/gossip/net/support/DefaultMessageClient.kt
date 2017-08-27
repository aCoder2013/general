package com.song.general.gossip.net.support

import com.song.general.gossip.concurrent.DefaultThreadFactory
import com.song.general.gossip.net.Message
import com.song.general.gossip.net.MessageClient
import com.song.general.gossip.net.handler.InboundMessageDispatchHandler
import com.song.general.gossip.net.utils.NetUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by song on 2017/8/20.
 */
class DefaultMessageClient : MessageClient {

    private var workerGroup: EventLoopGroup? = null

    private var messageGroup: DefaultEventExecutorGroup? = null

    private val bootstrap = Bootstrap()

    private var createChannelLock: Lock? = null

    private val channelCache = ConcurrentHashMap<String/*address of the node*/, ChannelAdapter>()

    override fun start() {
        logger.info("Start to init MessageClient")
        val start = System.currentTimeMillis()
        this.createChannelLock = ReentrantLock()
        val coreNum = Runtime.getRuntime().availableProcessors()
        workerGroup = NioEventLoopGroup(coreNum,
                DefaultThreadFactory("Message-client-service"))
        messageGroup = DefaultEventExecutorGroup(coreNum * 2,
                DefaultThreadFactory("Message-client-worker"))
        bootstrap.group(workerGroup!!)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(object : ChannelInitializer<SocketChannel>() {

                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast("log", LoggingHandler())
                                .addLast("messageEncoder", ObjectEncoder())
                                .addLast("messageDecoder", ObjectDecoder(MAX_OBJECT_SIZE, ClassResolvers
                                        .weakCachingResolver(Thread.currentThread().contextClassLoader)))
                                .addLast(IdleStateHandler(0, 0, DEFAULT_CONNECTION_IDLE_SECONDS))
                                .addLast(messageGroup, "messageDispatcher",
                                        InboundMessageDispatchHandler())
                    }
                })
        logger
                .info("Finish to init MessageClient, cost {} ms", System.currentTimeMillis() - start)
    }

    override fun shutDown() {
        if (workerGroup != null) {
            workerGroup!!.shutdownGracefully()
        }
        if (messageGroup != null) {
            messageGroup!!.shutdownGracefully()
        }
    }

    @Throws(Exception::class)
    override fun <T> sendOneWay(address: String, message: Message<T>) {
        val channel = getOrCreateChannel(address)
        if (channel != null) {
            channel.writeAndFlush(message).addListener({ future ->
                if (future.isSuccess) {
                    logger.info("Send message successfully, $message")
                } else {
                    logger.error("Send message failed", future.cause())
                }
            })
        } else {
            logger.error("send message $message to node[$address] failed")
        }
    }

    override fun <T> sendAsync(address: String, message: Message<T>): MessageFuture<T> {
        var channel: Channel? = null
        var cause: Throwable? = null
        try {
            channel = getOrCreateChannel(address)
        } catch (e: Exception) {
            cause = e
        }

        val messageFuture = MessageFuture<T>()
        if (channel != null) {
            channel.writeAndFlush(message).addListener { future ->
                if (future.isSuccess) {
                    messageFuture.isSuccess = true
                } else {
                    messageFuture.isSuccess = false
                    messageFuture.cause(future.cause())
                }
            }
        } else {
            if (cause != null) {
                messageFuture.isSuccess = false
                messageFuture.cause(cause)
            }
        }
        return messageFuture
    }

    @Throws(InterruptedException::class)
    private fun getOrCreateChannel(address: String): Channel? {
        var channelAdapter: ChannelAdapter? = this.channelCache[address]
        if (channelAdapter == null) {
            channelAdapter = ChannelAdapter(address)
            val channel = (this.channelCache as ConcurrentMap<String, ChannelAdapter>).putIfAbsent(address, channelAdapter)
            if (channel != null) {
                channelAdapter = channel
            }
        }
        if (!channelAdapter.isOk) {
            if (this.createChannelLock!!.tryLock()) {
                logger.info("Try to connect to node {} .", address)
                try {
                    val channelFuture = this.bootstrap
                            .connect(NetUtils.string2SocketAddress(address))
                    if (channelFuture
                            .await(DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND.toLong(), TimeUnit.SECONDS) && channelFuture.isDone) {
                        if (channelFuture.isSuccess) {
                            channelAdapter.channelFuture = channelFuture
                        } else if (channelFuture.isCancelled) {
                            throw RuntimeException(
                                    "Connecting to node $address got cancelled.")
                        } else {
                            throw RuntimeException(
                                    "Connecting to node $address failed,detail :", channelFuture
                                    .cause())
                        }
                    } else {
                        throw RuntimeException("Can't connect to " + address + " during "
                                + DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND + " seconds.")
                    }
                } catch (e: Exception) {
                    logger.error("createChannel: connect to node[{}] failed.", address, e)
                } finally {
                    this.createChannelLock!!.unlock()
                }
            } else {
                logger.info(
                        "Someone is trying to connect to node $address, so we'll just wait.")
                val await = channelAdapter
                        .await(DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND.toLong(), TimeUnit.SECONDS)
                if (!await) {
                    throw RuntimeException("Connect to node " + address + " timeout after "
                            + DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND + " seconds.")
                }

            }
        }
        if (channelAdapter.isOk) {
            logger.info("Finish to connect to node {} .", address)
            return channelAdapter.geChannel()
        } else {
            logger.warn("createChannel : connect to remote node[{}] timeout")
        }
        return null
    }

    companion object {

        private val logger = LoggerFactory.getLogger(DefaultMessageClient::class.java)

        /**
         * 最大消息大小，默认为4M
         */
        private val MAX_OBJECT_SIZE = 4 * 1024 * 1024
        private val DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND = 3
        private val DEFAULT_CONNECTION_IDLE_SECONDS = 60
    }
}
