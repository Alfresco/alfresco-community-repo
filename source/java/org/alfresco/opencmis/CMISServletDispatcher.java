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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

/**
 * Dispatches OpenCMIS requests to a servlet e.g. the OpenCMIS AtomPub servlet.
 * 
 * @author steveglover
 *
 */
public abstract class CMISServletDispatcher implements CMISDispatcher
{
    protected CmisServiceFactory cmisServiceFactory;
    protected HttpServlet servlet;
	protected CMISDispatcherRegistry registry;
	protected String serviceName;
	protected BaseUrlGenerator baseUrlGenerator;
	
	public void setBaseUrlGenerator(BaseUrlGenerator baseUrlGenerator)
	{
		this.baseUrlGenerator = baseUrlGenerator;
	}

	public void setRegistry(CMISDispatcherRegistry registry)
	{
		this.registry = registry;
	}

	public void setCmisServiceFactory(CmisServiceFactory cmisServiceFactory)
    {
        this.cmisServiceFactory = cmisServiceFactory;
    }
	
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	/*
	 *  Implement getBinding to provide the appropriate CMIS binding.
	 */
    protected abstract Binding getBinding();
    
	/*
	 *  Implement getServlet to provide the appropriate servlet implementation.
	 */
	protected abstract HttpServlet getServlet();

	protected Object getServletAttribute(String attrName)
	{
		if(attrName.equals(CmisRepositoryContextListener.SERVICES_FACTORY))
		{
			return cmisServiceFactory;
		}

		return null;
	}
	
    protected ServletConfig getServletConfig()
    {
    	ServletConfig config = new CMISServletConfig();
    	return config;
    }
    
	public void init()
	{
		try
		{
			// fake the CMIS AtomPub servlet
			ServletConfig config = getServletConfig();
	    	this.servlet = getServlet();
	    	servlet.init(config);
		}
		catch(ServletException e)
		{
			throw new AlfrescoRuntimeException("Failed to initialise CMIS webscript", e);
		}
	}

	protected CMISHttpServletRequest getHttpRequest(WebScriptRequest req)
	{
		String serviceName = getServiceName();
		CMISHttpServletRequest httpReqWrapper = new CMISHttpServletRequest(req, serviceName, baseUrlGenerator, getBinding());
    	return httpReqWrapper;
	}

	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
	{
		try
		{
	        HttpServletResponse httpResp = WebScriptServletRuntime.getHttpServletResponse(res);

			// fake a servlet request. Note that getPathInfo is the only method that the servlet uses,
			// hence the other methods are not implemented.
	    	CMISHttpServletRequest httpReqWrapper = getHttpRequest(req);

	    	servlet.service(httpReqWrapper, httpResp);
		}
		catch(ServletException e)
		{
			throw new AlfrescoRuntimeException("", e);
		}
	}

    /**
     * Fake a CMIS servlet config.
     * 
     * @author steveglover
     *
     */
	@SuppressWarnings("rawtypes")
    private class CMISServletConfig implements ServletConfig
    {
		private List parameterNames = new ArrayList();

    	@SuppressWarnings("unchecked")
		CMISServletConfig()
    	{
    		parameterNames.add(CmisAtomPubServlet.PARAM_CALL_CONTEXT_HANDLER);
		}

		@Override
		public String getInitParameter(String arg0)
		{
			if(arg0.equals(CmisAtomPubServlet.PARAM_CALL_CONTEXT_HANDLER))
			{
				return "org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler";
			}
			return null;
		}

		@Override
		public Enumeration getInitParameterNames()
		{
			final Iterator it = parameterNames.iterator();

			Enumeration e = new Enumeration()
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
			return e;
		}

		// fake a servlet context. Note that getAttribute is the only method that the servlet uses,
		// hence the other methods are not implemented.
		@Override
		public ServletContext getServletContext()
		{
			return new ServletContext()
			{

				@Override
				public Object getAttribute(String arg0)
				{
					return getServletAttribute(arg0);
				}

				@Override
				public Enumeration getAttributeNames()
				{
					return null;
				}

				@Override
				public ServletContext getContext(String arg0)
				{
					return null;
				}

				@Override
				public String getInitParameter(String arg0)
				{
					return null;
				}

				@Override
				public Enumeration getInitParameterNames()
				{
					return null;
				}

				@Override
				public int getMajorVersion()
				{
					return 0;
				}

				@Override
				public String getMimeType(String arg0)
				{
					return null;
				}

				@Override
				public int getMinorVersion()
				{
					return 0;
				}

				@Override
				public RequestDispatcher getNamedDispatcher(String arg0)
				{
					return null;
				}

				@Override
				public String getRealPath(String arg0)
				{
					return null;
				}

				@Override
				public RequestDispatcher getRequestDispatcher(String arg0)
				{
					return null;
				}

				@Override
				public URL getResource(String arg0) throws MalformedURLException
				{
					return null;
				}

				@Override
				public InputStream getResourceAsStream(String arg0)
				{
					return null;
				}

				@Override
				public Set getResourcePaths(String arg0)
				{
					return null;
				}

				@Override
				public String getServerInfo()
				{
					return null;
				}

				@Override
				public Servlet getServlet(String arg0) throws ServletException
				{
					return null;
				}

				@Override
				public String getServletContextName()
				{
					return null;
				}

				@Override
				public Enumeration getServletNames()
				{
					return null;
				}

				@Override
				public Enumeration getServlets()
				{
					return null;
				}

				@Override
				public void log(String arg0)
				{
				}

				@Override
				public void log(Exception arg0, String arg1)
				{
				}

				@Override
				public void log(String arg0, Throwable arg1)
				{
				}

				@Override
				public void removeAttribute(String arg0)
				{
				}

				@Override
				public void setAttribute(String arg0, Object arg1)
				{
				}
			};
		}

		@Override
		public String getServletName()
		{
			return "OpenCMIS";
		}
    }
}
