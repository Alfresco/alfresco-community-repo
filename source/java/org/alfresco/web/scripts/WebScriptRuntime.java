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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
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
    private ServiceRegistry serviceRegistry;
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthorityService authorityService;

    /**
     * Construct
     * 
     * @param registry  web script registry
     * @param serviceRegistry  service registry
     */
    public WebScriptRuntime(WebScriptRegistry registry, ServiceRegistry serviceRegistry)
    {
        this.registry = registry;
        this.serviceRegistry = serviceRegistry;
        this.authorityService = serviceRegistry.getAuthorityService();
        this.retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
    }
    
    /**
     * Execute the Web Script encapsulated by this Web Script Runtime
     */
    public void executeScript()
    {
        long startRuntime = System.currentTimeMillis();

        String method = getScriptMethod();
        String scriptUrl = null;

        try
        {
            // extract script url
            scriptUrl = getScriptUrl();
            if (scriptUrl == null || scriptUrl.length() == 0)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Script URL not specified");
            }
        
            if (logger.isDebugEnabled())
                logger.debug("Processing script url ("  + method + ") " + scriptUrl);

            WebScriptMatch match = registry.findWebScript(method, scriptUrl);
            if (match == null || match.getKind() == WebScriptMatch.Kind.URI)
            {
                if (match == null)
                {
                    String msg = "Script url " + scriptUrl + " does not map to a Web Script.";
                    if (logger.isDebugEnabled())
                        logger.debug(msg);
                    throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, msg);
                }
                else
                {
                    String msg = "Script url " + scriptUrl + " does not support the method " + method;
                    if (logger.isDebugEnabled())
                        logger.debug(msg);
                    throw new WebScriptException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
                }
            }

            // create web script request & response
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
                    String reqFormat = scriptReq.getFormat();
                    String format = (reqFormat == null || reqFormat.length() == 0) ? "default" : scriptReq.getFormat();
                    WebScriptDescription desc = scriptReq.getServiceMatch().getWebScript().getDescription();
                    logger.debug("Format style: " + desc.getFormatStyle() + ", Default format: " + desc.getDefaultFormat());
                    logger.debug("Invoking Web Script "  + description.getId() + (user == null ? " (unauthenticated)" : " (authenticated as " + user + ") (format " + format + ") (" + locale + ")"));
                }

                // execute script within required level of authentication
                authenticatedExecute(scriptReq, scriptRes);
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
        catch(Throwable e)
        {
            if (logger.isInfoEnabled())
                logger.info("Caught exception & redirecting to status template: " + e.getMessage());
            if (logger.isDebugEnabled())
            {
                StringWriter writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                logger.debug("Caught exception: " + writer.toString());
            }
            
            // extract status code, if specified
            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            if (e instanceof WebScriptException)
            {
                statusCode = ((WebScriptException)e).getStatus();
            }

            // create web script status for status template rendering
            WebScriptStatus status = new WebScriptStatus();
            status.setCode(statusCode);
            status.setMessage(e.getMessage());
            status.setException(e);
            
            // create basic model for status template rendering
            WebScriptRequest req = createRequest(null);
            WebScriptResponse res = createResponse();
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("status", status);
            model.put("url", new URLModel(req));
            model.put("server", new ServerModel(serviceRegistry.getDescriptorService().getServerDescriptor()));
            model.put("date", new Date());
            
            // locate status template
            // NOTE: search order...
            //   1) root located <status>.ftl
            //   2) root located status.ftl
            String templatePath = getStatusCodeTemplate(statusCode);
            if (!registry.getTemplateProcessor().hasTemplate(templatePath))
            {
                templatePath = getStatusTemplate();
                if (!registry.getTemplateProcessor().hasTemplate(templatePath))
                {
                    throw new WebScriptException("Failed to find status template " + templatePath + " (format: " + WebScriptResponse.HTML_FORMAT + ")");
                }
            }

            // render output
            if (logger.isDebugEnabled())
            {
                logger.debug("Force success status header in response: " + req.forceSuccessStatus());
                logger.debug("Sending status " + statusCode + " (Template: " + templatePath + ")");
                logger.debug("Rendering response: content type=" + MimetypeMap.MIMETYPE_HTML);
            }

            res.reset();
            WebScriptCache cache = new WebScriptCache();
            cache.setNeverCache(true);
            res.setCache(cache);
            res.setStatus(req.forceSuccessStatus() ? HttpServletResponse.SC_OK : statusCode);
            res.setContentType(MimetypeMap.MIMETYPE_HTML + ";charset=UTF-8");
            try
            {
                registry.getTemplateProcessor().process(templatePath, model, res.getWriter());
            }
            catch (IOException e1)
            {
                throw new WebScriptException("Internal error", e1);
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
            transactionedExecute(scriptReq, scriptRes);
        }
        else if ((required == RequiredAuthentication.user || required == RequiredAuthentication.admin) && isGuest)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
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
                if (authenticate(required, isGuest, scriptReq, scriptRes))
                {
                    if (required == RequiredAuthentication.admin && !authorityService.hasAdminAuthority())
                    {
                        throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires admin authentication; however, a non-admin has attempted access.");
                    }
                    
                    // Execute Web Script
                    transactionedExecute(scriptReq, scriptRes);
                }
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
     * Execute script within required level of transaction
     * 
     * @param scriptReq
     * @param scriptRes
     * @throws IOException
     */
    protected void transactionedExecute(final WebScriptRequest scriptReq, final WebScriptResponse scriptRes)
        throws IOException
    {
        final WebScript script = scriptReq.getServiceMatch().getWebScript();
        final WebScriptDescription description = script.getDescription();
        if (description.getRequiredTransaction() == RequiredTransaction.none)
        {
            wrappedExecute(scriptReq, scriptRes);
        }
        else
        {
            // encapsulate script within transaction
            RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Begin transaction: " + description.getRequiredTransaction());
                    
                    wrappedExecute(scriptReq, scriptRes);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("End transaction: " + description.getRequiredTransaction());
                    
                    return null;
                }        
            };
        
            if (description.getRequiredTransaction() == RequiredTransaction.required)
            {
                retryingTransactionHelper.doInTransaction(work); 
            }
            else
            {
                retryingTransactionHelper.doInTransaction(work, false, true); 
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
     * 
     * @return true if authorised, false otherwise
     */
    // TODO: DC - This method to be refactored during Web Script F/W extraction
    protected abstract boolean authenticate(RequiredAuthentication required, boolean isGuest, WebScriptRequest req, WebScriptResponse res);
    
    /**
     * Pre-execution hook
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     * @return  true => execute script, false => do not execute script
     */
    protected boolean preExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes)
    {
       return true;
    }

    /**
     * Post-execution hook
     * 
     * Note: this hook is not invoked if the script is not executed.
     * 
     * @param scriptReq  Web Script Request
     * @param scriptRes  Web Script Response
     */
    protected void postExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes)
    {
    }
    
    protected String getStatusCodeTemplate(int statusCode)
    {
        return "/" + statusCode + ".ftl";
    }
    
    protected String getStatusTemplate()
    {
        return "/status.ftl";
    }
    
}
