package com.song.general.gossip.utils

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by song on 2017/9/2.
 */
object GlobalVersionGenerator {

    private val VERSION = AtomicInteger()

    fun nextVersion(): Int = VERSION.incrementAndGet()

}