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
package org.alfresco.web.scripts.facebook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.config.ServerConfigElement;
import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRegistry;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptServletAuthenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Facebook Canvas Page Servlet.
 * 
 * @author davidc
 */
public class FacebookServletRuntime extends FacebookAPIRuntime
{
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookServletRuntime.class);

    // Component dependencies
    protected FacebookService facebookService;

    /**
     * Construct
     * 
     * @param registry
     * @param serviceRegistry
     * @param authenticator
     * @param req
     * @param res
     */
    public FacebookServletRuntime(WebScriptRegistry registry, ServiceRegistry serviceRegistry, WebScriptServletAuthenticator authenticator,
            HttpServletRequest req, HttpServletResponse res, ServerConfigElement serverConfig, FacebookService facebookService)
    {
        super(registry, serviceRegistry, authenticator, req, res, serverConfig);
        this.facebookService = facebookService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
     */
    @Override
    protected WebScriptRequest createRequest(WebScriptMatch match)
    {
        FacebookServletRequest fbreq = new FacebookServletRequest(serverConfig, req, match, getScriptUrl());
        
        if (match != null)
        {
            FacebookAppModel appModel = facebookService.getAppModel(fbreq.getApiKey());
            fbreq.setSecretKey(appModel.getSecret());
            fbreq.setAppId(appModel.getId());
        }

        if (logger.isDebugEnabled())
            logger.debug("Facebook request [apiKey=" + fbreq.getApiKey() + ", user=" + fbreq.getUserId() + ", session=" + fbreq.getSessionKey() + ", secret=" + fbreq.getSecretKey() + "]");
        
        return fbreq;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptServletRuntime#getScriptUrl()
     */
    @Override
    protected String getScriptUrl()
    {
        return "/facebook" + super.getScriptUrl();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getStatusCodeTemplate(int)
     */
    @Override
    protected String getStatusCodeTemplate(int statusCode)
    {
        return "/fbml." + statusCode + ".ftl";
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getStatusTemplate()
     */
    @Override
    protected String getStatusTemplate()
    {
        return "/fbml.status.ftl";
    }

}
