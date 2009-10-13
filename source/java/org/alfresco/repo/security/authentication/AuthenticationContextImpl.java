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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.PermissionService;

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
