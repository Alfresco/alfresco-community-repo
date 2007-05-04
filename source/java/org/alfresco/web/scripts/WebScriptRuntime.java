/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.scripts;

import java.io.IOException;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.alfresco.web.scripts.WebScriptDescription.RequiredTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Encapsulates the execution of a single Web Script.
 *
 * Provides support for logging, transactions and authentication.
 * 
 * Sub-classes of WebScriptRuntime maintain the execution environment e.g. servlet
 * request & response.
 * 
 * A new instance of WebScriptRuntime is required for each new execution environment.
 * 
 * @author davidc
 */
public abstract class WebScriptRuntime
{
    // Logger
    protected static final Log logger = LogFactory.getLog(WebScriptRuntime.class);

    /** Component Dependencies */
    private WebScriptRegistry registry;
    private TransactionService transactionService;

    /**
     * Construct
     * 
     * @param registry  web script registry
     * @param transactionService  transaction service
     */
    public WebScriptRuntime(WebScriptRegistry registry, TransactionService transactionService)
    {
        this.registry = registry;
        this.transactionService = transactionService;
    }
    
    /**
     * Execute the Web Script encapsulated by this Web Script Runtime
     */
    public void executeScript()
    {
        long startRuntime = System.currentTimeMillis();

        String method = getScriptMethod();
        String scriptUrl = getScriptUrl();
        
        try
        {
            if (logger.isDebugEnabled())
                logger.debug("Processing script url ("  + method + ") " + scriptUrl);

            WebScriptMatch match = registry.findWebScript(method, scriptUrl);
            if (match != null)
            {
                // setup web script context
                final WebScriptRequest scriptReq = createRequest(match);
                final WebScriptResponse scriptRes = createResponse();
                
                if (logger.isDebugEnabled())
                    logger.debug("Agent: " + scriptReq.getAgent());

                long startScript = System.currentTimeMillis();
                final WebScript script = match.getWebScript();
                final WebScriptDescription description = script.getDescription();
                
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        String user = AuthenticationUtil.getCurrentUserName();
                        String locale = I18NUtil.getLocale().toString();
                        logger.debug("Invoking Web Script "  + description.getId() + (user == null ? " (unauthenticated)" : " (authenticated as " + user + ")" + " (" + locale + ")"));
                    }
                    
                    if (description.getRequiredTransaction() == RequiredTransaction.none)
                    {
                        authenticatedExecute(scriptReq, scriptRes);
                    }
                    else
                    {
                        // encapsulate script within transaction
                        TransactionUtil.TransactionWork<Object> work = new TransactionUtil.TransactionWork<Object>()
                        {
                            public Object doWork() throws Throwable
                            {
                                if (logger.isDebugEnabled())
                                    logger.debug("Begin transaction: " + description.getRequiredTransaction());
                                
                                authenticatedExecute(scriptReq, scriptRes);
                                
                                if (logger.isDebugEnabled())
                                    logger.debug("End transaction: " + description.getRequiredTransaction());
                                
                                return null;
                            }        
                        };
                    
                        if (description.getRequiredTransaction() == RequiredTransaction.required)
                        {
                            TransactionUtil.executeInUserTransaction(transactionService, work); 
                        }
                        else
                        {
                            TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, work); 
                        }
                    }
                }
                catch(IOException e)
                {
                    throw new WebScriptException("Failed to execute script", e);
                }
                finally
                {
                    if (logger.isDebugEnabled())
                    {
                        long endScript = System.currentTimeMillis();
                        logger.debug("Web Script " + description.getId() + " executed in " + (endScript - startScript) + "ms");
                    }
                }
            }
            else
            {
                String msg = "Script url (" + method + ") " + scriptUrl + " does not map to a Web Script.";
                if (logger.isDebugEnabled())
                    logger.debug(msg);

                throw new WebScriptException(msg);
            }
        }
        finally
        {
            long endRuntime = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                logger.debug("Processed script url ("  + method + ") " + scriptUrl + " in " + (endRuntime - startRuntime) + "ms");
        }
    }
    
    /**
     * Execute script whilst authenticated
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     * @throws IOException
     */
    protected void authenticatedExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes)
        throws IOException
    {
        WebScript script = scriptReq.getServiceMatch().getWebScript();
        WebScriptDescription desc = script.getDescription();
        RequiredAuthentication required = desc.getRequiredAuthentication();
        boolean isGuest = scriptReq.isGuest();
        
        if (required == RequiredAuthentication.none)
        {
            wrappedExecute(scriptReq, scriptRes);
        }
        else if (required == RequiredAuthentication.user && isGuest)
        {
            throw new WebScriptException("Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
        }
        else
        {
            String currentUser = null;

            try
            {
                //
                // Determine if user already authenticated
                //
                
                currentUser = AuthenticationUtil.getCurrentUserName();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Current authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                    logger.debug("Authentication required: " + required);
                    logger.debug("Guest login: " + isGuest);
                }

                //
                // Apply appropriate authentication to Web Script invocation
                //
                
                authenticate(required, isGuest);
                
                //
                // Execute Web Script
                wrappedExecute(scriptReq, scriptRes);
            }
            finally
            {
                //
                // Reset authentication for current thread
                //
                
                AuthenticationUtil.clearCurrentSecurityContext();
                if (currentUser != null)
                {
                    AuthenticationUtil.setCurrentUser(currentUser);
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("Authentication reset: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));                
            }
        }
    }
    
    /**
     * Execute Web Script with pre & post hooks
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     * @throws IOException
     */
    protected void wrappedExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes)
        throws IOException
    {
        boolean execute = preExecute(scriptReq, scriptRes);
        if (execute)
        {
            WebScript script = scriptReq.getServiceMatch().getWebScript();
            script.execute(scriptReq, scriptRes);
            postExecute(scriptReq, scriptRes);
        }
    }

    
    /**
     * Get the Web Script Method  e.g. get, post
     * 
     * @return  web script method
     */
    protected abstract String getScriptMethod();

    /**
     * Get the Web Script Url
     * 
     * @return  web script url
     */
    protected abstract String getScriptUrl();
    
    /**
     * Create a Web Script Request
     * 
     * @param match  web script matching the script method and url
     * @return  web script request
     */
    protected abstract WebScriptRequest createRequest(WebScriptMatch match);
    
    /**
     * Create a Web Script Response
     * 
     * @return  web script response
     */
    protected abstract WebScriptResponse createResponse();
    
    /**
     * Authenticate Web Script execution
     * 
     * @param required  required level of authentication
     * @param isGuest  is the request accessed as Guest
     */
    protected abstract void authenticate(RequiredAuthentication required, boolean isGuest);
    
    /**
     * Pre-execution hook
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     * @return  true => execute script, false => do not execute script
     */
    protected abstract boolean preExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes);

    /**
     * Post-execution hook
     * 
     * Note: this hook is not invoked if the script is not executed.
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     */
    protected abstract void postExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes);

}
