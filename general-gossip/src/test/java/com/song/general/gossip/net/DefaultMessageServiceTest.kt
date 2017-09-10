package com.song.general.gossip.net

import com.song.general.gossip.Gossip
import com.song.general.gossip.GossipAction
import com.song.general.gossip.GossipDigest
import com.song.general.gossip.net.support.DefaultMessageClient
import com.song.general.gossip.net.support.DefaultMessageServer
import com.song.general.gossip.net.utils.NetUtils
import com.song.general.gossip.support.DefaultSeedProvider
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

/**
 * Created by song on 2017/8/20.
 */
class DefaultMessageServiceTest {

    @Before
    @Throws(Exception::class)
    fun init() {
    }

    @Test
    @Throws(Exception::class)
    fun test() {
        val gossip = Gossip("127.0.0.1:", 2000, DefaultSeedProvider())
        //start server
        val socketAddress = NetUtils.string2SocketAddress("127.0.0.1:2000")
        val messageServer = DefaultMessageServer(gossip)
        messageServer.start()
        //start client
        val messageClient = DefaultMessageClient(gossip)
        messageClient.start()
        val gossipDigest = GossipDigest(NetUtils.string2SocketAddress("127.0.0.1:2001"), 1L, 0)
        val message = Message(NetUtils.string2SocketAddress("127.0.0.1:8080"), GossipAction.GOSSIP_ACK, gossipDigest)
        Thread {
            try {
                messageClient.sendOneWay(socketAddress, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        val messageFuture = messageClient
                .sendAsync(socketAddress, message)
        messageFuture.await()
        if (messageFuture.isSuccess) {
            println("发送成功" + Thread.currentThread().name)
        } else {
            println("发送失败")
            messageFuture.cause()?.printStackTrace()
        }
        Thread.sleep(1000000)
    }

    @Test
    fun name() {
        val string2SocketAddress = NetUtils.string2SocketAddress("12:80") as InetSocketAddress
        println(string2SocketAddress.isUnresolved)
    }
}