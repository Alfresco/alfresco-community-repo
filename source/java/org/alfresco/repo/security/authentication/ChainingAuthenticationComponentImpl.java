/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import java.util.ArrayList;
import java.util.List;

import net.sf.acegisecurity.Authentication;

import org.alfresco.service.Managed;

/**
 * A chaining authentication component is required for all the beans that qire up an authentication component and not an
 * authentication service. It supports chaining in much the same way and wires up components in the same way asthe
 * chaining authentication service wires up services.
 * 
 * @author andyh
 */
public class ChainingAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    /**
     * NLTM authentication mode - if unset - finds the first component that supports NTLM - if set - finds the first
     * component that supports the specified mode.
     */
    private NTLMMode ntlmMode = null;

    /**
     * The authentication components
     */
    private List<AuthenticationComponent> authenticationComponents;

    /**
     * An authentication service that supports change (as wired in to the authentication service). It is never used for
     * change it is to ensure it is at the top of the list (as required by the chaining authentication service)
     */
    private AuthenticationComponent mutableAuthenticationComponent;

    /**
     * Get the authentication components
     * 
     * @return - a list of authentication components
     */
    public List<AuthenticationComponent> getAuthenticationComponents()
    {
        return this.authenticationComponents;
    }

    /**
     * Set a list of authentication components
     * 
     * @param authenticationComponents
     */
    @Managed(category = "Security")
    public void setAuthenticationComponents(List<AuthenticationComponent> authenticationComponents)
    {
        this.authenticationComponents = authenticationComponents;
    }

    /**
     * Get the authentication service thta must be at the top of the list (this may be null)
     * 
     * @return
     */
    public AuthenticationComponent getMutableAuthenticationComponent()
    {
        return this.mutableAuthenticationComponent;
    }

    /**
     * Set the authentication component at the top of the list.
     * 
     * @param mutableAuthenticationComponent
     */
    @Managed(category = "Security")
    public void setMutableAuthenticationComponent(AuthenticationComponent mutableAuthenticationComponent)
    {
        this.mutableAuthenticationComponent = mutableAuthenticationComponent;
    }

    @Managed(category = "Security")
    public void setNtlmMode(NTLMMode ntlmMode)
    {
        this.ntlmMode = ntlmMode;
    }

    /**
     * Chain authentication with user name and password - tries all in order until one works, or fails.
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
     * NTLM passthrough authentication - if a mode is defined - the first PASS_THROUGH provider is used - if not, the
     * first component that supports NTLM is used if it supports PASS_THROUGH
     */
    @Override
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        if (this.ntlmMode != null)
        {
            switch (this.ntlmMode)
            {
            case NONE:
                throw new AuthenticationException("NTLM is not supported");
            case MD4_PROVIDER:
                throw new AuthenticationException("NTLM passthrough is not supported then configured for MD4 hashing");
            case PASS_THROUGH:
                for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
                {
                    if (authComponent.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return authComponent.authenticate(token);
                    }
                }
                throw new AuthenticationException("No NTLM passthrough authentication to use");
            default:
                throw new AuthenticationException("No NTLM passthrough authentication to use");
            }
        }
        else
        {
            for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
            {
                if (authComponent.getNTLMMode() != NTLMMode.NONE)
                {
                    if (authComponent.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return authComponent.authenticate(token);
                    }
                    else
                    {
                        throw new AuthenticationException(
                                "The first authentication component to support NTLM supports MD4 hashing");
                    }
                }
            }
            throw new AuthenticationException("No NTLM passthrough authentication to use");
        }

    }

    /**
     * Get the MD4 password hash
     */
    @Override
    public String getMD4HashedPassword(String userName)
    {
        if (this.ntlmMode != null)
        {
            switch (this.ntlmMode)
            {
            case NONE:
                throw new AuthenticationException("NTLM is not supported");
            case PASS_THROUGH:
                throw new AuthenticationException("NTLM passthrough is not supported then configured for MD4 hashing");
            case MD4_PROVIDER:
                for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
                {
                    if (authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
                    {
                        return authComponent.getMD4HashedPassword(userName);
                    }
                }
                throw new AuthenticationException("No MD4 provider available");
            default:
                throw new AuthenticationException("No MD4 provider available");
            }
        }
        else
        {
            for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
            {
                if (authComponent.getNTLMMode() != NTLMMode.NONE)
                {
                    if (authComponent.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        throw new AuthenticationException(
                                "The first authentication component to support NTLM supports passthrough");
                    }
                    else
                    {
                        return authComponent.getMD4HashedPassword(userName);
                    }
                }
            }
            throw new AuthenticationException("No MD4 provider available");
        }

    }

    /**
     * Get the NTLM mode - this is only what is set if one of the implementations provides support for that mode.
     */
    @Override
    public NTLMMode getNTLMMode()
    {
        if (this.ntlmMode != null)
        {
            switch (this.ntlmMode)
            {
            case NONE:
                return NTLMMode.NONE;
            case PASS_THROUGH:
                for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
                {
                    if (authComponent.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return NTLMMode.PASS_THROUGH;
                    }
                }
                return NTLMMode.NONE;
            case MD4_PROVIDER:
                for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
                {
                    if (authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
                    {
                        return NTLMMode.MD4_PROVIDER;
                    }
                }
                return NTLMMode.NONE;
            default:
                return NTLMMode.NONE;
            }
        }
        else
        {
            for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
            {
                if (authComponent.getNTLMMode() != NTLMMode.NONE)
                {
                    return authComponent.getNTLMMode();
                }
            }
            return NTLMMode.NONE;
        }
    }

    /**
     * If any implementation supports guest then guest is allowed
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
     * Set the current user - try all implementations - as some may check the user exists
     */
    @Override
    public Authentication setCurrentUser(String userName)
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            try
            {
                return authComponent.setCurrentUser(userName);
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Failed to set current user " + userName);
    }

    /**
     * Helper to get authentication components
     * 
     * @return
     */
    private List<AuthenticationComponent> getUsableAuthenticationComponents()
    {
        if (this.mutableAuthenticationComponent == null)
        {
            return this.authenticationComponents;
        }
        else
        {
            ArrayList<AuthenticationComponent> services = new ArrayList<AuthenticationComponent>(
                    this.authenticationComponents == null ? 1 : this.authenticationComponents.size() + 1);
            services.add(this.mutableAuthenticationComponent);
            if (this.authenticationComponents != null)
            {
                services.addAll(this.authenticationComponents);
            }
            return services;
        }
    }
}
