package com.song.general.gossip.net.codec.decoder

import com.song.general.gossip.net.Message
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

/**
 * Created by song on 2017/8/20.
 */
class MessageDecoder : MessageToMessageDecoder<Message<*>>() {

    @Throws(Exception::class)
    override fun decode(channelHandlerContext: ChannelHandlerContext, message: Message<*>,
                        list: List<Any>) {

    }
}
