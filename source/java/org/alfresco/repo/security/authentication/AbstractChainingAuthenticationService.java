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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * A base class for chaining authentication services. Where appropriate, methods will 'chain' across multiple
 * {@link AuthenticationService} instances, as returned by {@link #getUsableAuthenticationServices()}.
 * 
 * @author dward
 */
public abstract class AbstractChainingAuthenticationService extends AbstractAuthenticationService
{

    /**
     * Instantiates a new abstract chaining authentication service.
     */
    public AbstractChainingAuthenticationService()
    {
        super();
    }

    /**
     * Gets the mutable authentication service.
     * 
     * @return the mutable authentication service
     */
    public abstract AuthenticationService getMutableAuthenticationService();

    /**
     * Gets the authentication services across which methods will chain.
     * 
     * @return the usable authentication services
     */
    protected abstract List<AuthenticationService> getUsableAuthenticationServices();

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#createAuthentication(java.lang.String, char[])
     */
    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        if (getMutableAuthenticationService() == null)
        {
            throw new AuthenticationException(
                    "Unable to create authentication as there is no suitable authentication service.");
        }
        getMutableAuthenticationService().createAuthentication(userName, password);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#updateAuthentication(java.lang.String, char[], char[])
     */
    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        if (getMutableAuthenticationService() == null)
        {
            throw new AuthenticationException(
                    "Unable to update authentication as there is no suitable authentication service.");
        }
        getMutableAuthenticationService().updateAuthentication(userName, oldPassword, newPassword);

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#setAuthentication(java.lang.String, char[])
     */
    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        if (getMutableAuthenticationService() == null)
        {
            throw new AuthenticationException(
                    "Unable to set authentication as there is no suitable authentication service.");
        }
        getMutableAuthenticationService().setAuthentication(userName, newPassword);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#deleteAuthentication(java.lang.String)
     */
    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        if (getMutableAuthenticationService() == null)
        {
            throw new AuthenticationException(
                    "Unable to delete authentication as there is no suitable authentication service.");
        }
        getMutableAuthenticationService().deleteAuthentication(userName);

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#setAuthenticationEnabled(java.lang.String, boolean)
     */
    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        if (getMutableAuthenticationService() == null)
        {
            throw new AuthenticationException(
                    "Unable to set authentication enabled as there is no suitable authentication service.");
        }
        getMutableAuthenticationService().setAuthenticationEnabled(userName, enabled);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getAuthenticationEnabled(java.lang.String)
     */
    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                if (authService.getAuthenticationEnabled(userName))
                {
                    return true;
                }
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#authenticate(java.lang.String, char[])
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        preAuthenticationCheck(userName);
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.authenticate(userName, password);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to authenticate");

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#authenticateAsGuest()
     */
    public void authenticateAsGuest() throws AuthenticationException
    {
        preAuthenticationCheck(PermissionService.GUEST_AUTHORITY);
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.authenticateAsGuest();
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Guest authentication not supported");
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#guestUserAuthenticationAllowed()
     */
    public boolean guestUserAuthenticationAllowed()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService.guestUserAuthenticationAllowed())
            {
                return true;
            }
        }
        // it isn't allowed in any of the authentication components
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#authenticationExists(java.lang.String)
     */
    public boolean authenticationExists(String userName)
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService.authenticationExists(userName))
            {
                return true;
            }
        }
        // it doesn't exist in any of the authentication components
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getCurrentUserName()
     */
    public String getCurrentUserName() throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.getCurrentUserName();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#invalidateUserSession(java.lang.String)
     */
    public void invalidateUserSession(String userName) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.invalidateUserSession(userName);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to invalidate user session");

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#invalidateTicket(java.lang.String)
     */
    public void invalidateTicket(String ticket) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.invalidateTicket(ticket);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to invalidate ticket");

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#validate(java.lang.String)
     */
    public void validate(String ticket) throws AuthenticationException
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.validate(ticket);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Unable to validate ticket");

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getCurrentTicket()
     */
    public String getCurrentTicket()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.getCurrentTicket();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getNewTicket()
     */
    public String getNewTicket()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.getNewTicket();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#clearCurrentSecurityContext()
     */
    public void clearCurrentSecurityContext()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                authService.clearCurrentSecurityContext();
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to clear security context");

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#isCurrentUserTheSystemUser()
     */
    public boolean isCurrentUserTheSystemUser()
    {
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            try
            {
                return authService.isCurrentUserTheSystemUser();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getDomains()
     */
    public Set<String> getDomains()
    {
        HashSet<String> domains = new HashSet<String>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            domains.addAll(authService.getDomains());
        }
        return domains;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getDomainsThatAllowUserCreation()
     */
    public Set<String> getDomainsThatAllowUserCreation()
    {
        HashSet<String> domains = new HashSet<String>();
        if (getMutableAuthenticationService() != null)
        {
            domains.addAll(getMutableAuthenticationService().getDomainsThatAllowUserCreation());
        }
        return domains;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getDomainsThatAllowUserDeletion()
     */
    public Set<String> getDomainsThatAllowUserDeletion()
    {
        HashSet<String> domains = new HashSet<String>();
        if (getMutableAuthenticationService() != null)
        {
            domains.addAll(getMutableAuthenticationService().getDomainsThatAllowUserDeletion());
        }
        return domains;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getDomiansThatAllowUserPasswordChanges()
     */
    public Set<String> getDomiansThatAllowUserPasswordChanges()
    {
        HashSet<String> domains = new HashSet<String>();
        if (getMutableAuthenticationService() != null)
        {
            domains.addAll(getMutableAuthenticationService().getDomiansThatAllowUserPasswordChanges());
        }
        return domains;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AbstractAuthenticationService#getUsersWithTickets(boolean)
     */
    @Override
    public Set<String> getUsersWithTickets(boolean nonExpiredOnly)
    {
        HashSet<String> users = new HashSet<String>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService instanceof AbstractAuthenticationService)
            {
                users.addAll(((AbstractAuthenticationService) authService).getUsersWithTickets(nonExpiredOnly));
            }
        }
        return users;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AbstractAuthenticationService#countTickets(boolean)
     */
    @Override
    public int countTickets(boolean nonExpiredOnly)
    {
        int count = 0;
        for (TicketComponent tc : getTicketComponents())
        {
            count += tc.countTickets(nonExpiredOnly);
        }
        return count;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AbstractAuthenticationService#invalidateTickets(boolean)
     */
    @Override
    public int invalidateTickets(boolean nonExpiredOnly)
    {
        int count = 0;
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService instanceof AbstractAuthenticationService)
            {
                count += ((AbstractAuthenticationService) authService).invalidateTickets(nonExpiredOnly);
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AbstractAuthenticationService#getTicketComponents()
     */
    @Override
    public Set<TicketComponent> getTicketComponents()
    {
        Set<TicketComponent> tcs = new HashSet<TicketComponent>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            if (authService instanceof AbstractAuthenticationService)
            {
                tcs.addAll(((AbstractAuthenticationService) authService).getTicketComponents());
            }
        }
        return tcs;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.security.AuthenticationService#getDefaultAdministratorUserNames()
     */
    public Set<String> getDefaultAdministratorUserNames()
    {
        Set<String> defaultAdministratorUserNames = new TreeSet<String>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            defaultAdministratorUserNames.addAll(authService.getDefaultAdministratorUserNames());
        }
        return defaultAdministratorUserNames;
    }

}