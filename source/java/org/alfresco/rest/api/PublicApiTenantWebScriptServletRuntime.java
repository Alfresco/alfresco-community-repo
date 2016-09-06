/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.TenantWebScriptServletRuntime;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.alfresco.rest.framework.tools.ResponseWriter;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.*;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;

public class PublicApiTenantWebScriptServletRuntime extends TenantWebScriptServletRuntime implements ResponseWriter
{
    private static final Pattern CMIS_URI_PATTERN = Pattern.compile(".*/cmis/versions/[0-9]+\\.[0-9]+/.*");
    private ApiAssistant apiAssistant;

	public PublicApiTenantWebScriptServletRuntime(RuntimeContainer container, ServletAuthenticatorFactory authFactory, HttpServletRequest req,
			HttpServletResponse res, ServerProperties serverProperties, ApiAssistant apiAssistant)
	{
		super(container, authFactory, req, res, serverProperties);
        this.apiAssistant = apiAssistant;
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
        // MNT-13057 fix, do not decode CMIS uris.
        else if (CMIS_URI_PATTERN.matcher(requestURI).matches())
        {
            pathInfo = requestURI.substring(serviceContextPath.length());
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

    @Override
    protected void renderErrorResponse(Match match, Throwable exception, WebScriptRequest request, WebScriptResponse response) {

        //If its cmis or not an exception then use the default behaviour
        if (CMIS_URI_PATTERN.matcher(req.getRequestURI()).matches() || !(exception instanceof Exception))
        {
            super.renderErrorResponse(match, exception, request, response);
        }
        else
        {
            try {
                renderException((Exception)exception, response, apiAssistant);
            } catch (IOException e) {
                logger.error("Internal error", e);
                throw new WebScriptException("Internal error", e);
            }
        }

    }
}
