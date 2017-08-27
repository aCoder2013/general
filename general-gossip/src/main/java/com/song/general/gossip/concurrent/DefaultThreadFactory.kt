package com.song.general.gossip.concurrent

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by song on 2017/8/20.
 */
class DefaultThreadFactory(private val name: String) : ThreadFactory {

    private val threadIndex = AtomicInteger()

    override fun newThread(r: Runnable): Thread {
        return Thread(r,
                String.format("%s-%d", name, this.threadIndex.incrementAndGet()))
    }
}
