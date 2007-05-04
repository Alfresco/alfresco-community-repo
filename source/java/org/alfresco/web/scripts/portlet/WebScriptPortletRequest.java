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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRequest;


/**
 * JSR-168 Web Script Request
 * 
 * @author davidc
 */
public class WebScriptPortletRequest implements WebScriptRequest
{
    /** Portlet Request */
    private PortletRequest req;
    
    /** Script Url components */
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private String queryString;
    private Map<String, String> queryArgs;
    
    /** Service bound to this request */
    private WebScriptMatch serviceMatch;

    
    /**
     * Splits a portlet scriptUrl into its component parts
     * 
     * @param scriptUrl  url  e.g. /alfresco/service/mytasks?f=1 
     * @return  url parts  [0] = context (e.g. alfresco), [1] = servlet (e.g. service), [2] = script (e.g. mytasks), [3] = args (e.g. f=1)
     */
    public static String[] getScriptUrlParts(String scriptUrl)
    {
        String[] urlParts = new String[4];
        String path;
        String queryString;
        
        int argsIndex = scriptUrl.indexOf("?");
        if (argsIndex != -1)
        {
            path = scriptUrl.substring(0, argsIndex);
            queryString = scriptUrl.substring(argsIndex + 1);
        }
        else
        {
            path = scriptUrl;
            queryString = null;
        }
        
        String[] pathSegments = path.split("/");
        String pathInfo = "";
        for (int i = 3; i < pathSegments.length; i++)
        {
            pathInfo += "/" + pathSegments[i];
        }
        
        urlParts[0] = "/" + pathSegments[1];    // context path
        urlParts[1] = "/" + pathSegments[2];    // servlet path
        urlParts[2] = pathInfo;                 // path info
        urlParts[3] = queryString;              // query string

        return urlParts;
    }
    
    
    /**
     * Construct
     * 
     * @param req
     * @param scriptUrl
     * @param serviceMatch
     */
    WebScriptPortletRequest(PortletRequest req, String scriptUrl, WebScriptMatch serviceMatch)
    {
        this(req, getScriptUrlParts(scriptUrl), serviceMatch);
    }
    
    /**
     * Construct
     * 
     * @param req
     * 'param scriptUrlParts
     * @param serviceMatch
     */
    WebScriptPortletRequest(PortletRequest req, String[] scriptUrlParts, WebScriptMatch serviceMatch)
    {
        this.req = req;
        this.contextPath = scriptUrlParts[0];
        this.servletPath = scriptUrlParts[1];
        this.pathInfo = scriptUrlParts[2];
        this.queryString = scriptUrlParts[3];
        this.queryArgs = new HashMap<String, String>();
        if (this.queryString != null)
        {
            String[] args = this.queryString.split("&");
            for (String arg : args)
            {
                String[] parts = arg.split("=");
                // TODO: Handle multi-value parameters
                this.queryArgs.put(parts[0], parts.length == 2 ? parts[1] : "");
            }
        }
        this.serviceMatch = serviceMatch;
    }

    /**
     * Gets the Portlet Request
     * 
     * @return  Portlet Request
     */
    public PortletRequest getPortletRequest()
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
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getContextPath()
     */
    public String getContextPath()
    {
        return contextPath;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServiceContextPath()
     */
    public String getServiceContextPath()
    {
        return getContextPath() + servletPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getServicePath()
     */
    public String getServicePath()
    {
        return getServiceContextPath() + pathInfo;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getURL()
     */
    public String getURL()
    {
        return getServicePath() + (queryString != null ? "?" + queryString : "");
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getQueryString()
     */
    public String getQueryString()
    {
        return queryString;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getParameterNames()
     */
    public String[] getParameterNames()
    {
        Set<String> keys = queryArgs.keySet();
        String[] names = new String[keys.size()];
        keys.toArray(names);
        return names;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name)
    {
        return queryArgs.get(name);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getExtensionPath()
     */
    public String getExtensionPath()
    {
        String servicePath = serviceMatch.getPath();
        String extensionPath = pathInfo;
        int extIdx = extensionPath.indexOf(servicePath);
        if (extIdx != -1)
        {
            int extLength = (servicePath.endsWith("/") ? servicePath.length() : servicePath.length() + 1);
            extensionPath = extensionPath.substring(extIdx + extLength);
        }
        return extensionPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#isGuest()
     */
    public boolean isGuest()
    {
        return Boolean.valueOf(queryArgs.get("guest"));
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getFormat()
     */
    public String getFormat()
    {
        String format = queryArgs.get("format");
        return (format == null || format.length() == 0) ? "" : format;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRequest#getAgent()
     */
    public String getAgent()
    {
        // NOTE: rely on default agent mappings
        return null;
    }
    
}
