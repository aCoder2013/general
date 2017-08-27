package com.song.middleware.server.storage;

import com.song.middleware.server.domain.ServiceDO;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by song on 2017/8/5.
 */
public interface ServiceStorageComponent {

    ServiceDO syncQuery(String name, String ip, String port);

    List<ServiceDO> syncQuery(String name);

    Future<List<ServiceDO>> asyncQuery(String name);

    void syncRegister(ServiceDO serviceDO);

    Future<?> asyncRegister(ServiceDO serviceDO);

    void syncUnregister(String name, String ip, String port);
}
