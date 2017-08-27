package com.song.middleware.server.web.support;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by song on 2017/8/5.
 */
@Getter
@Setter
public class ApiResult<T> implements Serializable {

    private boolean success;

    private String message;

    private T data;

    public ApiResult() {
    }

    public ApiResult(boolean success) {
        this.success = success;
    }

    public static <T> ApiResult<T> create() {
        return create(false);
    }

    public static <T> ApiResult<T> create(boolean success) {
        return new ApiResult<>(success);
    }


}
