package com.song.middleware.server.web.api;

import com.song.middleware.server.domain.ServiceDO;
import com.song.middleware.server.storage.ServiceStorageComponent;
import com.song.middleware.server.storage.exception.StorageException;
import com.song.middleware.server.web.support.ApiResult;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by song on 2017/8/5.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/service")
public class ServiceInfoCollectorApi {

    private static final String DEFAULT_SERVICE_NAME = "unknown";
    private final ServiceStorageComponent storageComponent;

    @Autowired
    public ServiceInfoCollectorApi(ServiceStorageComponent storageComponent) {
        this.storageComponent = storageComponent;
    }

    @PostMapping(value = "/query")
    public ApiResult<List<ServiceDO>> query(
        @RequestParam(value = "name", required = false) String name) {
        ApiResult<List<ServiceDO>> apiResult = ApiResult.create();
        if (StringUtils.isEmpty(name)) {
            apiResult.setData(Collections.emptyList());
        }
        try {
            List<ServiceDO> serviceDOS = storageComponent.syncQuery(name);
            if (serviceDOS != null) {
                apiResult.setSuccess(true);
                apiResult.setData(serviceDOS);
            }
        } catch (Exception e) {
            apiResult.setMessage("query failed, " + e.getMessage());
            log.error("query failed_" + e.getMessage(), e);
        }
        return apiResult;
    }

    @PostMapping(value = "/register")
    public ApiResult<ServiceDO> register(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "ip", required = false) String ip,
        @RequestParam(value = "port", required = false) String port,
        @RequestParam(value = "healthCheckUrl", required = false) String healthCheckUrl) {
        ApiResult<ServiceDO> apiResult = ApiResult.create();
        if (StringUtils.isEmpty(name)) {
            name = DEFAULT_SERVICE_NAME;
        }
        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(port)) {
            apiResult.setMessage("Either ip or port is empty,please check!");
            return apiResult;
        }
        ServiceDO serviceDO = new ServiceDO();
        serviceDO.setName(name);
        serviceDO.setDescription(description);
        serviceDO.setIp(ip);
        serviceDO.setPort(port);
        serviceDO.setHealthCheckUrl(healthCheckUrl);
        try {
            storageComponent.syncRegister(serviceDO);
            apiResult.setSuccess(true);
        } catch (Exception e) {
            apiResult.setMessage(
                "save into storage failed ,please check the server log,detail:" + e.getMessage());
            log.error("Save service info failed_" + e.getMessage(), e);
        }
        return apiResult;
    }


    @PostMapping(value = "/unregister")
    public ApiResult<Void> unregister(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "ip", required = false) String ip,
        @RequestParam(value = "port", required = false) String port) {
        ApiResult<Void> apiResult = ApiResult.create();
        if (StringUtils.isNotBlank(name)
            || StringUtils.isBlank(ip)
            || StringUtils.isBlank(port)) {
            apiResult.setMessage("Either name、ip or port is empty,please check.");
            return apiResult;
        }
        try {
            storageComponent.syncUnregister(name, ip, port);
            apiResult.setSuccess(true);
        } catch (StorageException e) {
            log.error(name + "/" + ip + ":" + port + " unregister failed_" + e.getMessage(), e);
        }
        return apiResult;
    }

}