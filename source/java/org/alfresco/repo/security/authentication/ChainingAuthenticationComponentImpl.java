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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;

/**
 * A chaining authentication component is required for all the beans that wire up an authentication component and not an
 * authentication service. It supports chaining in much the same way and wires up components in the same way as the
 * chaining authentication service wires up services.
 * 
 * @author andyh
 */
public class ChainingAuthenticationComponentImpl extends AbstractChainingAuthenticationComponent implements NLTMAuthenticator
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
    public void setMutableAuthenticationComponent(AuthenticationComponent mutableAuthenticationComponent)
    {
        this.mutableAuthenticationComponent = mutableAuthenticationComponent;
    }

    public void setNtlmMode(NTLMMode ntlmMode)
    {
        this.ntlmMode = ntlmMode;
    }

    /**
     * NTLM passthrough authentication - if a mode is defined - the first PASS_THROUGH provider is used - if not, the
     * first component that supports NTLM is used if it supports PASS_THROUGH
     */
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
                    if (!(authComponent instanceof NLTMAuthenticator))
                    {
                        continue;
                    }
                    NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return ssoAuthenticator.authenticate(token);
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
                if (!(authComponent instanceof NLTMAuthenticator))
                {
                    continue;
                }
                NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                if (ssoAuthenticator.getNTLMMode() != NTLMMode.NONE)
                {
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return ssoAuthenticator.authenticate(token);
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
     * Get the guest user name
     */
    public String getGuestUserName()
    {
        return AuthenticationUtil.getGuestUserName();
    }

    /**
     * Get the MD4 password hash
     */
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
                    if (!(authComponent instanceof NLTMAuthenticator))
                    {
                        continue;
                    }
                    NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.MD4_PROVIDER)
                    {
                        return ssoAuthenticator.getMD4HashedPassword(userName);
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
                if (!(authComponent instanceof NLTMAuthenticator))
                {
                    continue;
                }
                NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                if (ssoAuthenticator.getNTLMMode() != NTLMMode.NONE)
                {
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        throw new AuthenticationException(
                                "The first authentication component to support NTLM supports passthrough");
                    }
                    else
                    {
                        return ssoAuthenticator.getMD4HashedPassword(userName);
                    }
                }
            }
            throw new AuthenticationException("No MD4 provider available");
        }

    }

    /**
     * Get the NTLM mode - this is only what is set if one of the implementations provides support for that mode.
     */
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
                    if (!(authComponent instanceof NLTMAuthenticator))
                    {
                        continue;
                    }
                    NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.PASS_THROUGH)
                    {
                        return NTLMMode.PASS_THROUGH;
                    }
                }
                return NTLMMode.NONE;
            case MD4_PROVIDER:
                for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
                {
                    if (!(authComponent instanceof NLTMAuthenticator))
                    {
                        continue;
                    }
                    NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                    if (ssoAuthenticator.getNTLMMode() == NTLMMode.MD4_PROVIDER)
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
                if (!(authComponent instanceof NLTMAuthenticator))
                {
                    continue;
                }
                NLTMAuthenticator ssoAuthenticator = (NLTMAuthenticator)authComponent;
                if (ssoAuthenticator.getNTLMMode() != NTLMMode.NONE)
                {
                    return ssoAuthenticator.getNTLMMode();
                }
            }
            return NTLMMode.NONE;
        }
    }

    /**
     * Helper to get authentication components
     * 
     * @return
     */
    protected Collection<AuthenticationComponent> getUsableAuthenticationComponents()
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

    @Override
    protected AuthenticationComponent getAuthenticationComponent(String name)
    {
        // not implemented
        return null;
    }
}
