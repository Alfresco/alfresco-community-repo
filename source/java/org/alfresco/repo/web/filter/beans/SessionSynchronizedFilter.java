/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A filter that will use the HttpSession (if it exists) as the monitor for a
 * synchronized block so that only one request per session is processed at any
 * time.
 * 
 * Originally created to avoid having to make 200+ JSF session scoped beans thread
 * safe.
 * 
 * @author Alan Davis
 */
public class SessionSynchronizedFilter implements Filter
{
    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        HttpSession session = null;
        if (request instanceof HttpServletRequest)
        {
            session = ((HttpServletRequest)request).getSession(false);
        }
        if (session != null)
        {
            synchronized(session)
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }
}
