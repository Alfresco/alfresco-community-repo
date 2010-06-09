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
package org.alfresco.repo.web.scripts.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;

/**
 * This servlet serves as a useful 'subroutine' for portlets, which using their request dispatcher, can go 'through the
 * looking glass' to this servlet and use the standard Alfresco servlet api-based authentication mechanisms.
 * 
 * @author dward
 */
public class AuthenticatorServlet extends HttpServlet
{
    public static final String SERVLET_NAME = "authenticatorServlet";
    public static final String ATTR_IS_GUEST = "_alf_isGuest";
    public static final String ATTR_REQUIRED_AUTH = "_alf_requiredAuth";
    public static final String ATTR_AUTH_STATUS = "_alf_authStatus";

    private static final long serialVersionUID = 5657140557243797744L;

    private static final Log logger = LogFactory.getLog(AuthenticatorServlet.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        ServletContext context = getServletContext();
        boolean isGuest = (Boolean) req.getAttribute(ATTR_IS_GUEST);
        RequiredAuthentication required = (RequiredAuthentication) req.getAttribute(ATTR_REQUIRED_AUTH);
        AuthenticationStatus status;
        if (isGuest && RequiredAuthentication.guest == required)
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating as Guest");

            status = AuthenticationHelper.authenticate(context, req, res, true);
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating session");

            status = AuthenticationHelper.authenticate(context, req, res, false, false);
        }
        req.setAttribute(ATTR_AUTH_STATUS, status);
    }
}
