package com.song.general.gossip.net

import com.song.general.gossip.net.support.MessageFuture

/**
 * Created by song on 2017/8/20.
 */
interface MessageClient : LifeCycle {

    /**
     * 发送消息，而不管是否成功

     * @param address 发向的地址,格式为 host:port
     * *
     * @param message 通用消息体
     * *
     * @param <T> 消息体具体具体承载了哪种类型
    </T> */
    @Throws(Exception::class)
    fun <T> sendOneWay(address: String, message: Message<T>)

    /**
     * 异步发送消息
     */
    fun <T> sendAsync(address: String, message: Message<T>): MessageFuture<T>

}
