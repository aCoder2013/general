package com.song.general.gossip.utils

import com.google.gson.GsonBuilder

/**
 * Created by song on 2017/9/3.
 */
object JsonUtils {

    /**
     * Thread safe gson instance.
     */
    val gson = GsonBuilder()
            .create()

    fun toJson(obj: Any): String = gson.toJson(obj)

    inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)
}
