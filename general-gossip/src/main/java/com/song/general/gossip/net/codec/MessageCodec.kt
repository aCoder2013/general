package com.song.general.gossip.net.codec

import com.song.general.gossip.net.codec.decoder.MessageDecoder
import com.song.general.gossip.net.codec.encoder.MessageEncoder
import io.netty.channel.CombinedChannelDuplexHandler

/**
 * Created by song on 2017/8/20.
 */
class MessageCodec : CombinedChannelDuplexHandler<MessageDecoder, MessageEncoder>(MessageDecoder(), MessageEncoder())
