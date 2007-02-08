/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.alfresco.repo.security.authentication.AuthenticationUtil;


/**
 * API Service Request
 * 
 * @author davidc
 */
public class APIRequest extends HttpServletRequestWrapper
{

    /**
     * Enumerartion of HTTP Methods 
     */
    public enum HttpMethod
    {
        GET;
        // TODO: Complete list...
    }

    /**
     * Enumeration of "required" Authentication level
     */
    public enum RequiredAuthentication
    {
        None,
        Guest,
        User
    }
    
    /**
     * Construct
     * 
     * @param req
     */
    /*package*/ APIRequest(HttpServletRequest req)
    {
        super(req);
    }

    /**
     * Gets the HTTP Method
     * 
     * @return  Http Method
     */
    public HttpMethod getHttpMethod()
    {
        String method = getMethod().trim().toUpperCase();
        return HttpMethod.valueOf(method);
    }

    /**
     * Gets the Alfresco Context URL
     *  
     * @return  context url  e.g. http://localhost:port/alfresco
     */
    public String getPath()
    {
        return getScheme() + "://" + getServerName() + ":" + getServerPort() + getContextPath();
    }

    /**
     * Gets the Alfresco Service URL
     * 
     * @return  service url  e.g. http://localhost:port/alfresco/service
     */
    public String getServicePath()
    {
        return getPath() + getServletPath();
    }

    /**
     * Gets the full request URL
     * 
     * @return  request url e.g. http://localhost:port/alfresco/service/text?searchTerms=dsfsdf
     */
    public String getUrl()
    {
        return getScheme() + "://" + getServerName() + ":" + getServerPort() + getPathInfo() + (getQueryString() != null ? "?" + getQueryString() : "");
    }
    
    /**
     * Gets the currently authenticated username
     * 
     * @return  username
     */
    public String getAuthenticatedUsername()
    {
        return AuthenticationUtil.getCurrentUserName();
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
        if (userAgent.indexOf("Firefox/") != -1)
        {
            return "Firefox";
        }
        else if (userAgent.indexOf("MSIE") != -1)
        {
            return "MSIE";
        }
        return null;
    }
}
