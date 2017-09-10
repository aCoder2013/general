package com.song.general.gossip.net.support

import com.song.general.gossip.concurrent.DefaultThreadFactory
import com.song.general.gossip.net.Message
import com.song.general.gossip.net.MessageClient
import com.song.general.gossip.net.handler.InboundMessageDispatchHandler
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
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
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

    private val channelCache = ConcurrentHashMap<SocketAddress/*address of the node*/, ChannelAdapter>()

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
        workerGroup?.shutdownGracefully()
        messageGroup?.shutdownGracefully()
    }

    @Throws(Exception::class)
    override fun sendOneWay(socketAddress: SocketAddress, message: Message) {
        val channel = getOrCreateChannel(socketAddress)
        if (channel != null) {
            channel.writeAndFlush(message).addListener({ future ->
                if (future.isSuccess) {
                    logger.info("Send message successfully, $message")
                } else {
                    logger.error("Send message failed", future.cause())
                }
            })
        } else {
            logger.error("send message $message to node[$socketAddress] failed")
        }
    }

    override fun sendAsync(socketAddress: SocketAddress, message: Message): MessageFuture {
        var channel: Channel? = null
        var cause: Throwable? = null
        try {
            channel = getOrCreateChannel(socketAddress)
        } catch (e: Exception) {
            cause = e
        }

        val messageFuture = MessageFuture()
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
            messageFuture.isSuccess = false
            cause?.let { messageFuture.cause(it) }
        }
        return messageFuture
    }

    @Throws(InterruptedException::class)
    private fun getOrCreateChannel(socketAddress: SocketAddress): Channel? {
        var channelAdapter: ChannelAdapter? = this.channelCache[socketAddress]
        if (channelAdapter == null) {
            channelAdapter = ChannelAdapter(socketAddress)
            val channel = this.channelCache.putIfAbsent(socketAddress, channelAdapter)
            if (channel != null) {
                channelAdapter = channel
            }
        }
        if (!channelAdapter.isOk) {
            if (this.createChannelLock!!.tryLock()) {
                logger.info("Try to connect to node {} .", socketAddress)
                try {
                    val channelFuture = this.bootstrap
                            .connect(socketAddress)
                    if (channelFuture
                            .await(DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND.toLong(), TimeUnit.SECONDS) && channelFuture.isDone) {
                        if (channelFuture.isSuccess) {
                            channelAdapter.channelFuture = channelFuture
                        } else if (channelFuture.isCancelled) {
                            throw RuntimeException(
                                    "Connecting to node $socketAddress got cancelled.")
                        } else {
                            throw RuntimeException(
                                    "Connecting to node $socketAddress failed,detail :", channelFuture
                                    .cause())
                        }
                    } else {
                        throw RuntimeException("Can't connect to " + socketAddress + " during "
                                + DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND + " seconds.")
                    }
                } catch (e: Exception) {
                    logger.error("createChannel: connect to node[{}] failed.", socketAddress, e)
                } finally {
                    this.createChannelLock!!.unlock()
                }
            } else {
                logger.info(
                        "Someone is trying to connect to node $socketAddress, so we'll just wait.")
                val await = channelAdapter
                        .await(DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND.toLong(), TimeUnit.SECONDS)
                if (!await) {
                    throw RuntimeException("Connect to node " + socketAddress + " timeout after "
                            + DEFAULT_CHANNEL_CONNECT_TIMEOUT_SECOND + " seconds.")
                }

            }
        }
        if (channelAdapter.isOk) {
            logger.info("Finish to connect to node {} .", socketAddress)
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
