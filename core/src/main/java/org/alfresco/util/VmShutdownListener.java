/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class that keeps track of the VM shutdown status.  It can be
 * used by threads as a singleton to check if the
 * VM shutdown status has been activated.
 * <p>
 * <b>NOTE: </b> In order to prevent a proliferation of shutdown hooks,
 *      it is advisable to use instances as singletons only. 
 * <p>
 * This component should be used by long-running, but interruptable processes.
 * 
 * @author Derek Hulley
 */
public class VmShutdownListener
{
    private Log logger;
    private volatile boolean vmShuttingDown;
    
    /**
     * Constructs this instance to listen to the VM shutdown call.
     *
     */
    public VmShutdownListener(final String name)
    {
        logger = LogFactory.getLog(VmShutdownListener.class);
        
        vmShuttingDown = false;
        Runnable shutdownRunnable = new Runnable()
        {
            public void run()
            {
                vmShuttingDown = true;
                if (logger.isDebugEnabled())
                {
                    logger.debug("VM shutdown detected by listener " + name);
                }
            };  
        };
        Thread shutdownThread = new Thread(shutdownRunnable, "ShutdownListener-" + name);
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * @return Returns true if the VM shutdown signal was detected.
     */
    public boolean isVmShuttingDown()
    {
        return vmShuttingDown;
    }

    /**
     * Message carrier to break out of loops using the callback.
     * 
     * @author Derek Hulley
     * @since 3.2.1
     */
    public static class VmShutdownException extends RuntimeException
    {
        private static final long serialVersionUID = -5876107469054587072L;
    }
}
