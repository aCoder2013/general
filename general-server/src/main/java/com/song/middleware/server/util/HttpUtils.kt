package com.song.middleware.server.util

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by song on 2017/8/6.
 */
object HttpUtils {

    val log: Logger = LoggerFactory.getLogger(HttpUtils.javaClass)

    val instance: OkHttpClient = OkHttpClient.Builder()
            .writeTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build()

    fun doGet(url: String): String? {
        var response: String? = null
        try {
            response = instance.newCall(Request.Builder().url(url).build()).execute()
                    .body().string()
        } catch (e: IOException) {
            log.error("get from $url failed", e)
        }
        return response
    }

}
