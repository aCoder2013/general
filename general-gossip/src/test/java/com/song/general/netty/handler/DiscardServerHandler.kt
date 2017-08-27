package com.song.general.netty.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.CharsetUtil
import io.netty.util.ReferenceCountUtil

/**
 * Created by song on 2017/8/19.
 */
class DiscardServerHandler : ChannelInboundHandlerAdapter() {

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val byteBuf = msg as ByteBuf
        try {
            val name = byteBuf.toString(CharsetUtil.UTF_8)
            ctx.writeAndFlush(byteBuf)
        } finally {
            ReferenceCountUtil.release(msg)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
