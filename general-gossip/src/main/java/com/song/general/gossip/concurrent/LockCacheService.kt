package com.song.general.gossip.concurrent

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import io.netty.util.internal.StringUtil
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by song on 2017/8/20.
 */
object LockCacheService {

    private val DEFAULT_LOCK_EXPIRE_MINUTE = 3

    private val NULL_LOCK = ReentrantLock()

    /**
     * key of the map , same key will share the same lock instance.
     */
    private val LOCK_LOADING_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(DEFAULT_LOCK_EXPIRE_MINUTE.toLong(), TimeUnit.MINUTES)
            .build(object : CacheLoader<String, Lock>() {
                @Throws(Exception::class)
                override fun load(s: String): Lock {
                    return ReentrantLock()
                }
            })

    fun getLock(key: String): Lock {
        if (StringUtil.isNullOrEmpty(key)) {
            return NULL_LOCK
        }
        try {
            return LOCK_LOADING_CACHE.get(key)
        } catch (e: ExecutionException) {
            // This really shouldn't happen, but ...
            e.printStackTrace()
        }

        return NULL_LOCK
    }
}
