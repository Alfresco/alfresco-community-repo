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
package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A general interface for servlet-based authentication. Allows code to be shared by Web Client, WebDAV and Sharepoint
 * authentication classes.
 * 
 * @author dward
 */
public interface AuthenticationDriver
{
    /**
     * Authenticate user based on information in http request such as Authorization header or cached session
     * information.
     * 
     * @param context
     *            the context
     * @param request
     *            http request
     * @param response
     *            http response
     * @return <code>true</code> if authentication was successful
     * @throws IOException
     * @throws ServletException
     */
    public boolean authenticateRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    /**
     * Send a status 401 response that will restart the log in handshake.
     * 
     * @param context
     *            the context
     * @param request
     *            http request
     * @param response
     *            http response
     * @throws IOException
     */
    public void restartLoginChallenge(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException;
}