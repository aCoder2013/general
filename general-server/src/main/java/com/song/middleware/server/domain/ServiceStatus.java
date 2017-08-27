package com.song.middleware.server.domain;

/**
 * Created by song on 2017/8/6.
 */
public enum ServiceStatus {

    UP(1, "up"),
    DOWN(2, "down"),
    OFFLINE(3,"offline");
    private int code;
    private String desc;

    ServiceStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
