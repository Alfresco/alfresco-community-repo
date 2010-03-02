/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.filter.beans;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An adapter from the servlet filter world into the Spring dependency injected world. Simply looks up a
 * {@link DependencyInjectedFilter} with a configured bean name and delegates the
 * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} call to that. This allows us to swap in and out
 * different implementations for different 'hook points' in web.xml.
 * 
 * @author dward
 */
public class BeanProxyFilter implements Filter
{
    /**
     * Name of the init parameter that carries the proxied bean name 
     */
    private static final String INIT_PARAM_BEAN_NAME = "beanName";
    
    private DependencyInjectedFilter filter;
    private ServletContext context;    
    
    /**
     * Initialize the filter.
     * 
     * @param args
     *            FilterConfig
     * @throws ServletException
     *             the servlet exception
     * @exception ServletException
     */
    public void init(FilterConfig args) throws ServletException
    {
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(args.getServletContext());
        this.filter = (DependencyInjectedFilter)ctx.getBean(args.getInitParameter(INIT_PARAM_BEAN_NAME));
        this.context = args.getServletContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        this.filter = null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        this.filter.doFilter(this.context, request, response, chain);
    }
}
