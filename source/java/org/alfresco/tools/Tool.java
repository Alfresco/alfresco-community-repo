/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.tools;

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
    /*package*/ ToolContext processArgs(String[] args)
        throws ToolException
    {
        return new ToolContext();
    }
    
    /**
     * Display Tool Help
     */
    /*package*/ void displayHelp()
    {
        System.out.println("Sorry.  Help is not available.");
    }

    /**
     * Perform Tool Behaviour
     * 
     * @throws ToolException
     */
    /*package*/ abstract void execute()
        throws ToolException;

    /**
     * Get the tool name
     * 
     * @return the tool name
     */
    /*package*/ abstract String getToolName();

    /**
     * Get the Application Context
     * 
     * @return  the application context
     */
    /*package*/ final ApplicationContext getApplicationContext()
    {
        return appContext;
    }
    
    /**
     * Get the Repository Service Registry
     * 
     * @return the service registry
     */    
    /*package*/ final ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }
    
    /**
     * Log  message
     * 
     * @param msg  message to log
     */
    /*package*/ final void log(String msg)
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
    /*package*/ final void logVerbose(String msg)
    {
        if (toolContext.isVerbose())
        {
            log(msg);
        }
    }
    
    /**
     * Tool entry point
     * 
     * @param args  the tool arguments
     */
    /*package*/ final void start(String[] args)
    {
        try
        {
            // Process tool arguments
            toolContext = processArgs(args);
            toolContext.validate();
    
            try
            {
                if (toolContext.isHelp())
                {
                    // Display help, if requested
                    displayHelp();
                }
                else
                {
                    // Perform Tool behaviour
                    log(getToolName());
                    initialiseRepository();
                    login();
                    execute();
                    log(getToolName() + " successfully completed.");
                }
                System.exit(0);
            }
            catch (ToolException e)
            {
                displayError(e);
                System.exit(-1);
            }
        }
        catch(ToolException e)
        {
            System.out.println(e.getMessage());
            System.out.println();
            displayHelp();
            System.exit(-1);
        }
        catch (Throwable e)
        {
            System.out.println("The following error has occurred:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Login to Repository
     */
    private void login()
    {
        // TODO: Replace with call to ServiceRegistry
        AuthenticationService auth = (AuthenticationService)appContext.getBean("authenticationService");
        auth.authenticate(toolContext.getUsername(), toolContext.getPassword().toCharArray());
        log("Connected as " + toolContext.getUsername());
    }
    
    /**
     * Initialise the Repository
     */
    private void initialiseRepository()
    {
        appContext = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = (ServiceRegistry) appContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
    }
    
    /**
     * Display Error Message
     * 
     * @param e  exception
     */
    private void displayError(Throwable e)
    {
        System.out.println(e.getMessage());
        if (toolContext != null && toolContext.isVerbose())
        {
            e.printStackTrace();
        }
    }    

}
