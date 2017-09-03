package com.song.middleware.server.domain

import java.io.Serializable

/**
 * Created by song on 2017/8/5.
 */
class ServiceDO : Serializable {

    var id: Long = 0

    var name: String? = null

    var description: String? = null

    var ip: String? = null

    var port: String? = null

    /**
     * 服务当前所处的状态，默认为down
     */
    var status = ServiceStatus.DOWN.code

    /**
     * 健康检查链接
     */
    var healthCheckUrl: String? = null

    /**
     * 健康检查间隔
     * 单位是秒
     */
    var healthCheckInterval: Long = 0

    companion object {

        private const val serialVersionUID = 7208074479190445962L
    }
}
