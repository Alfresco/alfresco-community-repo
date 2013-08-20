/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.authentication;

import java.rmi.RemoteException;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the AuthenticationService. The WSDL for this
 * service can be accessed from
 * http://localhost:8080/alfresco/wsdl/authentication-service.wsdl
 * 
 * @author gavinc
 */
public class AuthenticationWebService implements AuthenticationServiceSoapPort
{
    private static Log logger = LogFactory.getLog(AuthenticationWebService.class);

    private AuthenticationService authenticationService;
    
    private AuthenticationComponent authenticationComponent;

    /**
     * Sets the AuthenticationService instance to use
     * 
     * @param authenticationSvc
     *            The AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationSvc)
    {
        this.authenticationService = authenticationSvc;
    }
    
    /**
     * Set the atuthentication component
     * 
     * @param authenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) 
    {
		this.authenticationComponent = authenticationComponent;
	}

    /**
     * @see org.alfresco.repo.webservice.authentication.AuthenticationServiceSoapPort#startSession(java.lang.String,
     *      java.lang.String)
     */
    public AuthenticationResult startSession(String username, String password)
            throws RemoteException, AuthenticationFault
    {
        try
        {
            this.authenticationService.authenticate(username, password.toCharArray());
            String ticket = this.authenticationService.getCurrentTicket();

            if (logger.isDebugEnabled())
            {
                logger.debug("Issued ticket '" + ticket + "' for '" + username + "'");
            }
            
            return new AuthenticationResult(username, ticket, Utils.getSessionId());
        } 
        catch (AuthenticationException ae)
        {
            ae.printStackTrace();
            throw new AuthenticationFault(100, ae.getMessage());
        } 
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new AuthenticationFault(0, e.getMessage());
        }
    }

    /**
     * @see org.alfresco.repo.webservice.authentication.AuthenticationServiceSoapPort#endSession()
     */
    public void endSession(final String ticket) throws RemoteException, AuthenticationFault
    {
        try
        {
            if (ticket != null)
            {
            	RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                    	AuthenticationWebService.this.authenticationComponent.setSystemUserAsCurrentUser();
                    	AuthenticationWebService.this.authenticationService.invalidateTicket(ticket);
                    	AuthenticationWebService.this.authenticationService.clearCurrentSecurityContext();
    
		                if (logger.isDebugEnabled())
		                {
		                    logger.debug("Session ended for ticket '" + ticket + "'");
		                }
		                
		                return null;
                    }
                };
                Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
                Utils.invalidateSession();
            }
        } 
        catch (Throwable e)
        {          
            throw new AuthenticationFault(0, e.getMessage());
        }
    }
}
