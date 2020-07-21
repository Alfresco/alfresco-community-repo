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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.TenantWebScriptServlet;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PublicApiWebScriptServlet extends TenantWebScriptServlet
{
	private static final long serialVersionUID = 726730674397482039L;
	private ApiAssistant apiAssistant;
    @Override
    public void init() throws ServletException
    {
    	super.init();
    	
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        container = (RuntimeContainer)context.getBean("publicapi.container");
        apiAssistant = (ApiAssistant) context.getBean("apiAssistant");
    }

    protected WebScriptServletRuntime getRuntime(HttpServletRequest req, HttpServletResponse res)
    {
        WebScriptServletRuntime runtime = new PublicApiTenantWebScriptServletRuntime(container, authenticatorFactory, req, res, serverProperties, apiAssistant);
        return runtime;
    }
}
