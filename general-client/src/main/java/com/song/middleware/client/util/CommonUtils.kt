package com.song.middleware.client.util

/**
 * Created by song on 2017/8/6.
 */
object CommonUtils {

    private val REGISTER_API = "/api/v1/service/register"
    private val UNREGISTER_API = "/api/v1/service/unregister"

    private val QUERY_SERVICE_API = "/api/v1/service/query"
    val HTTP_PROTOCOL = "http://"
    val HTTPS_PROTOCOL = "https://"

    fun getRegisterApi(hostAndPort: String): String {
        return HTTP_PROTOCOL + hostAndPort + REGISTER_API
    }

    fun getSecureRegisterApi(hostAndPort: String): String {
        return HTTPS_PROTOCOL + hostAndPort + REGISTER_API
    }

    fun getUnregisterApi(hostAndPort: String): String {
        return HTTP_PROTOCOL + hostAndPort + UNREGISTER_API
    }

    fun getSecureUnregisterApi(hostAndPort: String): String {
        return HTTPS_PROTOCOL + hostAndPort + UNREGISTER_API
    }

    fun getQueryServiceApi(hostAndPort: String): String {
        return HTTP_PROTOCOL + hostAndPort + QUERY_SERVICE_API
    }

    fun getSecureQueryServiceApi(hostAndPort: String): String {
        return HTTPS_PROTOCOL + hostAndPort + QUERY_SERVICE_API
    }
}
