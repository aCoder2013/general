package com.song.middleware.server.web.support

import java.io.Serializable

/**
 * Created by song on 2017/8/5.
 */
class ApiResult<T> : Serializable {

    var isSuccess: Boolean = false

    var message: String? = null

    var data: T? = null

    constructor() {}

    constructor(success: Boolean) {
        this.isSuccess = success
    }

    companion object {

        fun <T> create(): ApiResult<T> {
            return create(false)
        }

        fun <T> create(success: Boolean): ApiResult<T> {
            return ApiResult(success)
        }
    }
}
