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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.TenantWebScriptServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PublicApiWebScriptServlet extends TenantWebScriptServlet
{
	private static final long serialVersionUID = 726730674397482039L;
	
    @Override
    public void init() throws ServletException
    {
    	super.init();
    	
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        container = (RuntimeContainer)context.getBean("publicapi.container");
    }
    
    /* (non-Javadoc) 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
		// make the request input stream a BufferedInputStream so that the first x bytes can be reused.
    	PublicApiHttpServletRequest wrapped = new PublicApiHttpServletRequest(req);
        super.service(wrapped, res);
    }
    
    protected WebScriptServletRuntime getRuntime(HttpServletRequest req, HttpServletResponse res)
    {
        WebScriptServletRuntime runtime = new PublicApiTenantWebScriptServletRuntime(container, authenticatorFactory, req, res, serverProperties);
        return runtime;
    }
}
