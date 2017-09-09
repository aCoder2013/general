package com.song.general.gossip.net.handler

import com.song.general.gossip.net.Message

/**
 * Created by song on 2017/8/19.
 */
interface MessageHandler {

    fun handleMessage(message: Message)
}
