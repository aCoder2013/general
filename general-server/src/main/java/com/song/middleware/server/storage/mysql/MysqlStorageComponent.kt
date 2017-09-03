package com.song.middleware.server.storage.mysql

import com.song.middleware.server.domain.ServiceDO
import com.song.middleware.server.domain.ServiceStatus
import com.song.middleware.server.storage.ServiceStorageComponent
import com.song.middleware.server.storage.exception.StorageException
import com.song.middleware.server.storage.mysql.mapper.ServiceMapper
import com.song.middleware.server.util.HttpUtils
import okhttp3.Request.Builder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.*
import javax.annotation.PostConstruct

/**
 * Created by song on 2017/8/5.
 */
@Component
class MysqlStorageComponent @Autowired
constructor(private val serviceMapper: ServiceMapper) : ServiceStorageComponent {

    private var executor: ExecutorService? = null

    private var healthCheckExecutor: ScheduledExecutorService? = null

    @PostConstruct
    fun init() {
        val processorNum = Runtime.getRuntime().availableProcessors()
        this.executor = ThreadPoolExecutor(processorNum, processorNum, 0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue<Runnable>(DEFAULT_CAPACITY)) { r, executor ->
            executor.submit(r)
            throw StorageException("The storage thread pool queue is full, plz check.")
        }
        this.healthCheckExecutor = Executors.newScheduledThreadPool(processorNum)
        Runtime.getRuntime().addShutdownHook(Thread {
            executor!!.shutdown()
            try {
                if (!executor!!.awaitTermination(1, TimeUnit.MINUTES)) {
                    val runnables = executor!!.shutdownNow()
                    log.warn(
                            "Storage executor was abruptly shut down. {} running tasks will be interrupted.",
                            runnables.size)
                }
                healthCheckExecutor!!.shutdown()
            } catch (ignore: InterruptedException) {
                //we're shutting down anyway.
            }
        })
    }

    override fun syncQuery(name: String, ip: String, port: String): ServiceDO {
        try {
            return serviceMapper.findOne(name, ip, port)
        } catch (e: StorageException) {
            throw StorageException(e)
        }

    }

    override fun syncQuery(name: String): List<ServiceDO> {
        try {
            return serviceMapper.findAllByName(name)
        } catch (e: Exception) {
            throw StorageException(e)
        }

    }

    override fun asyncQuery(name: String): Future<List<ServiceDO>> {
        return executor!!.submit<List<ServiceDO>> { serviceMapper.findAllByName(name) }
    }

    override fun syncRegister(serviceDO: ServiceDO) {
        try {
            val serviceInDB = serviceMapper
                    .findOne(serviceDO.name!!, serviceDO.ip!!, serviceDO.port!!)
            if (serviceDO.status == ServiceStatus.DOWN.code) {
                serviceMapper
                        .updateStatus(serviceInDB.id, ServiceStatus.DOWN.code)
            }
            this.healthCheckExecutor!!.scheduleAtFixedRate({
                val serviceInstance = serviceMapper
                        .findOne(serviceDO.name!!, serviceDO.ip!!, serviceDO.port!!)
                var checkSuccess = false
                try {
                    val execute = HttpUtils.instance
                            .newCall(
                                    Builder().url(serviceInstance.healthCheckUrl).build())
                            .execute()
                    if (execute.isSuccessful) {
                        checkSuccess = true
                    }
                } catch (e: IOException) {
                    log.error("health check failed", e)
                }

                if (!checkSuccess) {
                    serviceMapper
                            .updateStatus(serviceInstance.id, ServiceStatus.DOWN.code)
                }
            }, 60, serviceDO.healthCheckInterval, TimeUnit.SECONDS)
            log.info("Discover a new instance {}/{}:{}", serviceDO.name, serviceDO.id,
                    serviceDO.ip)
        } catch (e: Exception) {
            throw StorageException("Save service into storage failed_" + e.message, e)
        }

    }

    override fun asyncRegister(serviceDO: ServiceDO): Future<*> {
        return executor!!.submit { syncRegister(serviceDO) }
    }

    override fun syncUnregister(name: String, ip: String, port: String) {
        try {
            val serviceDO = serviceMapper.findOne(name, ip, port)
            if (serviceDO != null) {
                serviceMapper.updateStatus(serviceDO.id, ServiceStatus.OFFLINE.code)
            }
        } catch (e: Exception) {
            throw StorageException(e)
        }

    }

    companion object {

        private val log = LoggerFactory.getLogger(MysqlStorageComponent.javaClass)

        private val DEFAULT_CAPACITY = 5000
    }

}
