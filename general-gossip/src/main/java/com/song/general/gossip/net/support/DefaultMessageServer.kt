package com.song.general.gossip.net.support

import com.song.general.gossip.net.MessageServer
import com.song.general.gossip.net.config.MessageConfig
import com.song.general.gossip.net.handler.InboundMessageDispatchHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.logging.LoggingHandler
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Created by song on 2017/8/20.
 */
class DefaultMessageServer(val socketAddress: SocketAddress) : MessageServer {

    private val bootstrap = ServerBootstrap()

    private val coreNum = Runtime.getRuntime().availableProcessors()

    private var bossGroup: EventLoopGroup = NioEventLoopGroup(coreNum)
    private var workerGroup: EventLoopGroup = NioEventLoopGroup(coreNum * 2)
    private var messageGroup: EventLoopGroup = NioEventLoopGroup(coreNum * 2)

    override fun start() {
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(InetSocketAddress(MessageConfig.serverPort))
                .childHandler(object : ChannelInitializer<SocketChannel>() {

                    @Throws(Exception::class)
                    override fun initChannel(socketChannel: SocketChannel) {
                        socketChannel.pipeline()
                                .addLast("log", LoggingHandler())
                                .addLast("messageEncoder", ObjectEncoder())
                                .addLast("messageDecoder",
                                        ObjectDecoder(MessageConfig.maxMsgSize, ClassResolvers
                                                .weakCachingResolver(
                                                        Thread.currentThread().contextClassLoader)))
                                /*
                                    业务逻辑在自己的线程池中处理，避免阻塞IO线程
                                */
                                .addLast(messageGroup, "messageDispatcher",
                                        InboundMessageDispatchHandler())
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

        try {
            bootstrap.bind(socketAddress).sync()
        } catch (e: InterruptedException) {
            throw RuntimeException("Sync bind server got interrupted", e)
        }

    }

    override fun shutDown() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        messageGroup.shutdownGracefully()
    }
}
