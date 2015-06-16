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
package org.alfresco.web.scripts.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet filter to add a 'Cache-Control' HTTP header to a response.
 * The Cache-Control header is set to a max-age value by a configurable setting
 * in the 'expires' init parameters - values are in days.
 * 
 * WebScripts or other servlets that happen to match the response type
 * configured for the filter (e.g. "*.js") should override cache settings
 * as required.
 * 
 * @author Kevin Roast
 */
public class StaticAssetCacheFilter implements Filter
{
    private static final long DAY_S = 60L*60L*24L;          // 1 day in seconds
    private static final long DEFAULT_30DAYS = 30L;         // default of 30 days if not configured
    
    private long expire = DAY_S * DEFAULT_30DAYS;           // initially set to default value of 30 days
    
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        String expireParam = config.getInitParameter("expires");
        if (expireParam != null)
        {
            this.expire = Long.parseLong(expireParam) * DAY_S;
        }
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException
    {
        ((HttpServletResponse)res).setHeader("Cache-Control", "must-revalidate, max-age=" + Long.toString(this.expire));
        chain.doFilter(req, res);
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        this.expire = DAY_S * DEFAULT_30DAYS;
    }
}