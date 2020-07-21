package org.alfresco.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtils
{
    /**
     * Returns the "real" IP address represented by ipAddress. If ipAddress is a loopback
     * address it is converted into the host's underlying IP address
     * 
     * @param ipAddress String
     * @return String
     * @throws UnknownHostException
     */
    public static String getRealIPAddress(String ipAddress) throws UnknownHostException
    {
        if(ipAddress.equals("localhost") || ipAddress.equals("127.0.0.1"))
        {
            // make sure we are using a "real" IP address
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        }

        return ipAddress;
    }
}
