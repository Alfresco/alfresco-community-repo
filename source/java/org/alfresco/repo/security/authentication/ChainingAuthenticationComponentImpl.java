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

import org.alfresco.service.cmr.security.PermissionService;

/**
 * A chaining authentication component is required for all the beans that qire up an authentication component and not an
 * authentication service. It supports chaining in much the same way and wires up components in the same way asthe
 * chaining authentication service wires up services.
 * 
 * @author andyh
 */
public class ChainingAuthenticationComponentImpl implements AuthenticationComponent
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
        return authenticationComponents;
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
        return mutableAuthenticationComponent;
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
     * Chain authentication with user name and password - tries all in order until one works, or fails.
     */
    public void authenticate(String userName, char[] password) throws AuthenticationException
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
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        if (ntlmMode != null)
        {
            switch (ntlmMode)
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
                        throw new AuthenticationException("The first authentication component to support NTLM supports MD4 hashing");
                    }
                }
            }
            throw new AuthenticationException("No NTLM passthrough authentication to use");
        }

    }

    /**
     * Clear the security context
     */
    public void clearCurrentSecurityContext()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Get the current authentication
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException
    {
        return AuthenticationUtil.getCurrentAuthentication();
    }

    /**
     * Get the current user name
     */
    public String getCurrentUserName() throws AuthenticationException
    {
        return AuthenticationUtil.getCurrentUserName();
    }

    /**
     * Get the guest user name
     */
    public String getGuestUserName()
    {
        return PermissionService.GUEST_AUTHORITY.toLowerCase();
    }

    /**
     * Get the MD4 password hash
     */
    public String getMD4HashedPassword(String userName)
    {
        if (ntlmMode != null)
        {
            switch (ntlmMode)
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
                        throw new AuthenticationException("The first authentication component to support NTLM supports passthrough");
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
    public NTLMMode getNTLMMode()
    {
        if (ntlmMode != null)
        {
            switch (ntlmMode)
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
     * Get the system user name
     */
    public String getSystemUserName()
    {
        return AuthenticationUtil.SYSTEM_USER_NAME;
    }
    
    /**
     * If any implementation supports System then System is allowed
     */
    public boolean isSystemUserName(String userName)
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            if (authComponent.isSystemUserName(userName))
            {
                return true;
            }
        }
        return false; 	
    }

    /**
     * If any implementation supports guest then guest is allowed
     */
    public boolean guestUserAuthenticationAllowed()
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

    /**
     * Ste the current authentication
     */
    public Authentication setCurrentAuthentication(Authentication authentication)
    {
        return AuthenticationUtil.setCurrentAuthentication(authentication);
    }

    /**
     * Set the current user - try all implementations - as some may check the user exists
     */
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
     * Authenticate as guest - try all in the cahin
     */
    public Authentication setGuestUserAsCurrentUser()
    {
        for (AuthenticationComponent authComponent : getUsableAuthenticationComponents())
        {
            try
            {
                return authComponent.setGuestUserAsCurrentUser();
            }
            catch (AuthenticationException e)
            {
                // Ignore and chain
            }
        }
        throw new AuthenticationException("Guest authentication is not allowed");
    }

    /**
     * Set the system user
     */
    public Authentication setSystemUserAsCurrentUser()
    {
        return setCurrentUser(getSystemUserName());
    }

    /**
     * Helper to get authentication components
     * 
     * @return
     */
    private List<AuthenticationComponent> getUsableAuthenticationComponents()
    {
        if (mutableAuthenticationComponent == null)
        {
            return authenticationComponents;
        }
        else
        {
            ArrayList<AuthenticationComponent> services = new ArrayList<AuthenticationComponent>(authenticationComponents == null ? 1 : (authenticationComponents.size() + 1));
            services.add(mutableAuthenticationComponent);
            if (authenticationComponents != null)
            {
                services.addAll(authenticationComponents);
            }
            return services;
        }
    }

}
