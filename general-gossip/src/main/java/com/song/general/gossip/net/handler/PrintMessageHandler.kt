package com.song.general.gossip.net.handler

import com.song.general.gossip.MessageHandler
import com.song.general.gossip.net.Message
import org.slf4j.LoggerFactory

/**
 * Created by song on 2017/8/27.
 */
class PrintMessageHandler : MessageHandler {

    override fun <T> handleMessage(message: Message<T>) {
        logger.info("Process message : $message")
    }

    companion object {
        private val logger = LoggerFactory
                .getLogger(PrintMessageHandler::class.java)
    }
}

