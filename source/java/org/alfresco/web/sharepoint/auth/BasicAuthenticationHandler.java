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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
                    logger.debug("Authenticate the user '" + username + "'");
                
                authenticationService.authenticate(username, password.toCharArray());
                
                if (mapper.isSiteMember(request, alfrescoContext, username))
                {
                    user = new User(username, authenticationService.getCurrentTicket(session.getId()), personService.getPerson(username));
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
