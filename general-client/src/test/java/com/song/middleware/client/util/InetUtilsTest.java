package com.song.middleware.client.util;

import java.net.InetAddress;
import org.junit.Test;

/**
 * Created by song on 2017/8/6.
 */
public class InetUtilsTest {

    @Test
    public void findFirstNonLoopbackAddress() throws Exception {
        InetAddress address = InetUtils.findFirstNonLoopbackAddress();
        System.out.println(address.getHostAddress());
        System.out.println(address.getHostName());
    }

}