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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Stas Sokolovsky
 * 
 * Servlet filter responsible for setting a fallback default locale for ALL requests. 
 */
public class GlobalLocalizationFilter implements Filter
{
    /**
     * Run the filter
     * 
     * @param request ServletRequest
     * @param response ServletResponse
     * @param chain FilterChain
     * @exception IOException
     * @exception ServletException
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        setLanguageFromRequestHeader(httpRequest);

        // continue filter chaining
        chain.doFilter(request, response);

    }

    /**
     * Apply Client and Repository language locale based on the 'Accept-Language' request header
     *
     * @param request HttpServletRequest
     */
    public void setLanguageFromRequestHeader(HttpServletRequest req)
    {
        Locale locale = null;

        String acceptLang = req.getHeader("Accept-Language");
        if (acceptLang != null && acceptLang.length() > 0)
        {
            StringTokenizer tokenizer = new StringTokenizer(acceptLang, ",; ");
            // get language and convert to java locale format
            String language = tokenizer.nextToken().replace('-', '_');
            locale = I18NUtil.parseLocale(language);
            I18NUtil.setLocale(locale);
        }
        else
        {
            I18NUtil.setLocale(Locale.getDefault());
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Nothing to do        
    }

    public void destroy()
    {
        // Nothing to do
    }
}
