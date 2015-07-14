/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import java.io.IOException;
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
     * @return true if port is free or false if it's already in use.
     */
    public static boolean isPortFree(int port)
    {
        boolean isFree = true;
        ServerSocket serverSocket = null;
        
        try
        {
           serverSocket = new ServerSocket(port);
        }
        catch (IOException ioe)
        {
            isFree = false;
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
        
        return isFree;
    }
}
