package com.song.middleware.server.storage

import com.song.middleware.server.domain.ServiceDO
import java.util.concurrent.Future

/**
 * Created by song on 2017/8/5.
 */
interface ServiceStorageComponent {

    fun syncQuery(name: String, ip: String, port: String): ServiceDO

    fun syncQuery(name: String): List<ServiceDO>

    fun asyncQuery(name: String): Future<List<ServiceDO>>

    fun syncRegister(serviceDO: ServiceDO)

    fun asyncRegister(serviceDO: ServiceDO): Future<*>

    fun syncUnregister(name: String, ip: String, port: String)
}
