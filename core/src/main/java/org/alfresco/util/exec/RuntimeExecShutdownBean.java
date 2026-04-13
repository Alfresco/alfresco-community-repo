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
package org.alfresco.util.exec;

import java.util.Collections;
import java.util.List;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * This bean executes a list of shutdown commands when either the VM shuts down
 * or the application context closes.  In both cases, the commands are only
 * executed if the application context was started.
 * 
 * @author Derek Hulley
 */
public class RuntimeExecShutdownBean extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(RuntimeExecShutdownBean.class);
    
    /** the commands to execute on context closure or VM shutdown */
    private List<RuntimeExec> shutdownCommands;
    /** the registered shutdown hook */
    private Thread shutdownHook;
    /** ensures that commands don't get executed twice */
    private boolean executed;

    /**
     * Initializes the bean with empty defaults, i.e. it will do nothing
     */
    public RuntimeExecShutdownBean()
    {
        this.shutdownCommands = Collections.emptyList();
        this.executed = false;
    }

    /**
     * Set the commands to execute, in sequence, when the application context
     * is initialized.
     * 
     * @param startupCommands list of commands
     */
    public void setShutdownCommands(List<RuntimeExec> startupCommands)
    {
        this.shutdownCommands = startupCommands;
    }

    private synchronized void execute()
    {
        // have we already done this?
        if (executed)
        {
            return;
        }
        executed = true;
        for (RuntimeExec command : shutdownCommands)
        {
            ExecutionResult result = command.execute();
            // check for failure
            if (!result.getSuccess())
            {
                logger.error("Shutdown command execution failed.  Continuing with other commands.: \n" + result);
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Executed shutdown commands");
        }
    }
    
    /**
     * The thread that will call the shutdown commands.
     * 
     * @author Derek Hulley
     */
    private class ShutdownThread extends Thread
    {
        private ShutdownThread()
        {
            super(RuntimeExecShutdownBean.class.getName());
            this.setDaemon(true);
        }

        @Override
        public void run()
        {
            execute();
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // register shutdown hook
        shutdownHook = new ShutdownThread();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        if (logger.isDebugEnabled())
        {
            logger.debug("Registered shutdown hook");
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // remove shutdown hook and execute
        if (shutdownHook != null)
        {
            // execute
            execute();
            // remove hook
            try
            {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            catch (IllegalStateException e)
            {
                // VM is already shutting down
            }
            shutdownHook = null;
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Deregistered shutdown hook");
            }
        }
    }
    
}














