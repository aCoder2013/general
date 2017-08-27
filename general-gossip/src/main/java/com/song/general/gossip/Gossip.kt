package com.song.general.gossip

import com.song.general.gossip.net.MessageClient
import org.slf4j.LoggerFactory
import java.util.concurrent.ScheduledExecutorService

/**
 * Created by song on 2017/8/13.
 */
class Gossip {

    private val scheduledGossipTask: ScheduledExecutorService? = null

    private val messageClient: MessageClient? = null

    fun start() {

    }


    inner class GossipTask : Runnable {

        override fun run() {

        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Gossip::class.java)
    }

}
