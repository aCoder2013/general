package com.song.general.gossip.net.codec.encoder

import com.song.general.gossip.net.Message
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * Created by song on 2017/8/20.
 */
class MessageEncoder : MessageToByteEncoder<Message<*>>() {

    @Throws(Exception::class)
    override fun encode(channelHandlerContext: ChannelHandlerContext, message: Message<*>,
                        byteBuf: ByteBuf) {

    }

}
