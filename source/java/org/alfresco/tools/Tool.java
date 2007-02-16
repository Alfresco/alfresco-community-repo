/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.tools;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;


/**
 * Abstract Tool Implementation
 * 
 * @author David Caruana
 */
public abstract class Tool
{
    /** Tool Context */
    private ToolContext toolContext;
    /** Spring Application Context */
    private ApplicationContext appContext;
    /** Repository Service Registry */
    private ServiceRegistry serviceRegistry;
    
    
    /**
     * Process Tool Arguments
     * 
     * @param args  the arguments
     * @return  the tool context
     * @throws ToolException
     */
    protected ToolContext processArgs(String[] args)
        throws ToolArgumentException
    {
        return new ToolContext();
    }
    
    /**
     * Display Tool Help
     */
    protected void displayHelp()
    {
        logError("Sorry.  Help is not available.");
    }

    /**
     * Perform Tool Behaviour
     * 
     * @throws ToolException
     */
    protected abstract int execute()
        throws ToolException;

    /**
     * Get the tool name
     * 
     * @return the tool name
     */
    protected abstract String getToolName();

    /**
     * Log  message
     * 
     * @param msg  message to log
     */
    protected void logInfo(String msg)
    {
        if (toolContext.isQuiet() == false)
        {
            System.out.println(msg);
        }
    }

    /**
     * Log Verbose message
     * 
     * @param msg  message to log
     */
    protected void logVerbose(String msg)
    {
        if (toolContext.isVerbose())
        {
            logInfo(msg);
        }
    }
    
    /**
     * Log Error message
     * 
     * @param msg  message to log
     */
    protected void logError(String msg)
    {
    	System.out.println(msg);
    }
    
    /**
     * Handle Error Message
     * 
     * @param e  exception
     */
    protected int handleError(Throwable e)
    {
    	if (e instanceof ToolArgumentException)
    	{
            logError(e.getMessage());
            logError("");
            displayHelp();
    	}
    	else if (e instanceof ToolException)
    	{
    		logError(e.getMessage());
    	}
    	else
    	{
            logError("The following error has occurred:");
            logError(e.getMessage());
            if (toolContext != null && toolContext.isVerbose())
            {
            	StringWriter stringWriter = new StringWriter();
            	PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                logError(stringWriter.toString());
            }
    	}
    	
        // return generic error code
    	return -1;
    }

    /**
     * Exit Tool
     * 
     * @param status  status code
     */
    protected void exit(int status)
    {
    	System.exit(status);
    }

    /**
     * Get the Application Context
     * 
     * @return  the application context
     */
    protected final ApplicationContext getApplicationContext()
    {
        return appContext;
    }
    
    /**
     * Get the Repository Service Registry
     * 
     * @return the service registry
     */    
    protected final ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }
    
    /**
     * Tool entry point
     * 
     * @param args  the tool arguments
     */
    public final void start(String[] args)
    {
        long startTime = System.nanoTime();
    	int status = -1;
    	
        try
        {
            // Process tool arguments
            toolContext = processArgs(args);
            toolContext.validate();
    
            if (toolContext.isHelp())
            {
                // Display help, if requested
                displayHelp();
            }
            else
            {
                // Perform Tool behaviour
                logInfo(getToolName());
                initialiseRepository();
                login();
                long loginTime = System.nanoTime();
                logInfo("Time to login "+((loginTime - startTime)/1000000000f)+" seconds");
                status = execute();
                long executeTime = System.nanoTime();
                logInfo("Time to execute "+((executeTime - loginTime)/1000000000f)+" seconds");
                logInfo(getToolName() + " successfully completed.");
            }
        }
        catch (Throwable e)
        {
        	status = handleError(e);
        }
        
        exit(status);
    }

    /**
     * Login to Repository
     */
    private void login()
    {
        // TODO: Replace with call to ServiceRegistry
        AuthenticationService auth = (AuthenticationService) serviceRegistry.getAuthenticationService();
        auth.authenticate(toolContext.getUsername(), toolContext.getPassword().toCharArray());
        logInfo("Connected as " + toolContext.getUsername());
    }
    
    /**
     * Initialise the Repository
     */
    private void initialiseRepository()
    {
        appContext = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = (ServiceRegistry) appContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
    }
    
}
