package com.song.middleware.server.domain;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by song on 2017/8/5.
 */
@Getter
@Setter
public class ServiceDO implements Serializable {

    private static final long serialVersionUID = 7208074479190445962L;

    private long id;

    private String name;

    private String description;

    private String ip;

    private String port;

    /**
     * 服务当前所处的状态，默认为down
     */
    private int status = ServiceStatus.DOWN.getCode();

    /**
     * 健康检查链接
     */
    private String healthCheckUrl;

    /**
     * 健康检查间隔
     * 单位是秒
     */
    private long healthCheckInterval;
}
