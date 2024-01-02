/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.opencmis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.web.scripts.TenantWebScriptServletRequest;
import org.alfresco.service.descriptor.Descriptor;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

/**
 * Wraps an OpenCMIS HttpServletRequest, mapping urls and adding servlet attributes specific to the Alfresco implementation of OpenCMIS.
 */
@SuppressWarnings("rawtypes")
public class CMISHttpServletRequest extends HttpServletRequestWrapper
{
	protected WebScriptRequest req;
	protected String networkId;
	protected String operation;
	protected String id; // object id (or path for browser binding)
	protected String serviceName;
	protected BaseUrlGenerator baseUrlGenerator;
	protected Binding binding;
	protected Descriptor currentDescriptor;

	public CMISHttpServletRequest(WebScriptRequest req, String serviceName, BaseUrlGenerator baseUrlGenerator, Binding binding, Descriptor currentDescriptor,
	        TenantAdminService tenantAdminService)
	{
		super(WebScriptServletRuntime.getHttpServletRequest(req));
		this.req = req;
		this.serviceName = serviceName;
		this.baseUrlGenerator = baseUrlGenerator;
		this.binding = binding;

		String pathInfo = req.getPathInfo();
		WebScriptRequest baseReq = getBaseRequest(req);
		if(!pathInfo.startsWith("/cmis") && (baseReq instanceof TenantWebScriptServletRequest))
		{
			TenantWebScriptServletRequest servletReq = (TenantWebScriptServletRequest)baseReq;

			String tenant = servletReq.getTenant();
			if(tenant.equalsIgnoreCase(TenantUtil.DEFAULT_TENANT))
            {
			    String user = AuthenticationUtil.getFullyAuthenticatedUser();
                String domain = tenantAdminService.getUserDomain(user);
                if(domain == null || domain.equals(TenantService.DEFAULT_DOMAIN))
                {
                    this.networkId = tenant;
                }
                else
                {
                    this.networkId = domain;
                }
            }
			else
			{
			    this.networkId = tenant;
			}
		}

		Match match = req.getServiceMatch();
		Map<String, String> templateVars = match.getTemplateVars();

		this.operation = templateVars.get("operation");
		this.id = templateVars.get("id");

    	addAttributes();
	}

	@Override
	public HttpServletRequest getRequest()
	{
		return (HttpServletRequest) super.getRequest();
	}
	
	/*
	 * Recursively unwrap req if it is a WrappingWebScriptRequest
	 */
	private WebScriptRequest getBaseRequest(WebScriptRequest req)
	{
		WebScriptRequest ret = req;
		while(ret instanceof WrappingWebScriptRequest)
		{
			WrappingWebScriptRequest wrapping = (WrappingWebScriptRequest)req;
			ret = wrapping.getNext();
		}
		return ret;
	}

	protected void addAttributes()
	{
		if(networkId != null)
		{
			super.setAttribute(Constants.PARAM_REPOSITORY_ID, networkId);
		}
		super.setAttribute("serviceName", serviceName);
	}

	@Override
	public Object getAttribute(String arg0)
	{
		if(arg0.equals(Dispatcher.BASE_URL_ATTRIBUTE))
		{
			return baseUrlGenerator.getBaseUrl(getRequest(), networkId, binding);
		}
		else
		{
			return super.getAttribute(arg0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getAttributeNames()
	{
		Enumeration e = super.getAttributeNames();
		List attrNames = new ArrayList();
		while(e.hasMoreElements())
		{
			attrNames.add(e.nextElement());
		}
		attrNames.add(Dispatcher.BASE_URL_ATTRIBUTE);
		final Iterator it = attrNames.iterator();

	    return new Enumeration()
	    {
	        public boolean hasMoreElements()
	        {
	            return it.hasNext();
	        }

	        public Object nextElement()
	        {
	            return it.next();
	        }
	    };
	}

	@Override
	public String getParameter(String arg0)
	{
		if(arg0.equals(Constants.PARAM_REPOSITORY_ID))
		{
			return networkId;
		}
		return super.getParameter(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getParameterMap()
	{
		Map map = super.getParameterMap();
		Map ret = new HashedMap(map);
		if(networkId != null)
		{
			ret.put(Constants.PARAM_REPOSITORY_ID, new String[] { networkId });
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getParameterNames()
	{
		final Enumeration e = super.getParameterNames();
		List l = new ArrayList();
		while(e.hasMoreElements())
		{
			l.add(e.nextElement());
		}
		if(networkId != null)
		{
			l.add(Constants.PARAM_REPOSITORY_ID);
		}
		final Iterator it = l.iterator();
		Enumeration ret = new Enumeration()
		{
			@Override
			public boolean hasMoreElements()
			{
				return it.hasNext();
			}

			@Override
			public Object nextElement()
			{
				return it.next();
			}
		};

		return ret;
	}

	@Override
	public String getContextPath()
	{
		String contextPath = baseUrlGenerator.getContextPath(getRequest());
		return contextPath;
	}

	@Override
	public String getPathInfo()
	{
		StringBuilder sb = new StringBuilder("/");
		sb.append(networkId == null ? TenantUtil.DEFAULT_TENANT : networkId);
		if(operation != null)
		{
			sb.append("/");
			sb.append(operation);
		}
		return sb.toString();
	}

	@Override
	public String getQueryString()
	{
        StringBuilder queryString = new StringBuilder();
        String reqQueryString = super.getQueryString();

        if(networkId != null && networkId.length() > 0)
        {
            if (reqQueryString != null)
            {
	            queryString.append(reqQueryString + "&");
            }
	        queryString.append("repositoryId=" + networkId);
            if(operation == null || operation.isEmpty())
            {
            	queryString.append("&cmisselector=");
	            queryString.append(Constants.SELECTOR_REPOSITORY_INFO);
            }
            return queryString.toString();
        }
        return reqQueryString;
	}

	@Override
	public String getRequestURI()
	{
		String requestURI = baseUrlGenerator.getRequestURI(getRequest(), networkId, operation, id);
		return requestURI;
	}

	@Override
	public String getServletPath()
	{
		String servletPath = baseUrlGenerator.getServletPath(getRequest());
		return servletPath;
	}
}