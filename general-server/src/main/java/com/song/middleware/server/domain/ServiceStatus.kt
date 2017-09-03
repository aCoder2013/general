package com.song.middleware.server.domain

/**
 * Created by song on 2017/8/6.
 */
enum class ServiceStatus private constructor(val code: Int, val desc: String) {

    UP(1, "up"),
    DOWN(2, "down"),
    OFFLINE(3, "offline")
}
