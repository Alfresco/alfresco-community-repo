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
package org.alfresco.repo.security.authentication;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import net.sf.acegisecurity.Authentication;

/**
 * A base class for chaining authentication components. Where appropriate, methods will 'chain' across multiple
 * {@link AuthenticationComponent} instances, as returned by {@link #getUsableAuthenticationComponents()}.
 * 
 * @author dward
 */
public abstract class AbstractChainingAuthenticationComponent extends AbstractAuthenticationComponent
{

    /**
     * Instantiates a new abstract chaining authentication component.
     */
    public AbstractChainingAuthenticationComponent()
    {
        super();
    }

    /**
     * Gets the authentication components across which methods will chain.
     * 
     * @return the usable authentication components
     */
    protected abstract Collection<AuthenticationComponent> getUsableAuthenticationComponents();

    /**
     * Chain authentication with user name and password - tries all in order until one works, or fails.
     * 
     * @param userName
     *            the user name
     * @param password
     *            the password
     */
    @Override
    protected void authenticateImpl(String userName, char[] password)
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            try
            {
                authComponent.authenticate(userName, password);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to authenticate");
    }

    /**
     * If any implementation supports guest then guest is allowed.
     * 
     * @return true, if implementation allows guest login
     */
    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            if (authComponent.guestUserAuthenticationAllowed())
            {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authentication.AbstractAuthenticationComponent#setCurrentUser(java.lang.String,
     * org.alfresco.repo.security.authentication.AuthenticationComponent.UserNameValidationMode)
     */
    @Override
    public Authentication setCurrentUser(String userName, UserNameValidationMode validationMode)
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            try
            {
                return authComponent.setCurrentUser(userName, validationMode);
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to set current user " + userName);
    }

    /**
     * Set the current user - try all implementations - as some may check the user exists.
     * 
     * @param userName
     *            the user name
     * @return the authentication
     */
    @Override
    public Authentication setCurrentUser(String userName)
    {
        Exception last = null;
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            try
            {
                return authComponent.setCurrentUser(userName);
            }
            catch (AuthenticationException e)
            {
                last = e;
            }
        }
        throw new AuthenticationException("Failed to set current user " + userName, last);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getDefaultAdministratorUserNames()
    {
        Set<String> defaultAdministratorUserNames = new TreeSet<String>();
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            defaultAdministratorUserNames.addAll(authComponent.getDefaultAdministratorUserNames());
        }
        return defaultAdministratorUserNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getDefaultGuestUserNames()
    {
        Set<String> defaultGuestUserNames = new TreeSet<String>();
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            defaultGuestUserNames.addAll(authComponent.getDefaultGuestUserNames());
        }
        return defaultGuestUserNames;
    }

}