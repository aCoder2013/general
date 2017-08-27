package com.song.middleware.client.util;

/**
 * Created by song on 2017/8/6.
 */
public class CommonUtils {

    private static final String REGISTER_API = "/api/v1/service/register";
    private static final String UNREGISTER_API = "/api/v1/service/unregister";

    private static final String QUERY_SERVICE_API = "/api/v1/service/query";
    public static final String HTTP_PROTOCOL = "http://";
    public static final String HTTPS_PROTOCOL = "https://";

    public static String getRegisterApi(String hostAndPort) {
        return HTTP_PROTOCOL + hostAndPort + REGISTER_API;
    }

    public static String getSecureRegisterApi(String hostAndPort) {
        return HTTPS_PROTOCOL + hostAndPort + REGISTER_API;
    }

    public static String getUnregisterApi(String hostAndPort) {
        return HTTP_PROTOCOL + hostAndPort + UNREGISTER_API;
    }

    public static String getSecureUnregisterApi(String hostAndPort) {
        return HTTPS_PROTOCOL + hostAndPort + UNREGISTER_API;
    }

    public static String getQueryServiceApi(String hostAndPort) {
        return HTTP_PROTOCOL + hostAndPort + QUERY_SERVICE_API;
    }

    public static String getSecureQueryServiceApi(String hostAndPort) {
        return HTTPS_PROTOCOL + hostAndPort + QUERY_SERVICE_API;
    }
}
