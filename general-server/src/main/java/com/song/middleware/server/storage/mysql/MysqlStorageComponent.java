package com.song.middleware.server.storage.mysql;

import com.song.middleware.server.domain.ServiceDO;
import com.song.middleware.server.domain.ServiceStatus;
import com.song.middleware.server.storage.ServiceStorageComponent;
import com.song.middleware.server.storage.exception.StorageException;
import com.song.middleware.server.storage.mysql.mapper.ServiceMapper;
import com.song.middleware.server.util.HttpUtils;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by song on 2017/8/5.
 */
@Slf4j
@Component
public class MysqlStorageComponent implements ServiceStorageComponent {

    private static final int DEFAULT_CAPACITY = 5000;

    private final ServiceMapper serviceMapper;

    private ExecutorService executor;

    private ScheduledExecutorService healthCheckExecutor;

    @Autowired
    public MysqlStorageComponent(ServiceMapper serviceMapper) {
        this.serviceMapper = serviceMapper;
    }

    @PostConstruct
    public void init() {
        int processorNum = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(processorNum, processorNum, 0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(DEFAULT_CAPACITY), (r, executor) -> {
            executor.submit(r);
            throw new StorageException("The storage thread pool queue is full, plz check.");
        });
        this.healthCheckExecutor = Executors.newScheduledThreadPool(processorNum);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    List<Runnable> runnables = executor.shutdownNow();
                    log.warn(
                        "Storage executor was abruptly shut down. {} running tasks will be interrupted.",
                        runnables.size());
                }
                healthCheckExecutor.shutdown();
            } catch (InterruptedException ignore) {
                //we're shutting down anyway.
            }
        }));
    }

    @Override
    public ServiceDO syncQuery(String name, String ip, String port) {
        try {
            return serviceMapper.findOne(name, ip, port);
        } catch (StorageException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<ServiceDO> syncQuery(String name) {
        try {
            return serviceMapper.findAllByName(name);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public Future<List<ServiceDO>> asyncQuery(final String name) {
        return executor.submit(() -> serviceMapper.findAllByName(name));
    }

    @Override
    public void syncRegister(ServiceDO serviceDO) {
        try {
            ServiceDO serviceInDB = serviceMapper
                .findOne(serviceDO.getName(), serviceDO.getIp(), serviceDO.getPort());
            if (serviceInDB == null) {
                /*
                    Even if there is an race condition, mysql will complain about it.
                 */
                serviceDO.setStatus(ServiceStatus.UP.getCode());
                serviceMapper.save(serviceDO);
            } else if (serviceDO.getStatus() == ServiceStatus.DOWN.getCode()) {
                serviceMapper
                    .updateStatus(serviceInDB.getId(), ServiceStatus.DOWN.getCode());
            }
            this.healthCheckExecutor.scheduleAtFixedRate(() -> {
                ServiceDO serviceInstance = serviceMapper
                    .findOne(serviceDO.getName(), serviceDO.getIp(), serviceDO.getPort());
                boolean checkSuccess = false;
                try {
                    Response execute = HttpUtils.getInstance()
                        .newCall(
                            new Builder().url(serviceInstance.getHealthCheckUrl()).build())
                        .execute();
                    if (execute.isSuccessful()) {
                        checkSuccess = true;
                    }
                } catch (IOException e) {
                    log.error("health check failed", e);
                }
                if (!checkSuccess) {
                    serviceMapper
                        .updateStatus(serviceInstance.getId(), ServiceStatus.DOWN.getCode());
                }
            }, 60, serviceDO.getHealthCheckInterval(), TimeUnit.SECONDS);
            log.info("Discover a new instance {}/{}:{}", serviceDO.getName(), serviceDO.getId(),
                serviceDO.getIp());
        } catch (Exception e) {
            throw new StorageException("Save service into storage failed_" + e.getMessage(), e);
        }
    }

    @Override
    public Future<?> asyncRegister(ServiceDO serviceDO) {
        return executor.submit(() -> syncRegister(serviceDO));
    }

    @Override
    public void syncUnregister(String name, String ip, String port) {
        try {
            ServiceDO serviceDO = serviceMapper.findOne(name, ip, port);
            if (serviceDO != null) {
                serviceMapper.updateStatus(serviceDO.getId(), ServiceStatus.OFFLINE.getCode());
            }
        }catch (Exception e){
            throw new StorageException(e);
        }
    }

}
