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
 * Facebook Servlet Request
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
     * @see org.alfresco.web.scripts.WebScriptServletRequest#getPathInfo()
     */
    @Override
    public String getPathInfo()
    {
        return pathInfo;
    }
    
}
