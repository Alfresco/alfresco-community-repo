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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.web.config.ServerConfigElement;


/**
 * HTTP Servlet Web Script Request
 * 
 * @author davidc
 */
public class WebScriptServletRequest extends WebScriptRequestImpl
{
    /** Server Config */
    private ServerConfigElement serverConfig;
    
    /** HTTP Request */
    private HttpServletRequest req;
    
    /** Service bound to this request */
    private WebScriptMatch serviceMatch;
    
    
    /**
     * Construct
     * 
     * @param req
     * @param serviceMatch
     */
    WebScriptServletRequest(HttpServletRequest req, WebScriptMatch serviceMatch)
    {
        this(null, req, serviceMatch);
    }

    /**
     * Construct
     *
     * @param serverConfig
     * @param req
     * @param serviceMatch
     */
    WebScriptServletRequest(ServerConfigElement serverConfig, HttpServletRequest req, WebScriptMatch serviceMatch)
    {
        this.serverConfig = serverConfig;
        this.req = req;
        this.serviceMatch = serviceMatch;
    }
    
    /**
     * Gets the HTTP Servlet Request
     * 
     * @return  HTTP Servlet Request
     */
    public HttpServletRequest getHttpServletRequest()
    {
        return req;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServiceMatch()
     */
    public WebScriptMatch getServiceMatch()
    {
        return serviceMatch;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServerPath()
     */
    public String getServerPath()
    {
        return getServerScheme() + "://" + getServerName() + ":" + getServerPort();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getContextPath()
     */
    public String getContextPath()
    {
        return req.getContextPath();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServiceContextPath()
     */
    public String getServiceContextPath()
    {
        return req.getContextPath() + req.getServletPath();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServicePath()
     */
    public String getServicePath()
    {
        String pathInfo = getPathInfo();
        return getServiceContextPath() + ((pathInfo == null) ? "" : pathInfo);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getURL()
     */
    public String getURL()
    {
        return getServicePath() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getPathInfo()
     */
    public String getPathInfo()
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        String requestURI = req.getRequestURI();
        String pathInfo = requestURI.substring(getServiceContextPath().length());
        try
        {
            return URLDecoder.decode(pathInfo, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new WebScriptException("Failed to retrieve path info", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getQueryString()
     */
    public String getQueryString()
    {
        return req.getQueryString();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getParameterNames()
     */
    public String[] getParameterNames()
    {
        Set<String> keys = req.getParameterMap().keySet();
        String[] names = new String[keys.size()];
        keys.toArray(names);
        return names;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name)
    {
        return req.getParameter(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getArrayParameter(java.lang.String)
     */
    public String[] getParameterValues(String name)
    {
        return req.getParameterValues(name);
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
        String userAgent = req.getHeader("user-agent");
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

    /**
     * Get Server Scheme
     * 
     * @return  server scheme
     */
    private String getServerScheme()
    {
        String scheme = null;
        if (serverConfig != null)
        {
            scheme = serverConfig.getScheme();
        }
        if (scheme == null)
        {
            scheme = req.getScheme();
        }
        return scheme;
    }

    /**
     * Get Server Name
     * 
     * @return  server name
     */
    private String getServerName()
    {
        String name = null;
        if (serverConfig != null)
        {
            name = serverConfig.getHostName();
        }
        if (name == null)
        {
            name = req.getServerName();
        }
        return name;
    }

    /**
     * Get Server Port
     * 
     * @return  server name
     */
    private int getServerPort()
    {
        Integer port = null;
        if (serverConfig != null)
        {
            port = serverConfig.getPort();
        }
        if (port == null)
        {
            port = req.getServerPort();
        }
        return port;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequestImpl#forceSuccessStatus()
     */
    @Override
    public boolean forceSuccessStatus()
    {
        String forceSuccess = req.getHeader("alf-force-success-response");
        return Boolean.valueOf(forceSuccess);
    }

}
