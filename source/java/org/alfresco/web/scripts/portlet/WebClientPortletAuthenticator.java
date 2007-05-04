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

import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.scripts.WebScriptContext;
import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Portlet authenticator which synchronizes with the Alfresco Web Client authentication
 * 
 * @author davidc
 */
public class WebClientPortletAuthenticator implements WebScriptPortletAuthenticator
{
    // Logger
    private static final Log logger = LogFactory.getLog(WebClientPortletAuthenticator.class);

    // dependencies
    private AuthenticationService authenticationService;
    private WebScriptContext scriptContext;

    
    /**
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @param scriptContext
     */
    public void setScriptContext(WebScriptContext scriptContext)
    {
        this.scriptContext = scriptContext;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.WebScriptPortletAuthenticator#authenticate(org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication, boolean, javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void authenticate(RequiredAuthentication required, boolean isGuest, RenderRequest req, RenderResponse res)
    {
        PortletSession session = req.getPortletSession();
        String portalUser = req.getRemoteUser();
        
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

            if (logger.isDebugEnabled())
                logger.debug("Setting Web Client authentication context for guest");
            
            createWebClientUser(session);
            removeSessionInvalidated(session);
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Authenticating as user " + portalUser);
            
            AuthenticationUtil.setCurrentUser(portalUser);

            // determine if Web Client context needs to be updated
            User user = getWebClientUser(session);
            if (user == null || !portalUser.equals(user.getUserName()))
            {
                if (logger.isDebugEnabled())
                    logger.debug("Setting Web Client authentication context for user " + portalUser);
                
                createWebClientUser(session);
                removeSessionInvalidated(session);
            }
        }
    }

    /**
     * Helper.  Remove Web Client session invalidated flag
     * 
     * @param session
     */
    private void removeSessionInvalidated(PortletSession session)
    {
        session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED, PortletSession.APPLICATION_SCOPE);
    }
    
    /**
     * Helper.  Create Web Client session user
     * 
     * @param session
     */
    private void createWebClientUser(PortletSession session)
    {
        NodeRef personRef = scriptContext.getPerson();
        User user = new User(authenticationService.getCurrentUserName(), authenticationService.getCurrentTicket(), personRef);
        NodeRef homeRef = scriptContext.getUserHome(personRef);
        if (homeRef != null)
        {
            user.setHomeSpaceId(homeRef.getId());
        }
        session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user, PortletSession.APPLICATION_SCOPE);
    }
    
    /**
     * Helper.  Get Web Client session user
     * 
     * @param session
     * @return
     */
    private User getWebClientUser(PortletSession session)
    {
        return (User)session.getAttribute(AuthenticationHelper.AUTHENTICATION_USER, PortletSession.APPLICATION_SCOPE);
    }
    
}
