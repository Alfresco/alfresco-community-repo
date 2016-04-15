package org.alfresco.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco port-related utility functions.
 * 
 * @author abalmus
 */
public class PortUtil
{
    private static Log logger = LogFactory.getLog(PortUtil.class);
    
    /**
     * Check if specified port is free.
     * @param port Port number to check.
     * @param host A local address to bind to; if null, "" or "0.0.0.0" then all local addresses will be considered.
     */
    public static void checkPort(int port, String host) throws IOException
    {
        ServerSocket serverSocket = null;
        
        try
        {
            if (host != null && !host.equals("") && !"0.0.0.0".equals(host.trim()))
            {
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host.trim()));
            }
            else
            {
                serverSocket = new ServerSocket(port);
            }
        }
        finally
        {
            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch (IOException ioe)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(ioe.toString());
                    }
                }
            }
        }
    }
}
