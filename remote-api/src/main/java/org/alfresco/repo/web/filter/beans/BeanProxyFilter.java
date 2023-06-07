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
package org.alfresco.repo.web.filter.beans;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

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
     * @see jakarta.servlet.Filter#destroy()
     */
    public void destroy()
    {
        this.filter = null;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        this.filter.doFilter(this.context, request, response, chain);
    }
}
