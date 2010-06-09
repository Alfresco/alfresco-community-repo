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
package org.alfresco.repo.web.scripts.portlet;

import java.io.IOException;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.repo.web.scripts.servlet.AuthenticatorServlet;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.portlet.PortletAuthenticatorFactory;


/**
 * Portlet authenticator which synchronizes with the Alfresco Web Client authentication
 * 
 * @author davidc
 * @author dward
 */
public class WebClientPortletAuthenticatorFactory implements PortletAuthenticatorFactory
{
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.PortletAuthenticatorFactory#create(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public Authenticator create(RenderRequest req, RenderResponse res)
    {
        return new WebClientPortletAuthenticator(req, res);
    }


    public class WebClientPortletAuthenticator implements Authenticator
    {
        // dependencies
        private RenderRequest req;
        private RenderResponse res;
        
        /**
         * Construct
         * 
         * @param authenticationService
         * @param req
         * @param res
         */
        public WebClientPortletAuthenticator(RenderRequest req, RenderResponse res)
        {
            this.req = req;
            this.res = res;
        }

        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#authenticate(org.alfresco.web.scripts.Description.RequiredAuthentication, boolean)
         */
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
        {
            PortletSession session = req.getPortletSession();
            req.setAttribute(AuthenticatorServlet.ATTR_REQUIRED_AUTH, required);
            req.setAttribute(AuthenticatorServlet.ATTR_IS_GUEST, isGuest);
            PortletContext context = session.getPortletContext();
            try
            {
                context.getNamedDispatcher(AuthenticatorServlet.SERVLET_NAME).include(req, res);
            }
            catch (PortletException e)
            {
                throw new WebScriptException("Failed to authenticate", e);
            }
            catch (IOException e)
            {
                throw new WebScriptException("Failed to authenticate", e);
            }
            AuthenticationStatus status = (AuthenticationStatus) req.getAttribute(AuthenticatorServlet.ATTR_AUTH_STATUS);
            return !(status == null || status == AuthenticationStatus.Failure);
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
         */
        public boolean emptyCredentials()
        {
            // Ticket - based authentication not supported
            return true;
        }        
    }
}