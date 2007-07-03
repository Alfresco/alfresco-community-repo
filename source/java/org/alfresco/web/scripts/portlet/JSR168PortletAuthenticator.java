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
package org.alfresco.web.scripts.portlet;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Portlet authenticator
 * 
 * @author davidc
 */
public class JSR168PortletAuthenticator implements WebScriptPortletAuthenticator
{
    // Logger
    private static final Log logger = LogFactory.getLog(JSR168PortletAuthenticator.class);

    // dependencies
    private AuthenticationService authenticationService;
    
    /**
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.WebScriptPortletAuthenticator#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean, javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public boolean authenticate(RequiredAuthentication required, boolean isGuest, RenderRequest req, RenderResponse res)
    {
        // first look for the username key in the session - we add this by hand for some portals
        // when the WebScriptPortletRequest is created
        String portalUser = (String)req.getPortletSession().getAttribute(WebScriptPortletRequest.ALFPORTLETUSERNAME);
        if (portalUser == null)
        {
            portalUser = req.getRemoteUser();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("JSR-168 Remote user: " + portalUser);
        }

        if (isGuest || portalUser == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating as Guest");
            
            // authenticate as guest
            AuthenticationUtil.setCurrentUser(AuthenticationUtil.getGuestUserName());
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating as user " + portalUser);
            
            if (!authenticationService.authenticationExists(portalUser))
            {
                throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "User " + portalUser + " is not a known Alfresco user");
            }
            AuthenticationUtil.setCurrentUser(portalUser);
        }
        
        return true;
    }

}
