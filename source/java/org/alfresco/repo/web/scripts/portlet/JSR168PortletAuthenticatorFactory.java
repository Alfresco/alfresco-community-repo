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

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.portlet.PortletAuthenticatorFactory;
import org.springframework.extensions.webscripts.portlet.WebScriptPortletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Portlet authenticator
 * 
 * @author davidc
 */
public class JSR168PortletAuthenticatorFactory implements PortletAuthenticatorFactory
{
    // Logger
    private static final Log logger = LogFactory.getLog(JSR168PortletAuthenticatorFactory.class);

    // dependencies
    private AuthenticationService unprotAuthenticationService;
    private TransactionService txnService;    
    
    /**
     * @param authenticationService
     */
    public void setUnprotAuthenticationService(AuthenticationService authenticationService)
    {
        this.unprotAuthenticationService = authenticationService;
    }

    /**
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.txnService = transactionService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.portlet.PortletAuthenticatorFactory#create(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public Authenticator create(RenderRequest req, RenderResponse res)
    {
        return new JSR168PortletAuthenticator(req, res);
    }

    
    /**
     * Portlet authenticator
     * 
     * @author davidc
     */
    public class JSR168PortletAuthenticator implements Authenticator
    {
        // dependencies
        private RenderRequest req;
        
        /**
         * Construct
         * 
         * @param authenticationService
         * @param req
         * @param res
         */
        public JSR168PortletAuthenticator(RenderRequest req, RenderResponse res)
        {
            this.req = req;
        }
        
        /*(non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#authenticate(org.alfresco.web.scripts.Description.RequiredAuthentication, boolean)
         */
        public boolean authenticate(RequiredAuthentication required, boolean isGuest)
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
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Authenticating as user " + portalUser);

                UserTransaction txn = null;
                try
                {
                	txn = txnService.getUserTransaction();
                	txn.begin();

                	if (!unprotAuthenticationService.authenticationExists(portalUser))
                	{
                		throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "User " + portalUser + " is not a known Alfresco user");
                	}
                	AuthenticationUtil.setFullyAuthenticatedUser(portalUser);
                }
                catch (Throwable err)
                {
                	throw new AlfrescoRuntimeException("Error authenticating user: " + portalUser, err);
                }
                finally
                {
                	try
                	{
                		if (txn != null)
                		{
                			txn.rollback();
                		}
                	}
                	catch (Exception tex)
                	{
                		// nothing useful we can do with this
                	}
                }
            }
            
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
         */
        public boolean emptyCredentials()
        {
            String portalUser = (String)req.getPortletSession().getAttribute(WebScriptPortletRequest.ALFPORTLETUSERNAME);
            if (portalUser == null)
            {
                portalUser = req.getRemoteUser();
            }
            return (portalUser == null);
        }
    }

}
