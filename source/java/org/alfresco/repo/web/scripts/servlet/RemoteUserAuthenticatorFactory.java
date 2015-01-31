/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.web.auth.AuthenticationListener;
import org.alfresco.repo.web.auth.TicketCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Authenticator to provide Remote User based Header authentication dropping back to Basic Auth otherwise. 
 * Statelessly authenticating via a secure header now does not require a Session so can be used with
 * request-level load balancers which was not previously possible.
 * <p>
 * @see web-scripts-application-context.xml and web.xml - bean id 'webscripts.authenticator.remoteuser'
 * <p>
 * This authenticator can be bound to /service and does not require /wcservice (Session) mapping.
 * 
 * @since 5.1
 * @author Kevin Roast
 */
public class RemoteUserAuthenticatorFactory extends BasicHttpAuthenticatorFactory
{
    private static Log logger = LogFactory.getLog(RemoteUserAuthenticatorFactory.class);
    
    protected RemoteUserMapper remoteUserMapper;
    protected AuthenticationComponent authenticationComponent;
    
    public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper)
    {
        this.remoteUserMapper = remoteUserMapper;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    @Override
    public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res)
    {
        return new RemoteUserAuthenticator(req, res, this.listener);
    }

    /**
     * Remote User authenticator - adds header authentication onto Basic Auth. Stateless does not require Session.
     * 
     * @author Kevin Roast
     */
    public class RemoteUserAuthenticator extends BasicHttpAuthenticator
    {
        public RemoteUserAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res, AuthenticationListener listener)
        {
            super(req, res, listener);
        }
        
        @Override
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            // retrieve the remote user if configured and available - authenticate that user directly
            final String userId = getRemoteUser();
            if (userId != null)
            {
                authenticationComponent.setCurrentUser(userId);
                listener.userAuthenticated(new TicketCredentials(authenticationService.getCurrentTicket()));
                return true;
            }
            else
            {
                return super.authenticate(required, isGuest);
            }
        }
        
        /**
         * Retrieve the remote user from servlet request header when using a secure connection.
         * The RemoteUserMapper bean must be active and configured.
         * 
         * @return remote user ID or null if not active or found
         */
        protected String getRemoteUser()
        {
            String userId = null;
            
            // If the remote user mapper is configured, we may be able to map in an externally authenticated user
            if (remoteUserMapper != null &&
                    (!(remoteUserMapper instanceof ActivateableBean) || ((ActivateableBean) remoteUserMapper).isActive()))
            {
                userId = remoteUserMapper.getRemoteUser(this.servletReq.getHttpServletRequest());
            }
            
            if (logger.isDebugEnabled())
            {
                if (userId == null)
                {
                    logger.debug("No external user ID in request.");
                }
                else
                {
                    logger.debug("Extracted external user ID from request: " + userId);
                }
            }
            
            return userId;
        }
    }
}
