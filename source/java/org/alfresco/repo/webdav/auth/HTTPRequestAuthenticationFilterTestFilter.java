/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
