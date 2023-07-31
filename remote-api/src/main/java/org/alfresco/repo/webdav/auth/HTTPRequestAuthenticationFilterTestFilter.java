/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This filter simply sets a request header to test the SSO filters
 */
public class HTTPRequestAuthenticationFilterTestFilter implements Filter
{

    private String httpServletRequestAuthHeaderName;

    private String userName;

    /**
     * Initialize the filter
     * 
     * @param config
     *            FitlerConfig
     * @exception ServletException
     */
    public void init(FilterConfig config) throws ServletException
    {
        httpServletRequestAuthHeaderName = config.getInitParameter("httpServletRequestAuthHeaderName");
        if (httpServletRequestAuthHeaderName == null)
        {
            httpServletRequestAuthHeaderName = "x-user";
        }

        userName = config.getInitParameter("userName");
        if (userName == null)
        {
            userName = "guest";
        }
    }

    /**
     * Run the authentication filter
     * 
     * @param req
     *            ServletRequest
     * @param resp
     *            ServletResponse
     * @param chain
     *            FilterChain
     * @exception ServletException
     * @exception IOException
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        // Assume it's an HTTP request

        HttpServletRequest httpReq = (HttpServletRequest) req;
        chain.doFilter(getProxy(httpReq), resp);
    }

    /**
     * Cleanup filter resources
     */
    public void destroy()
    {
        // Nothing to do
    }

    private HttpServletRequest getProxy(HttpServletRequest req)
    {
        HttpServletRequest proxy = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(), new Class[] { HttpServletRequest.class },
                new Handler(req));
        return proxy;
    }

    class Handler implements InvocationHandler
    {
        HttpServletRequest httpReq;

        Handler(HttpServletRequest httpReq)
        {
            this.httpReq = httpReq;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("getHeader"))
            {
                Object arg0 = args[0];
                if (arg0 != null)
                {
                    if (arg0 instanceof String)
                    {
                        String headerName = (String) arg0;
                        if (headerName.equals(httpServletRequestAuthHeaderName))
                        {
                            return userName;
                        }
                    }
                }
            }
            return method.invoke(httpReq, args);
        }

    }
}
