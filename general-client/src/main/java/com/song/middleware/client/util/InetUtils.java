package com.song.middleware.client.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by song on 2017/8/6.
 */
public class InetUtils {

    private static final Logger logger = LoggerFactory.getLogger(InetUtils.class);

    public static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = 2147483647;
            Enumeration nics = NetworkInterface.getNetworkInterfaces();

            out:
            while (true) {
                NetworkInterface ifc;
                while (true) {
                    do {
                        if (!nics.hasMoreElements()) {
                            break out;
                        }

                        ifc = (NetworkInterface) nics.nextElement();
                    } while (!ifc.isUp());

                    if (ifc.getIndex() >= lowest && result != null) {
                        continue;
                    }

                    lowest = ifc.getIndex();
                    break;
                }

                Enumeration addrs = ifc.getInetAddresses();

                while (addrs.hasMoreElements()) {
                    InetAddress address = (InetAddress) addrs.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        logger.trace("Found non-loopback interface: " + ifc.getDisplayName());
                        result = address;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Cannot get first non-loopback address", e);
        }

        if (result != null) {
            return result;
        } else {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                logger.warn("Unable to retrieve localhost");
                return null;
            }
        }
    }

}
