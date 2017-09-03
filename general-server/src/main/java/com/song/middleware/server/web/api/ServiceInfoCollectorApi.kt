package com.song.middleware.server.web.api

import com.song.middleware.server.domain.ServiceDO
import com.song.middleware.server.storage.ServiceStorageComponent
import com.song.middleware.server.storage.exception.StorageException
import com.song.middleware.server.web.support.ApiResult
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by song on 2017/8/5.
 */
@RestController
@RequestMapping(value = "/api/v1/service")
class ServiceInfoCollectorApi @Autowired
constructor(private val storageComponent: ServiceStorageComponent) {

    @PostMapping(value = "/query")
    fun query(
            @RequestParam(value = "name", required = false) name: String): ApiResult<List<ServiceDO>> {
        val apiResult = ApiResult.create<List<ServiceDO>>()
        if (StringUtils.isEmpty(name)) {
            apiResult.data = emptyList<ServiceDO>()
        }
        try {
            val serviceDOS = storageComponent.syncQuery(name)
            if (serviceDOS != null) {
                apiResult.isSuccess = true
                apiResult.data = serviceDOS
            }
        } catch (e: Exception) {
            apiResult.message = "query failed, " + e.message
            log.error("query failed_" + e.message, e)
        }

        return apiResult
    }

    @PostMapping(value = "/register")
    fun register(
            @RequestParam(value = "name", required = false) name: String,
            @RequestParam(value = "description", required = false) description: String,
            @RequestParam(value = "ip", required = false) ip: String,
            @RequestParam(value = "port", required = false) port: String,
            @RequestParam(value = "healthCheckUrl", required = false) healthCheckUrl: String): ApiResult<ServiceDO> {
        var name = name
        val apiResult = ApiResult.create<ServiceDO>()
        if (StringUtils.isEmpty(name)) {
            name = DEFAULT_SERVICE_NAME
        }
        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(port)) {
            apiResult.message = "Either ip or port is empty,please check!"
            return apiResult
        }
        val serviceDO = ServiceDO()
        serviceDO.name = name
        serviceDO.description = description
        serviceDO.ip = ip
        serviceDO.port = port
        serviceDO.healthCheckUrl = healthCheckUrl
        try {
            storageComponent.syncRegister(serviceDO)
            apiResult.isSuccess = true
        } catch (e: Exception) {
            apiResult.message = "save into storage failed ,please check the server log,detail:" + e.message
            log.error("Save service info failed_" + e.message, e)
        }

        return apiResult
    }


    @PostMapping(value = "/unregister")
    fun unregister(
            @RequestParam(value = "name", required = false) name: String,
            @RequestParam(value = "ip", required = false) ip: String,
            @RequestParam(value = "port", required = false) port: String): ApiResult<Void> {
        val apiResult = ApiResult.create<Void>()
        if (StringUtils.isNotBlank(name)
                || StringUtils.isBlank(ip)
                || StringUtils.isBlank(port)) {
            apiResult.message = "Either name、ip or port is empty,please check."
            return apiResult
        }
        try {
            storageComponent.syncUnregister(name, ip, port)
            apiResult.isSuccess = true
        } catch (e: StorageException) {
            log.error(name + "/" + ip + ":" + port + " unregister failed_" + e.message, e)
        }

        return apiResult
    }

    companion object {

        private val log = LoggerFactory.getLogger(ServiceInfoCollectorApi.javaClass)

        private val DEFAULT_SERVICE_NAME = "unknown"
    }

}