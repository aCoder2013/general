package com.song.middleware.server.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by song on 2017/8/6.
 */
@Slf4j
public class HttpUtils {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
        .writeTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .connectTimeout(3, TimeUnit.SECONDS)
        .build();

    public static OkHttpClient getInstance() {
        return OK_HTTP_CLIENT;
    }

    public static String doGet(String url) {
        String response = null;
        try {
            response = OK_HTTP_CLIENT.newCall(new Request.Builder().url(url).build()).execute()
                .body().string();
        } catch (IOException e) {
            log.error("get from " + url + " failed", e);
        }
        return response;
    }
}
