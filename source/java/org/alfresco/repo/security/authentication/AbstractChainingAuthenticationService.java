/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A base class for chaining authentication services. Where appropriate, methods will 'chain' across multiple
 * {@link AuthenticationService} instances, as returned by {@link #getUsableAuthenticationServices()}.
 * 
 * @author dward
 */
public abstract class AbstractChainingAuthenticationService extends AbstractAuthenticationService implements MutableAuthenticationService
{
    private static final Log logger = LogFactory.getLog(AbstractChainingAuthenticationService.class);

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
    public abstract MutableAuthenticationService getMutableAuthenticationService();

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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationMutable(String userName)
    {
        MutableAuthenticationService mutableAuthenticationService = getMutableAuthenticationService();
        return mutableAuthenticationService == null ? false : mutableAuthenticationService
                .isAuthenticationMutable(userName);
    }

    
    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationCreationAllowed()
    {
        MutableAuthenticationService mutableAuthenticationService = getMutableAuthenticationService();
        return mutableAuthenticationService == null ? false : mutableAuthenticationService
                .isAuthenticationCreationAllowed();
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        if (isAuthenticationMutable(userName))
        {
            MutableAuthenticationService mutableAuthenticationService = getMutableAuthenticationService();
            if (mutableAuthenticationService != null)
            {
                return mutableAuthenticationService.getAuthenticationEnabled(userName);
            }
        }
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

    /**
     * {@inheritDoc}
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        preAuthenticationCheck(userName);
        List<AuthenticationService> usableAuthenticationServices = getUsableAuthenticationServices();
        int counter = usableAuthenticationServices.size();
        for (AuthenticationService authService : usableAuthenticationServices)
        {
            try
            {
                counter--;
                authService.authenticate(userName, password);
                if (logger.isDebugEnabled())
                {
                    logger.debug("authenticate "+userName+" with "+getId(authService)+" SUCCEEDED");
                }
                return;
            }
            catch (AuthenticationException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("authenticate "+userName+" with "+getId(authService)+(counter == 0 ? " FAILED (end of chain)" : " failed (try next in chain)"));
                }
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to authenticate");

    }

    /**
     * Should be overridden to returns the ID of the authService for use in debug.
     * @param authService in question.
     * @return the ID of the authService. This implementation has no way to work 
     *         this out so returns the simple class name.
     */
    protected String getId(AuthenticationService authService)
    {
        return authService.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    public void authenticateAsGuest() throws AuthenticationException
    {
        String defaultGuestName = AuthenticationUtil.getGuestUserName();
        if (defaultGuestName != null && defaultGuestName.length() > 0)
        {
            preAuthenticationCheck(defaultGuestName);
        }
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
        throw new AuthenticationException(GUEST_AUTHENTICATION_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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
    
    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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
        throw new AuthenticationException("Unable to issue ticket");
    }

    /**
     * {@inheritDoc}
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
        throw new AuthenticationException("Unable to issue ticket");
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    public Set<String> getDefaultGuestUserNames()
    {
        Set<String> defaultGuestUserNames = new TreeSet<String>();
        for (AuthenticationService authService : getUsableAuthenticationServices())
        {
            defaultGuestUserNames.addAll(authService.getDefaultGuestUserNames());
        }
        return defaultGuestUserNames;
    }
    
    

}