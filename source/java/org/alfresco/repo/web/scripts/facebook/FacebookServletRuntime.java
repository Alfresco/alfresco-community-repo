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
package org.alfresco.repo.web.scripts.facebook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.StatusTemplate;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;


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
     * @param container
     * @param authFactory
     * @param req
     * @param res
     * @param serverProperties
     * @param facebookService
     */
    public FacebookServletRuntime(RuntimeContainer container, ServletAuthenticatorFactory authFactory, HttpServletRequest req, HttpServletResponse res, 
            ServerProperties serverProperties, FacebookService facebookService)
    {
        super(container, authFactory, req, res, serverProperties);
        this.facebookService = facebookService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
     */
    @Override
    protected WebScriptRequest createRequest(Match match)
    {
        FacebookServletRequest fbreq = new FacebookServletRequest(this, req, match, serverProperties, getScriptUrl());
        
        if (match != null)
        {
            FacebookAppModel appModel = facebookService.getAppModel(fbreq.getApiKey());
            fbreq.setSecretKey(appModel.getSecret());
            fbreq.setAppId(appModel.getId());
        }

        if (logger.isDebugEnabled())
            logger.debug("Facebook request [apiKey=" + fbreq.getApiKey() + ", user=" + fbreq.getUserId() + ", session=" + fbreq.getSessionKey() + ", secret=" + fbreq.getSecretKey() + "]");
        
        servletReq = fbreq;
        return servletReq;
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
    protected StatusTemplate getStatusCodeTemplate(int statusCode)
    {
        return new StatusTemplate("/fbml." + statusCode + ".ftl", WebScriptResponse.HTML_FORMAT);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getStatusTemplate()
     */
    @Override
    protected StatusTemplate getStatusTemplate()
    {
        return new StatusTemplate("/fbml.status.ftl", WebScriptResponse.HTML_FORMAT);
    }

}
