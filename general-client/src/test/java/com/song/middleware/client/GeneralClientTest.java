package com.song.middleware.client;

import com.alibaba.fastjson.JSONObject;
import com.song.middleware.client.util.InetUtils;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;

/**
 * Created by song on 2017/8/6.
 */
public class GeneralClientTest {

    @Test
    public void register() throws Exception {
        GeneralClient generalClient = new GeneralClient();
        generalClient.setServers(new ArrayList<>(Collections.singletonList("localhost:8080")));
        generalClient.setName("general-service");
        generalClient.setIp(InetUtils.findFirstNonLoopbackAddress().getHostAddress());
        generalClient.setPort("8050");
//        generalClient.init();
        System.out.println(JSONObject.toJSON(generalClient.getServices("general-service")));
    }

}