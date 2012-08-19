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

import net.sf.acegisecurity.AccountExpiredException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.CredentialsExpiredException;
import net.sf.acegisecurity.DisabledException;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.LockedException;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.repo.tenant.TenantService;

/**
 * @author Andy Hind
 * @author dward
 */
public class AuthenticationContextImpl implements AuthenticationContext
{
    private TenantService tenantService;

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Explicitly set the given validated user details to be authenticated.
     * 
     * @param ud
     *            the User Details
     * @return Authentication
     */
    public Authentication setUserDetails(UserDetails ud)
    {
        try
        {
            // Apply the same validation that ACEGI would have to the user details - we may be going through a 'back
            // door'.
            if (!ud.isEnabled())
            {
                throw new DisabledException("User is disabled");
            }
            if (!ud.isAccountNonExpired())
            {
                throw new AccountExpiredException("User account has expired");
            }
            if (!ud.isAccountNonLocked())
            {
                throw new LockedException("User account is locked");
            }
            if (!ud.isCredentialsNonExpired())
            {
                throw new CredentialsExpiredException("User credentials have expired");
            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, "", ud
                    .getAuthorities());
            auth.setDetails(ud);
            auth.setAuthenticated(true);
            return setCurrentAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
        finally
        {
            // Support for logging tenantdomain / username (via log4j NDC)
            AuthenticationUtil.logNDC(ud.getUsername());
        }
    }

    public Authentication setSystemUserAsCurrentUser()
    {
        return setSystemUserAsCurrentUser(TenantService.DEFAULT_DOMAIN);
    }

    public Authentication setSystemUserAsCurrentUser(String tenantDomain)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_SYSTEM");
        return setUserDetails(new User(getSystemUserName(tenantDomain), "", true, true, true, true, gas));
    }

    public String getSystemUserName()
    {
        return AuthenticationUtil.SYSTEM_USER_NAME;
    }

    public String getSystemUserName(String tenantDomain)
    {
        return this.tenantService.getDomainUser(getSystemUserName(), tenantDomain);
    }

    public boolean isSystemUserName(String userName)
    {
        return getSystemUserName().equals(this.tenantService.getBaseNameUser(userName));
    }

    public boolean isCurrentUserTheSystemUser()
    {
        return isSystemUserName(getCurrentUserName());
    }

    public String getGuestUserName(String tenantDomain)
    {
        return this.tenantService.getDomainUser(getGuestUserName(), tenantDomain);
    }
    
    public String getGuestUserName()
    {
        return AuthenticationUtil.getGuestUserName();
    }

    public boolean isGuestUserName(String userName)
    {
        return AuthenticationUtil.getGuestUserName().equalsIgnoreCase(this.tenantService.getBaseNameUser(userName));
    }

    public Authentication setCurrentAuthentication(Authentication authentication)
    {
        return AuthenticationUtil.setFullAuthentication(authentication);
    }

    public Authentication getCurrentAuthentication() throws AuthenticationException
    {
        return AuthenticationUtil.getFullAuthentication();
    }

    public String getCurrentUserName() throws AuthenticationException
    {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }

    public void clearCurrentSecurityContext()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public String getUserDomain(String userName)
    {
        return this.tenantService.getUserDomain(userName);
    }
}
