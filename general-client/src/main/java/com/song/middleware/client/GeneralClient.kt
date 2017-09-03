package com.song.middleware.client

import com.alibaba.fastjson.JSONObject
import com.song.middleware.client.util.CommonUtils
import okhttp3.FormBody
import okhttp3.FormBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by song on 2017/8/6.
 */
class GeneralClient {

    /**
     * 是否使用https
     */
    var isSecure: Boolean = false

    var name: String? = null

    var description: String? = null

    var ip: String? = null

    var port: String? = null

    /**
     * 服务器列表
     */
    var servers: List<String>? = null

    fun init() {
        executeOnServers(object : CallbackOnServer {
            override fun execute(server: String): Boolean {
                val registerUrl = if (this@GeneralClient.isSecure)
                    CommonUtils.getSecureRegisterApi(server)
                else
                    CommonUtils.getRegisterApi(server)
                val body = Builder()
                        .add("name", name)
                        .add("description", StringUtils.defaultString(description, ""))
                        .add("ip", ip)
                        .add("port", port)
                        .build()
                val request = Request.Builder()
                        .url(registerUrl)
                        .post(body)
                        .build()
                try {
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        return true
                    }
                } catch (e: IOException) {
                    logger.error("register to server $server failed", e)
                }

                return false
            }
        })
    }

    fun shutdown() {
        executeOnServers(object : CallbackOnServer {
            override fun execute(server: String): Boolean {
                val unregisterUrl = if (this@GeneralClient.isSecure)
                    CommonUtils.getSecureUnregisterApi(server)
                else
                    CommonUtils.getUnregisterApi(server)
                val body = Builder()
                        .add("name", name)
                        .add("ip", ip)
                        .add("port", port)
                        .build()
                val request = Request.Builder()
                        .url(unregisterUrl)
                        .post(body)
                        .build()
                try {
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        return true
                    }
                } catch (e: IOException) {
                    logger.error("unregister to server $server failed", e)
                }

                return false
            }
        })
    }

    private fun executeOnServers(callbackOnServer: CallbackOnServer) {
        if (servers == null || servers!!.isEmpty()) {
            throw IllegalArgumentException("General servers can't be null or empty.")
        }
        var success = false
        for (server in servers!!) {
            success = callbackOnServer.execute(server)
        }
        if (!success) {
            throw RuntimeException("All the servers are down, please check!")
        }
    }

    internal interface CallbackOnServer {

        fun execute(server: String): Boolean
    }

    fun getServices(serviceName: String): List<String> {
        val services = ArrayList<String>()
        executeOnServers(object : CallbackOnServer {
            override fun execute(server: String): Boolean {
                val request = Request.Builder()
                        .url(
                                if (this@GeneralClient.isSecure)
                                    CommonUtils.getSecureQueryServiceApi(server)
                                else
                                    CommonUtils.getQueryServiceApi(server))
                        .post(FormBody.Builder().add("name", serviceName).build())
                        .build()
                try {
                    val execute = okHttpClient.newCall(request).execute()
                    if (execute.isSuccessful) {
                        val body = execute.body()
                        if (body != null) {
                            val string = body.string()
                            val jsonObject = JSONObject.parseObject(string)
                            if (jsonObject.getBoolean("success")) {
                                val lists = jsonObject.getJSONArray("data")
                                val size = lists.size
                                for (i in 0..size - 1) {
                                    val serviceInfo = lists.getJSONObject(i)
                                    val ip = serviceInfo.getString("ip")
                                    val port = serviceInfo.getString("port")
                                    services.add("$ip:$port")
                                }
                                return true
                            }
                        }
                    }
                } catch (e: IOException) {
                    logger.error("query instances of " + serviceName + "failed", e)
                }

                return false
            }
        })
        return services
    }

    companion object {

        private val logger = LoggerFactory.getLogger(GeneralClient::class.java)

        private val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build()
    }

}
