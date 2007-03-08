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
package org.alfresco.web.api.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


/**
 * API Service Request
 * 
 * @author davidc
 */
public class APIRequest extends HttpServletRequestWrapper
{
    /** Service bound to this request */
    private APIServiceMatch serviceMatch;
    
    
    /**
     * Construct
     * 
     * @param req
     * @param serviceMatch
     */
    /*package*/ APIRequest(HttpServletRequest req, APIServiceMatch serviceMatch)
    {
        super(req);
        this.serviceMatch = serviceMatch;
    }

    /**
     * Gets the matching API Service for this request
     * 
     * @return  the service match
     */
    public APIServiceMatch getServiceMatch()
    {
        return serviceMatch;
    }
    
    /**
     * Get server portion of the request
     *
     * e.g. scheme://host:port
     *  
     * @return  server path
     */
    public String getServerPath()
    {
        return getScheme() + "://" + getServerName() + ":" + getServerPort();
    }
    
    /**
     * Gets the Alfresco Service Context Path
     * 
     * @return  service url  e.g. /alfresco/service
     */
    public String getServiceContextPath()
    {
        return getContextPath() + getServletPath();
    }

    /**
     * Gets the Alfresco Service Path
     * 
     * @return  service url  e.g. /alfresco/service/search/keyword
     */
    public String getServicePath()
    {
        return getServiceContextPath() + getPathInfo();
    }

    /**
     * Gets the full request URL
     * 
     * @return  request url e.g. /alfresco/service/search/keyword?q=term
     */
    public String getURL()
    {
        return getServicePath() + (getQueryString() != null ? "?" + getQueryString() : "");
    }
    
    /**
     * Gets the path extension beyond the path registered for this service
     * 
     * e.g.
     * a) service registered path = /search/engine
     * b) request path = /search/engine/external
     * 
     * => /external
     * 
     * @return  extension path
     */
    public String getExtensionPath()
    {
        String servicePath = serviceMatch.getPath();
        String extensionPath = getPathInfo();
        int extIdx = extensionPath.indexOf(servicePath);
        if (extIdx != -1)
        {
            int extLength = (servicePath.endsWith("/") ? servicePath.length() : servicePath.length() + 1);
            extensionPath = extensionPath.substring(extIdx + extLength);
        }
        return extensionPath;
    }

    /**
     * Determine if Guest User?
     * 
     * @return  true => guest user
     */
    public boolean isGuest()
    {
        return Boolean.valueOf(getParameter("guest"));
    }
    
    /**
     * Get Requested Format
     * 
     * @return  content type requested
     */
    public String getFormat()
    {
        String format = getParameter("format");
        return (format == null || format.length() == 0) ? "" : format;
    }
 
    /**
     * Get User Agent
     * 
     * TODO: Expand on known agents
     * 
     * @return  MSIE / Firefox
     */
    public String getAgent()
    {
        String userAgent = getHeader("user-agent");
        if (userAgent != null)
        {
            if (userAgent.indexOf("Firefox/") != -1)
            {
                return "Firefox";
            }
            else if (userAgent.indexOf("MSIE") != -1)
            {
                return "MSIE";
            }
        }
        return null;
    }
}
