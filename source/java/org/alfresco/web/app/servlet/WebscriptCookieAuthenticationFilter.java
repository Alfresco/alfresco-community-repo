/*
 * #%L
 * Alfresco Repository WAR Community
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
package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * WebScript aware Authentication Filter. Directly handles login script calls, allowing Surf to establish a cookie
 * for a manual login, rather than the usual stateless ticket based logins.
 * <p>
 * This functionality has been extracted from the WebScriptSSOAuthenticationFilter so that they can work independently.
 * 
 * @author Gethin James
 */
public class WebscriptCookieAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter
{
    private static final Log logger = LogFactory.getLog(WebscriptCookieAuthenticationFilter.class);
    private static final String API_LOGIN = "/api/login";
    
    public WebscriptCookieAuthenticationFilter()
    {
        setUserAttributeName(AuthenticationHelper.AUTHENTICATION_USER);
    }

    
    @Override
    public void doFilter(ServletContext context, ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException
    {
        // Get the HTTP request/response
        HttpServletRequest req = (HttpServletRequest)sreq;
        HttpServletResponse res = (HttpServletResponse)sresp;
        
        // Allow propagation of manual logins to the session user
        if (API_LOGIN.equals(req.getPathInfo()) && req.getMethod().equalsIgnoreCase("POST"))
        {
            if (handleLoginForm(req, res));
            {
               // Establish the session locale using request headers rather than web client preferences
               AuthenticationHelper.setupThread(context, req, res, false);
            }
        }
        else
        {
            chain.doFilter(sreq, sresp);
        }
    }

    @Override
    protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef)
    {
        // Create a web client user object
        User user = new User( userName, ticket, personNode);
        user.setHomeSpaceId( homeSpaceRef.getId());
        
        return user;
    }
    
    @Override
    protected Log getLogger()
    {
        return logger;
    }

}
