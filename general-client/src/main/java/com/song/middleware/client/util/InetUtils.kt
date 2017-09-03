package com.song.middleware.client.util

import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException

/**
 * Created by song on 2017/8/6.
 */
object InetUtils {

    private val logger = LoggerFactory.getLogger(InetUtils::class.java)

    fun findFirstNonLoopbackAddress(): InetAddress? {
        var result: InetAddress? = null

        try {
            var lowest = 2147483647
            val nics = NetworkInterface.getNetworkInterfaces()

            out@ while (true) {
                var ifc: NetworkInterface
                while (true) {
                    do {
                        if (!nics.hasMoreElements()) {
                            break@out
                        }

                        ifc = nics.nextElement() as NetworkInterface
                    } while (!ifc.isUp)

                    if (ifc.index >= lowest && result != null) {
                        continue
                    }

                    lowest = ifc.index
                    break
                }

                val addrs = ifc.inetAddresses

                while (addrs.hasMoreElements()) {
                    val address = addrs.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress()) {
                        logger.trace("Found non-loopback interface: " + ifc.displayName)
                        result = address
                    }
                }
            }
        } catch (e: IOException) {
            logger.error("Cannot get first non-loopback address", e)
        }

        if (result != null) {
            return result
        } else {
            try {
                return InetAddress.getLocalHost()
            } catch (e: UnknownHostException) {
                logger.warn("Unable to retrieve localhost")
                return null
            }

        }
    }

}
