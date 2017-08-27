package com.song.general.gossip.net.support

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by song on 2017/8/26.
 */
class MessageFuture<T> {

    var isSuccess: Boolean = false
        set(success) {
            field = success
            this.countDownLatch.countDown()
        }

    var data: T? = null

    var cause: Throwable? = null

    private val countDownLatch = CountDownLatch(1)

    @Throws(InterruptedException::class)
    fun await() {
        this.countDownLatch.await()
    }

    @Throws(InterruptedException::class)
    fun await(timeout: Long, timeUnit: TimeUnit) {
        this.countDownLatch.await(timeout, timeUnit)
    }

    fun cause(cause: Throwable) {
        this.cause = cause
    }

    fun cause(): Throwable? {
        return this.cause
    }
}
