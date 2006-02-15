/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.authentication;

import org.alfresco.service.cmr.security.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService
{
    MutableAuthenticationDao authenticationDao;

    AuthenticationComponent authenticationComponent;
    
    TicketComponent ticketComponent;
    
    public AuthenticationServiceImpl()
    {
        super();
    }

    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }

    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        authenticationDao.createUser(userName, password);
    }

    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        authenticationDao.updateUser(userName, newPassword);
    }

    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        authenticationDao.updateUser(userName, newPassword);
    }

    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        authenticationDao.deleteUser(userName);
    }

    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        return authenticationDao.getEnabled(userName);
    }

    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        authenticationDao.setEnabled(userName, enabled);
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        try
        {
           authenticationComponent.authenticate(userName, password);
        }
        catch(AuthenticationException ae)
        {
            clearCurrentSecurityContext();
            throw ae;
        }
    }

    public String getCurrentUserName() throws AuthenticationException
    {
        return authenticationComponent.getCurrentUserName();
    }

    public void invalidateUserSession(String userName) throws AuthenticationException
    {
        ticketComponent.invalidateTicketByUser(userName);
    }

    public void invalidateTicket(String ticket) throws AuthenticationException
    {
        ticketComponent.invalidateTicketById(ticket);
    }

    public void validate(String ticket) throws AuthenticationException
    {
        try
        {
           authenticationComponent.setCurrentUser(ticketComponent.validateTicket(ticket));
        }
           catch(AuthenticationException ae)
           {
               clearCurrentSecurityContext();
               throw ae;
           } 
    }

    public String getCurrentTicket()
    {
        return ticketComponent.getTicket(getCurrentUserName());
    }

    public void clearCurrentSecurityContext()
    {
        authenticationComponent.clearCurrentSecurityContext();
    }

    public boolean isCurrentUserTheSystemUser()
    {
        String userName = getCurrentUserName();
        if ((userName != null) && userName.equals(authenticationComponent.getSystemUserName()))
        {
            return true;
        }
        return false;
    }

    public void authenticateAsGuest() throws AuthenticationException
    {
        authenticationComponent.setGuestUserAsCurrentUser();
    }

    
}
