/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
