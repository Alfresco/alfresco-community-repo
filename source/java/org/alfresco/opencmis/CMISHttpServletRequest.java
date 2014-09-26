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
package org.alfresco.opencmis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
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
public class CMISHttpServletRequest implements HttpServletRequest
{
	protected WebScriptRequest req;
	protected HttpServletRequest httpReq;
	protected String networkId;
	protected String operation;
	protected String id; // object id (or path for browser binding)
	protected String serviceName;
	protected BaseUrlGenerator baseUrlGenerator;
	protected Binding binding;
	protected Descriptor currentDescriptor;

	public CMISHttpServletRequest(WebScriptRequest req, String serviceName, BaseUrlGenerator baseUrlGenerator, Binding binding, Descriptor currentDescriptor)
	{
		this.req = req;
		this.serviceName = serviceName;
		this.baseUrlGenerator = baseUrlGenerator;
		this.binding = binding;

		String pathInfo = req.getPathInfo();
		WebScriptRequest baseReq = getBaseRequest(req);
		if(!pathInfo.startsWith("/cmis") && (baseReq instanceof TenantWebScriptServletRequest))
		{
			TenantWebScriptServletRequest servletReq = (TenantWebScriptServletRequest)baseReq;
			this.networkId = servletReq.getTenant();
			if(TenantUtil.DEFAULT_TENANT.equals(this.networkId) || TenantUtil.SYSTEM_TENANT.equals(this.networkId))
			{
				this.networkId = TenantService.DEFAULT_DOMAIN;
			}
		}

		Match match = req.getServiceMatch();
		Map<String, String> templateVars = match.getTemplateVars();

        HttpServletRequest httpReq = WebScriptServletRuntime.getHttpServletRequest(req);
		this.httpReq = httpReq;
		this.operation = templateVars.get("operation");
		this.id = templateVars.get("id");

    	addAttributes();
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
			httpReq.setAttribute(Constants.PARAM_REPOSITORY_ID, networkId);
		}
		httpReq.setAttribute("serviceName", serviceName);
	}

	@Override
	public Object getAttribute(String arg0)
	{
		if(arg0.equals(Dispatcher.BASE_URL_ATTRIBUTE))
		{
			return baseUrlGenerator.getBaseUrl(this, networkId, binding);
		}
		else
		{
			return httpReq.getAttribute(arg0);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getAttributeNames()
	{
		Enumeration e = httpReq.getAttributeNames();
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
	public String getCharacterEncoding()
	{
		return httpReq.getCharacterEncoding();
	}

	@Override
	public int getContentLength()
	{
		return httpReq.getContentLength();
	}

	@Override
	public String getContentType()
	{
		return httpReq.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return httpReq.getInputStream();
	}

	@Override
	public String getLocalAddr()
	{
		return httpReq.getLocalAddr();
	}

	@Override
	public String getLocalName()
	{
		return httpReq.getLocalName();
	}

	@Override
	public int getLocalPort()
	{
		return httpReq.getLocalPort();
	}

	@Override
	public Locale getLocale()
	{
		return httpReq.getLocale();
	}

	@Override
	public Enumeration getLocales()
	{
		return httpReq.getLocales();
	}

	@Override
	public String getParameter(String arg0)
	{
		if(arg0.equals(Constants.PARAM_REPOSITORY_ID))
		{
			return networkId;
		}
		return httpReq.getParameter(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map getParameterMap()
	{
		Map map = httpReq.getParameterMap();
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
		final Enumeration e = httpReq.getParameterNames();
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
	public String[] getParameterValues(String arg0)
	{
		return httpReq.getParameterValues(arg0);
	}

	@Override
	public String getProtocol()
	{
		return httpReq.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return httpReq.getReader();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getRealPath(String arg0)
	{
		return httpReq.getRealPath(arg0);
	}

	@Override
	public String getRemoteAddr()
	{
		return httpReq.getRemoteAddr();
	}

	@Override
	public String getRemoteHost()
	{
		return httpReq.getRemoteHost();
	}

	@Override
	public int getRemotePort()
	{
		return httpReq.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		return httpReq.getRequestDispatcher(arg0);
	}

	@Override
	public String getScheme()
	{
		return httpReq.getScheme();
	}

	@Override
	public String getServerName() 
	{
		return httpReq.getServerName();
	}

	@Override
	public int getServerPort()
	{
		return httpReq.getServerPort();
	}

	@Override
	public boolean isSecure()
	{
		return httpReq.isSecure();
	}

	@Override
	public void removeAttribute(String arg0)
	{
		httpReq.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1)
	{
		httpReq.setAttribute(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
	{
		httpReq.setCharacterEncoding(arg0);
	}

	@Override
	public String getAuthType()
	{
		return httpReq.getAuthType();
	}

	@Override
	public String getContextPath()
	{
		String contextPath = baseUrlGenerator.getContextPath(httpReq);
		return contextPath;
	}

	@Override
	public Cookie[] getCookies()
	{
		return httpReq.getCookies();
	}

	@Override
	public long getDateHeader(String arg0)
	{
		return httpReq.getDateHeader(arg0);
	}

	@Override
	public String getHeader(String arg0)
	{
		return httpReq.getHeader(arg0);
	}

	@Override
	public Enumeration getHeaderNames()
	{
		return httpReq.getHeaderNames();
	}

	@Override
	public Enumeration getHeaders(String arg0)
	{
		return httpReq.getHeaders(arg0);
	}

	@Override
	public int getIntHeader(String arg0)
	{
		return httpReq.getIntHeader(arg0);
	}

	@Override
	public String getMethod()
	{
		return httpReq.getMethod();
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
	public String getPathTranslated()
	{
		return httpReq.getPathTranslated();
	}

	@Override
	public String getQueryString()
	{
        StringBuilder queryString = new StringBuilder();
        String reqQueryString = httpReq.getQueryString();

        if(networkId != null)
        {
            if (reqQueryString == null)
            {
                queryString.append("repositoryId=");
                queryString.append(networkId);
            }
            else
            {
                queryString.append(reqQueryString);
                queryString.append("&repositoryId=");
                queryString.append(networkId);
            }
            return queryString.toString();
        }
        else
        {
            return null;
        }
	}

	@Override
	public String getRemoteUser()
	{
		return httpReq.getRemoteUser();
	}

	@Override
	public String getRequestURI()
	{
		String requestURI = baseUrlGenerator.getRequestURI(httpReq, networkId, operation, id);
		return requestURI;
	}

	@Override
	public StringBuffer getRequestURL()
	{
		return httpReq.getRequestURL();
	}

	@Override
	public String getRequestedSessionId()
	{
		return httpReq.getRequestedSessionId();
	}

	@Override
	public String getServletPath()
	{
		String servletPath = baseUrlGenerator.getServletPath(httpReq);
		return servletPath;
	}

	@Override
	public HttpSession getSession()
	{
		return httpReq.getSession();
	}

	@Override
	public HttpSession getSession(boolean arg0)
	{
		return httpReq.getSession(arg0);
	}

	@Override
	public Principal getUserPrincipal()
	{
		return httpReq.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return httpReq.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return httpReq.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return httpReq.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return httpReq.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String arg0)
	{
		return httpReq.isUserInRole(arg0);
	}
}