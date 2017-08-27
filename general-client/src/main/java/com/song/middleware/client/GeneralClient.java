package com.song.middleware.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.song.middleware.client.util.CommonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by song on 2017/8/6.
 */
public class GeneralClient {

    private static final Logger logger = LoggerFactory.getLogger(GeneralClient.class);

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build();

    /**
     * 是否使用https
     */
    private boolean secure;

    private String name;

    private String description;

    private String ip;

    private String port;

    /**
     * 服务器列表
     */
    private List<String> servers;

    public void init() {
        executeOnServers(server -> {
            String registerUrl =
                isSecure() ? CommonUtils.getSecureRegisterApi(server)
                    : CommonUtils.getRegisterApi(server);
            FormBody body = new Builder()
                .add("name", name)
                .add("description", StringUtils.defaultString(description, ""))
                .add("ip", ip)
                .add("port", port)
                .build();
            Request request = new Request.Builder()
                .url(registerUrl)
                .post(body)
                .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return true;
                }
            } catch (IOException e) {
                logger.error("register to server " + server + " failed", e);
            }
            return false;
        });
    }

    public void shutdown() {
        executeOnServers(server -> {
            String unregisterUrl =
                isSecure() ? CommonUtils.getSecureUnregisterApi(server)
                    : CommonUtils.getUnregisterApi(server);
            FormBody body = new Builder()
                .add("name", name)
                .add("ip", ip)
                .add("port", port)
                .build();
            Request request = new Request.Builder()
                .url(unregisterUrl)
                .post(body)
                .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    return true;
                }
            } catch (IOException e) {
                logger.error("unregister to server " + server + " failed", e);
            }
            return false;
        });
    }

    private void executeOnServers(CallbackOnServer callbackOnServer) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("General servers can't be null or empty.");
        }
        boolean success = false;
        for (String server : servers) {
            success = callbackOnServer.execute(server);
        }
        if (!success) {
            throw new RuntimeException("All the servers are down, please check!");
        }
    }

    interface CallbackOnServer {

        boolean execute(String server);
    }

    public List<String> getServices(String serviceName) {
        List<String> services = new ArrayList<>();
        executeOnServers(server -> {
            Request request = new Request.Builder()
                .url(isSecure() ? CommonUtils.getSecureQueryServiceApi(server)
                    : CommonUtils.getQueryServiceApi(server))
                .post(new FormBody.Builder().add("name", serviceName).build())
                .build();
            try {
                Response execute = okHttpClient.newCall(request).execute();
                if (execute.isSuccessful()) {
                    ResponseBody body = execute.body();
                    if (body != null) {
                        String string = body.string();
                        JSONObject jsonObject = JSONObject.parseObject(string);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray lists = jsonObject.getJSONArray("data");
                            int size = lists.size();
                            for (int i = 0; i < size; i++) {
                                JSONObject serviceInfo = lists.getJSONObject(i);
                                String ip = serviceInfo.getString("ip");
                                String port = serviceInfo.getString("port");
                                services.add(ip + ":" + port);
                            }
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("query instances of " + serviceName + "failed", e);
            }
            return false;
        });
        return services;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

}
