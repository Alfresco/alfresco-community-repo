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

import java.util.Collections;
import java.util.Set;

import org.alfresco.service.cmr.security.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService
{
    MutableAuthenticationDao authenticationDao;

    AuthenticationComponent authenticationComponent;
    
    TicketComponent ticketComponent;
    
    private String domain;
    
    private boolean allowsUserCreation = true;
    
    private boolean allowsUserDeletion = true;
    
    private boolean allowsUserPasswordChange = true;
    
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
        String currentUser = AuthenticationUtil.getCurrentUserName();
        try
        {
            authenticate(userName, oldPassword);
        }
        finally
        {
            AuthenticationUtil.setCurrentUser(currentUser);
        }
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
    
    public boolean authenticationExists(String userName)
    {
        return authenticationDao.userExists(userName);
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
    
    public boolean guestUserAuthenticationAllowed()
    {
        return authenticationComponent.guestUserAuthenticationAllowed();
    }

    public boolean getAllowsUserCreation()
    {
        return allowsUserCreation;
    }

    public void setAllowsUserCreation(boolean allowsUserCreation)
    {
        this.allowsUserCreation = allowsUserCreation;
    }

    public boolean getAllowsUserDeletion()
    {
        return allowsUserDeletion;
    }

    public void setAllowsUserDeletion(boolean allowsUserDeletion)
    {
        this.allowsUserDeletion = allowsUserDeletion;
    }

    public boolean getAllowsUserPasswordChange()
    {
        return allowsUserPasswordChange;
    }

    public void setAllowsUserPasswordChange(boolean allowsUserPasswordChange)
    {
        this.allowsUserPasswordChange = allowsUserPasswordChange;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public Set<String> getDomains()
    {
       return Collections.singleton(getDomain());
    }

    public Set<String> getDomainsThatAllowUserCreation()
    {
        if(getAllowsUserCreation())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

    public Set<String> getDomainsThatAllowUserDeletion()
    {
        if(getAllowsUserDeletion())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

    public Set<String> getDomiansThatAllowUserPasswordChanges()
    {
        if(getAllowsUserPasswordChange())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

   

    
}
