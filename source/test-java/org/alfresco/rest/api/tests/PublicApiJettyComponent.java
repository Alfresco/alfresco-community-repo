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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.web.util.AbstractJettyComponent;
import org.alfresco.rest.api.PublicApiWebScriptServlet;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Manages an embedded jetty server, hooking it up to the repository spring context and providing 
 * authenticated, tenant-based access through the tenant servlet.
 * 
 * @author steveglover
 *
 */
public class PublicApiJettyComponent extends AbstractJettyComponent
{
	public PublicApiJettyComponent(int port, String contextPath, String[] configLocations, String[] classLocations)
	{
		super(port, contextPath, configLocations, classLocations);
	}
	
	@Override
	protected void configureWebAppContext(WebAppContext webAppContext)
	{
//		ServletContext servletContext = webAppContext.getServletContext();

	    // the tenant servlet with alfresco managed authentication
	    ServletHolder servletHolder = new ServletHolder(PublicApiWebScriptServlet.class);
	    servletHolder.setInitParameter("authenticator", "publicapi.authenticator");
	    webAppContext.addServlet(servletHolder, "/" + publicApiServletName + "/*");
	    
//	    DependencyInjectedFilter apiFilter = (DependencyInjectedFilter)getApplicationContext().getBean("publicAPICMISFilter");
//	    BeanProxyFilter filter = new BeanProxyFilter(servletContext, apiFilter);
//	    FilterHolder filterHolder = new FilterHolder(filter);
//	    webAppContext.addFilter(filterHolder, "/" + publicApiServletName + "/*", null);

	    // the tenant servlet with alfresco managed authentication
	    servletHolder = new ServletHolder(CmisAtomPubServlet.class);
	    servletHolder.setInitParameter("callContextHandler", "org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler");
	    webAppContext.addServlet(servletHolder, "/cmisatom/*");
	}
	
//	private static class BeanProxyFilter implements Filter
//	{
//	    private DependencyInjectedFilter filter;
//	    private ServletContext context;    
//	    
//	    private BeanProxyFilter(ServletContext context, DependencyInjectedFilter filter)
//	    {
//	    	this.context = context;
//	    	this.filter = filter;
//	    }
//
//	    /**
//	     * Initialize the filter.
//	     * 
//	     * @param args
//	     *            FilterConfig
//	     * @throws ServletException
//	     *             the servlet exception
//	     * @exception ServletException
//	     */
//	    public void init(FilterConfig args) throws ServletException
//	    {
//	    }
//
//	    /* (non-Javadoc)
//	     * @see javax.servlet.Filter#destroy()
//	     */
//	    public void destroy()
//	    {
//	        this.filter = null;
//	    }
//
//	    /* (non-Javadoc)
//	     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
//	     */
//	    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
//	            ServletException
//	    {
//	        this.filter.doFilter(this.context, request, response, chain);
//	    }
//	}
}
