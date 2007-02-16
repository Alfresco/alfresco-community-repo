/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.webservice.authentication;

import java.rmi.RemoteException;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.service.cmr.security.AuthenticationService;
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
    public void endSession(String ticket) throws RemoteException, AuthenticationFault
    {
        try
        {
            if (ticket != null)
            {
                this.authenticationService.validate(ticket);
                this.authenticationService.invalidateTicket(ticket);
                this.authenticationService.clearCurrentSecurityContext();
    
                if (logger.isDebugEnabled())
                {
                    logger.debug("Session ended for ticket '" + ticket + "'");
                }
            }
        } 
        catch (Throwable e)
        {            
            throw new AuthenticationFault(0, e.getMessage());
        }
    }
}
