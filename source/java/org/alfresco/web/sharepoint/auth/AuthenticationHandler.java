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
package org.alfresco.web.sharepoint.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.SessionUser;

/**
 * Sharepoint authentication plugin API
 * 
 * @author PavelYur
 */
public interface AuthenticationHandler
{
    public final static String HEADER_AUTHORIZATION = "Authorization";

    public final static String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    public final static String NTLM_START = "NTLM";

    public final static String BASIC_START = "BASIC";

    public final static String USER_SESSION_ATTRIBUTE = "_vtiAuthTicket";

    /**
     * Authenticate user based on information in http request such as Authorization header or else.
     * 
     * @param request
     *            http request
     * @param response
     *            http response
     * @param alfrescoContext
     *            deployment context of alfresco application
     * @param mapper
     *            an object capable of determining which users are site members
     * @return SessionUser information about currently loged in user or null.
     */
    public SessionUser authenticateRequest(HttpServletRequest request, HttpServletResponse response,
            SiteMemberMapper mapper, String alfrescoContext);

    /**
     * Send to user response with http status 401
     * 
     * @param response
     *            http response
     */
    public void forceClientToPromptLogonDetails(HttpServletResponse response);

}