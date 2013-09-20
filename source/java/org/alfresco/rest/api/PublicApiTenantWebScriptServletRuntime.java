/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.TenantWebScriptServletRuntime;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;

public class PublicApiTenantWebScriptServletRuntime extends TenantWebScriptServletRuntime
{
	public PublicApiTenantWebScriptServletRuntime(RuntimeContainer container, ServletAuthenticatorFactory authFactory, HttpServletRequest req,
			HttpServletResponse res, ServerProperties serverProperties)
	{
		super(container, authFactory, req, res, serverProperties);
	}

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#getScriptUrl()
     */
    @Override
    protected String getScriptUrl()
    {
        // NOTE: Don't use req.getPathInfo() - it truncates the path at first semi-colon in Tomcat
        final String requestURI = req.getRequestURI();
        final String serviceContextPath = req.getContextPath() + req.getServletPath();
        String pathInfo;
        
        if (serviceContextPath.length() > requestURI.length())
        {
            // NOTE: assume a redirect has taken place e.g. tomcat welcome-page
            // NOTE: this is unlikely, and we'll take the hit if the path contains a semi-colon
            pathInfo = req.getPathInfo();
        }
        else
        {
            pathInfo = URLDecoder.decode(requestURI.substring(serviceContextPath.length()));
        }
        
        // NOTE: must contain at least root / and single character for tenant name
        if (pathInfo.length() < 2 || pathInfo.equals("/"))
        {
        	// url path has no tenant id -> get networks request 
        	pathInfo = PublicApiTenantWebScriptServletRequest.NETWORKS_PATH;
        }
        else
        {
        	if(!pathInfo.substring(0, 6).toLowerCase().equals("/cmis/"))
        	{
		        // remove tenant
		        int idx = pathInfo.indexOf('/', 1);
		        pathInfo = pathInfo.substring(idx == -1 ? pathInfo.length() : idx);
		        if(pathInfo.equals("") || pathInfo.equals("/"))
		        {
		        	// url path is just a tenant id -> get network request 
		        	pathInfo = PublicApiTenantWebScriptServletRequest.NETWORK_PATH;
		        }
        	}
        }

        return pathInfo;
    }
	
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptRuntime#createRequest(org.alfresco.web.scripts.WebScriptMatch)
     */
    @Override
    protected WebScriptRequest createRequest(Match match)
    {
//    	try
//    	{
    		// make the request input stream a BufferedInputStream so that the first x bytes can be reused.
//	    	PublicApiHttpServletRequest wrapped = new PublicApiHttpServletRequest(req);

	        // TODO: construct org.springframework.extensions.webscripts.servlet.WebScriptServletResponse when
	        //       org.alfresco.web.scripts.WebScriptServletResponse (deprecated) is removed
	        servletReq = new PublicApiTenantWebScriptServletRequest(this, req, match, serverProperties);
	        return servletReq;
//    	}
//    	catch(IOException e)
//    	{
//    		throw new AlfrescoRuntimeException("", e);
//    	}
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptContainer#getName()
     */
    public String getName()
    {
        return "PublicApiTenantServletRuntime";
    }
}
