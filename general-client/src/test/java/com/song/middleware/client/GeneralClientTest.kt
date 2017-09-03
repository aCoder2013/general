package com.song.middleware.client

import com.alibaba.fastjson.JSONObject
import com.song.middleware.client.util.InetUtils
import org.junit.Test

/**
 * Created by song on 2017/8/6.
 */
class GeneralClientTest {

    @Test
    @Throws(Exception::class)
    fun register() {
        val generalClient = GeneralClient()
        generalClient.servers = listOf("localhost:8080")
        generalClient.name = "general-service"
        generalClient.ip = InetUtils.findFirstNonLoopbackAddress()?.hostAddress
        generalClient.port = "8050"
        generalClient.init()
        println(JSONObject.toJSON(generalClient.getServices("general-service")))
    }

}