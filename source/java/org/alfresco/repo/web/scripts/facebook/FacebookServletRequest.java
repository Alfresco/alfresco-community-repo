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
package org.alfresco.repo.web.scripts.facebook;

import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.Runtime;

/**
 * Facebook Servlet Request
 * 
 * @author davidc
 */
@SuppressWarnings("deprecation")
public class FacebookServletRequest extends WebScriptServletRequest
{
    private String appId;
    private String secretKey;
    private String pathInfo;
    
    
    /**
     * Construct
     *
     * @param serverConfig
     * @param req
     * @param serviceMatch
     */
    public FacebookServletRequest(Runtime container, HttpServletRequest req, Match serviceMatch, ServerProperties serverProperties, String pathInfo)
    {
        super(container, req, serviceMatch, serverProperties);
        this.pathInfo = pathInfo;
    }
    
    
    /**
     * @param secretKey  application secret
     */
    /*package*/ void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }
    
    /**
     * @param appId  application id
     */
    /*package*/ void setAppId(String appId)
    {
        this.appId = appId;
    }
    
    /**
     * @return  application api key
     */
    public String getApiKey()
    {
        return getParameter("fb_sig_api_key");
    }
    
    /**
     * @return  Facebook user id
     */
    public String getUserId()
    {
        return getParameter("fb_sig_user");
    }

    /**
     * @return  session key
     */
    public String getSessionKey()
    {
        return getParameter("fb_sig_session_key");
    }

    /**
     * @return  true => within Facebook canvas
     */
    public boolean isInCanvas()
    {
        String canvas = getParameter("fb_sig_api_key");
        return (canvas == null || canvas.equals("1"));
    }
    
    /**
     * @return  application secret
     */
    public String getSecretKey()
    {
        return secretKey;
    }
    
    /**
     * @return  application id
     */
    public String getAppId()
    {
        return appId;
    }

    /**
     * @return  application canvas path
     */
    public String getCanvasPath()
    {
        String pathInfo = getPathInfo();
        String[] pathSegments = pathInfo.split("/");
        if (pathSegments.length < 3)
        {
            throw new WebScriptException("Cannot establish Facebook Canvas Page URL from request " + getURL());
        }
        return pathSegments[2];
    }
    
    /**
     * @return  application page path
     */
    public String getPagePath()
    {
        String pagePath = getPathInfo();
        if (pagePath.startsWith("/facebook"))
        {
            pagePath = pathInfo.substring("/facebook".length());
        }
        return pagePath;
    }

    /**
     * @return  friends of authenticated Facebook user
     */
    public String[] getFriends()
    {
        String[] friends;
        String friendsStr = getParameter("fb_sig_friends");
        friends = (friendsStr == null) ? new String[0] : friendsStr.split(",");
        return friends;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.WebScriptServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo()
    {
        return pathInfo;
    }
    
}
