package com.song.middleware.client.util

/**
 * Created by song on 2017/8/6.
 */
object CollectionUtils {

    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    fun isNotEmpty(collection: Collection<*>): Boolean {
        return !isEmpty(collection)
    }

}
