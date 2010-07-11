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
import javax.servlet.http.HttpSession;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.codec.binary.Base64;

/**
 * <p>BASIC web authentication implementation.</p>
 * 
 * @author PavelYur
 *
 */
public class BasicAuthenticationHandler extends AbstractAuthenticationHandler
{
    /* (non-Javadoc)
     * @see org.alfresco.web.vti.auth.AuthenticationHandler#authenticateRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.alfresco.web.vti.auth.SiteMemberMapper, java.lang.String)
     */
    public SessionUser authenticateRequest(HttpServletRequest request, HttpServletResponse response,
            SiteMemberMapper mapper, String alfrescoContext)
    {
        SessionUser user = null;
        
        String authHdr = request.getHeader(HEADER_AUTHORIZATION);
        HttpSession session = request.getSession();
        
        if (authHdr != null && authHdr.length() > 5 && authHdr.substring(0, 5).equalsIgnoreCase(BASIC_START))
        {
            String basicAuth = new String(Base64.decodeBase64(authHdr.substring(5).getBytes()));
            String username = null;
            String password = null;

            int pos = basicAuth.indexOf(":");
            if (pos != -1)
            {
                username = basicAuth.substring(0, pos);
                password = basicAuth.substring(pos + 1);
            }
            else
            {
                username = basicAuth;
                password = "";
            }

            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("Authenticating user '" + username + "'");
                
                authenticationService.authenticate(username, password.toCharArray());
                
                // Normalize the user ID taking into account case sensitivity settings
                username = authenticationService.getCurrentUserName();
                
                if (logger.isDebugEnabled())
                    logger.debug("Authenticated user '" + username + "'");

                if (mapper.isSiteMember(request, alfrescoContext, username))
                {
                    user = new User(username, authenticationService.getCurrentTicket(), personService.getPerson(username));
                    if (session != null)
                        session.setAttribute(USER_SESSION_ATTRIBUTE, user);
                }
            }
            catch (AuthenticationException ex)
            {
                // Do nothing, user object will be null
            }
        }

        return user;
    }

    
    @Override
    public String getWWWAuthenticate()
    {
        return "BASIC realm=\"Alfresco Server\"";
    }
}
