package com.song.general.gossip.net

import com.song.general.gossip.GossipAction
import com.song.general.gossip.GossipDigest
import com.song.general.gossip.net.support.DefaultMessageClient
import com.song.general.gossip.net.support.DefaultMessageServer
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

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
        //start server
        val messageServer = DefaultMessageServer()
        messageServer.start()
        //start client
        val messageClient = DefaultMessageClient()
        messageClient.start()
        val gossipDigest = GossipDigest(InetAddress.getLocalHost(), 1)
        val message = Message(1, GossipAction.GOSSIP_ACK, gossipDigest,
                System.currentTimeMillis())
        Thread {
            try {
                messageClient.sendOneWay("127.0.0.1:7005", message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        val messageFuture = messageClient
                .sendAsync("127.0.0.1:7005", message)
        messageFuture.await()
        if (messageFuture.isSuccess) {
            println("发送成功" + Thread.currentThread().name)
        } else {
            println("发送失败")
            messageFuture.cause()!!.printStackTrace()
        }
        Thread.sleep(1000000)
    }
}