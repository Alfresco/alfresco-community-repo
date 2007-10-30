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

import org.alfresco.web.config.ServerConfigElement;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptServletRequest;


/**
 * HTTP Servlet Web Script Request
 * 
 * @author davidc
 */
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
    public FacebookServletRequest(ServerConfigElement serverConfig, HttpServletRequest req, WebScriptMatch serviceMatch, String pathInfo)
    {
        super(serverConfig, req, serviceMatch);
        this.pathInfo = pathInfo;
    }
    
    /*package*/ void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }
    
    /*package*/ void setAppId(String appId)
    {
        this.appId = appId;
    }
    
    public String getApiKey()
    {
        return getParameter("fb_sig_api_key");
    }
    
    public String getUserId()
    {
        return getParameter("fb_sig_user");
    }
    
    public String getSessionKey()
    {
        return getParameter("fb_sig_session_key");
    }

    public boolean isInCanvas()
    {
        String canvas = getParameter("fb_sig_api_key");
        return (canvas == null || canvas.equals("1"));
    }
    
    public String getSecretKey()
    {
        return secretKey;
    }
    
    public String getAppId()
    {
        return appId;
    }
    
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
    
    public String[] getFriends()
    {
        String[] friends;
        String friendsStr = getParameter("fb_sig_friends");
        friends = (friendsStr == null) ? new String[0] : friendsStr.split(",");
        return friends;
    }
    
    @Override
    public String getPathInfo()
    {
        return pathInfo;
    }
}
