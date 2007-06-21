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
package org.alfresco.web.scripts.portlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.web.scripts.DeclarativeWebScriptRegistry;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptDescription;
import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.WebScriptRuntime;
import org.alfresco.web.scripts.WebScriptURLRequest;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;


/**
 * Generic JSR-168 Portlet for hosting an Alfresco Web Script as a Portlet.
 *
 * Accepts the following init-config:
 * 
 * scriptUrl => the url of the web script to host e.g. /alfresco/service/mytasks
 *  
 * @author davidc
 */
public class WebScriptPortlet implements Portlet
{
    private static Log logger = LogFactory.getLog(WebScriptPortlet.class);

    // Portlet initialisation
    protected String initScriptUrl = null;
    
    // Component Dependencies
    protected DeclarativeWebScriptRegistry registry;
    protected RetryingTransactionHelper transactionHelper;
    protected AuthorityService authorityService;
    protected WebScriptPortletAuthenticator authenticator;


    /* (non-Javadoc)
     * @see javax.portlet.Portlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException
    {
        initScriptUrl = config.getInitParameter("scriptUrl");
        PortletContext portletCtx = config.getPortletContext();
        WebApplicationContext ctx = (WebApplicationContext)portletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        registry = (DeclarativeWebScriptRegistry)ctx.getBean("webscripts.registry");
        transactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        authorityService = (AuthorityService)ctx.getBean("authorityService");
        
        // retrieve authenticator via portlet initialization parameter
        String authenticatorId = config.getInitParameter("authenticator");
        if (authenticatorId == null || authenticatorId.length() == 0)
        {
            authenticatorId = "webscripts.authenticator.jsr168";
        }
        Object bean = ctx.getBean(authenticatorId);
        if (bean == null || !(bean instanceof WebScriptPortletAuthenticator))
        {
            throw new PortletException("Initialisation parameter 'authenticator' does not refer to a Web Script authenticator (" + authenticatorId + ")");
        }
        authenticator = (WebScriptPortletAuthenticator)bean;
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest req, ActionResponse res) throws PortletException, PortletSecurityException, IOException
    {
        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet())
        {
            String name = param.getKey();
            if (name.equals("scriptUrl") || name.startsWith("arg."))
            {
                res.setRenderParameter(name, param.getValue());
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#render(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void render(RenderRequest req, RenderResponse res) throws PortletException, PortletSecurityException, IOException
    {
        PortletMode portletMode = req.getPortletMode();
        if (PortletMode.VIEW.equals(portletMode))
        {
           doView(req, res);
        }
//        else if (PortletMode.HELP.equals(portletMode))
//        {
//           doHelp(request, response);
//        }
//        else if (PortletMode.EDIT.equals(portletMode))
//        {
//           doEdit(request, response);
//        }
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#destroy()
     */
    public void destroy()
    {
    }

    /**
     * Render Web Script view
     * 
     * @param req
     * @param res
     * @throws PortletException
     * @throws PortletSecurityException
     * @throws IOException
     */
    protected void doView(RenderRequest req, RenderResponse res) throws PortletException, PortletSecurityException, IOException
    {
        //
        // Establish Web Script URL
        //
        
        String scriptUrl = req.getParameter("scriptUrl");
        if (scriptUrl != null)
        {
            // build web script url from render request
            StringBuilder scriptUrlArgs = new StringBuilder(128);
            Map<String, String[]> params = req.getParameterMap();
            for (Map.Entry<String, String[]> param : params.entrySet())
            {
                String name = param.getKey();
                if (name.startsWith("arg."))
                {
                    String argName = name.substring("arg.".length());
                    for (String argValue : param.getValue())
                    {
                        scriptUrlArgs.append((scriptUrlArgs.length() == 0) ? "" : "&");
                        // decode url arg (as it would be if this was a servlet)
                        try
                        {
                            scriptUrlArgs.append(argName).append("=")
                                         .append(URLDecoder.decode(argValue, "UTF-8"));
                        }
                        catch (UnsupportedEncodingException e)
                        {
                           throw new AlfrescoRuntimeException("Unable to decode UTF-8 url!", e);
                        }
                       
                    }
                }
            }
            scriptUrl += (scriptUrlArgs.length() != 0 ? ("?" + scriptUrlArgs.toString()) : "");
        }
        else
        {
            // retrieve initial scriptUrl as configured by Portlet
            scriptUrl = initScriptUrl;
            if (scriptUrl == null)
            {
                throw new PortletException("Initial Web script URL has not been specified.");
            }
        }
    
        //
        // Execute Web Script
        //
        
        if (logger.isDebugEnabled())
            logger.debug("Processing portal render request " + req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/" + req.getContextPath() + " (scriptUrl=" + scriptUrl + ")");

        WebScriptRuntime runtime = new WebScriptPortalRuntime(req, res, scriptUrl);
        runtime.executeScript();
    }
    
    /**
     * JSR-168 Web Script Runtime
     * 
     * @author davidc
     */
    private class WebScriptPortalRuntime extends WebScriptRuntime
    {
        private RenderRequest req;
        private RenderResponse res;
        private String[] requestUrlParts;
        

        /**
         * Construct
         * @param req
         * @param res
         * @param requestUrl
         */
        public WebScriptPortalRuntime(RenderRequest req, RenderResponse res, String requestUrl)
        {
            super(registry, transactionHelper, authorityService);
            this.req = req;
            this.res = res;
            this.requestUrlParts = WebScriptURLRequest.splitURL(requestUrl);
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptMethod()
         */
        @Override
        protected String getScriptMethod()
        {
            return "get";
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
         */
        @Override
        protected String getScriptUrl()
        {
            return requestUrlParts[2];
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
         */
        @Override
        protected WebScriptRequest createRequest(WebScriptMatch match)
        {
            return new WebScriptPortletRequest(req, requestUrlParts, match);
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#createResponse()
         */
        @Override
        protected WebScriptResponse createResponse()
        {
            return new WebScriptPortletResponse(res);
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean)
         */
        @Override
        protected boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            return authenticator.authenticate(required, isGuest, req, res);
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptRuntime#preExecute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
         */
        @Override
        protected boolean preExecute(WebScriptRequest scriptReq, WebScriptResponse scriptRes)
        {
            // Set Portlet title based on Web Script
            WebScript script = scriptReq.getServiceMatch().getWebScript();
            WebScriptDescription desc = script.getDescription();
            res.setTitle(desc.getShortName());

            // Note: Do not render script if portlet window is minimized
            return !WindowState.MINIMIZED.equals(req.getWindowState());            
        }
    }
}
