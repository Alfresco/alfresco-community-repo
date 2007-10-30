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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts.facebook;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.scripts.WebScriptRuntime;
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
    // Logger
    private static final Log logger = LogFactory.getLog(FacebookServlet.class);

    // Component Dependencies
    protected FacebookService facebookService;


    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing facebook canvas ("  + req.getMethod() + ") " + req.getRequestURL() + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
        
        WebScriptRuntime runtime = new FacebookServletRuntime(registry, serviceRegistry, authenticator, req, res, serverConfig, facebookService);
        runtime.executeScript();
    }
        
    @Override
    protected void initServlet(ApplicationContext context)
    {
        facebookService = (FacebookService)context.getBean("facebook.service");
    }
    
}
