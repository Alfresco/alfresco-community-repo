/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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