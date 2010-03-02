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
package org.alfresco.repo.web.scripts.facebook;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;


/**
 * Facebook Canvas Page Requests
 * 
 * @author davidc
 */
public class FacebookServlet extends FacebookAPIServlet
{
    private static final long serialVersionUID = 2276870692940598426L;

    // Logger
    private static final Log logger = LogFactory.getLog(FacebookServlet.class);

    // Component Dependencies
    protected FacebookService facebookService;


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing facebook canvas ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
        
        FacebookServletRuntime runtime = new FacebookServletRuntime(container, authenticatorFactory, req, res, serverProperties, facebookService);
        runtime.executeScript();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptServlet#initServlet(org.springframework.context.ApplicationContext)
     */
    @Override
    protected void initServlet(ApplicationContext context)
    {
        facebookService = (FacebookService)context.getBean("facebook.service");
    }
    
}
