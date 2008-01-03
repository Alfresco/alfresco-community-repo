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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * This class abstract the support required to set up and query the Acegi context for security enforcement. There are
 * some simple default method implementations to support simple authentication.
 * 
 * @author Andy Hind
 */
public abstract class AbstractAuthenticationComponent implements AuthenticationComponent
{
    /**
     * The abstract class keeps track of support for guest login
     */
    private Boolean allowGuestLogin = null;
    
    private TenantService tenantService;

    public AbstractAuthenticationComponent()
    {
        super();
    }

    /**
     * Set if guest login is supported.
     * 
     * @param allowGuestLogin
     */
    public void setAllowGuestLogin(Boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }
    
    public void setTenantService(TenantService tenantService)
    {
    	this.tenantService = tenantService;
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        // Support guest login from the login screen
        if (isGuestUserName(userName))
        {
            setGuestUserAsCurrentUser(tenantService.getUserDomain(userName));
        }
        else
        {
            authenticateImpl(userName, password);
        }
    }

    /**
     * Default unsupported authentication implementation 
     *  - as of 2.1 this is the best way to implement your own authentication component as it will support guest login
     *  - prior to this direct over ride for authenticate(String , char[]) was used. This will still work.
     * 
     * @param userName
     * @param password
     */
    protected void authenticateImpl(String userName, char[] password)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Explicitly set the current user to be authenticated.
     * 
     * @param userName
     *            String
     * @return Authentication
     */
    public Authentication setCurrentUser(String userName) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UserDetails ud = null;
            if (userName.equals(AuthenticationUtil.SYSTEM_USER_NAME))
            {
                GrantedAuthority[] gas = new GrantedAuthority[1];
                gas[0] = new GrantedAuthorityImpl("ROLE_SYSTEM");
                ud = new User(AuthenticationUtil.SYSTEM_USER_NAME, "", true, true, true, true, gas);
            }
            else if (isGuestUserName(userName))
            {
                GrantedAuthority[] gas = new GrantedAuthority[0];
                ud = new User(getGuestUserName(tenantService.getUserDomain(userName)), "", true, true, true, true, gas);
            }
            else
            {
                ud = getUserDetails(userName);
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, "", ud.getAuthorities());
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
            AuthenticationUtil.logNDC(userName);
        }
    }

    /**
     * Default implementation that makes an ACEGI object on the fly
     * 
     * @param userName
     * @return
     */
    protected UserDetails getUserDetails(String userName)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        UserDetails ud = new User(userName, "", true, true, true, true, gas);
        return ud;
    }

    /**
     * {@inheritDoc}
     */
    public Authentication setCurrentAuthentication(Authentication authentication)
    {
        return AuthenticationUtil.setCurrentAuthentication(authentication);
    }

    /**
     * Get the current authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException
    {
        return AuthenticationUtil.getCurrentAuthentication();
    }

    /**
     * Get the current user name.
     * 
     * @return String
     * @throws AuthenticationException
     */
    public String getCurrentUserName() throws AuthenticationException
    {
        return AuthenticationUtil.getCurrentUserName();
    }

    /**
     * Set the system user as the current user.
     * 
     * @return Authentication
     */
    public Authentication setSystemUserAsCurrentUser()
    {
        return setCurrentUser(AuthenticationUtil.SYSTEM_USER_NAME);
    }

    /**
     * Get the name of the system user
     * 
     * @return String
     */
    public String getSystemUserName()
    {
        return AuthenticationUtil.SYSTEM_USER_NAME;
    }

    /**
     * Get the name of the Guest User
     * note: for MT, will get guest for default domain only
     */
    public String getGuestUserName()
    {
        return PermissionService.GUEST_AUTHORITY.toLowerCase();
    }

    private String getGuestUserName(String tenantDomain)
    {
    	return tenantService.getDomainUser(getGuestUserName(), tenantDomain);
    }
    
    /**
     * Set the guest user as the current user.
     * note: for MT, will set to default domain only
     */
    public Authentication setGuestUserAsCurrentUser() throws AuthenticationException
    {
    	return setGuestUserAsCurrentUser(TenantService.DEFAULT_DOMAIN);
    }
    
    /**
     * Set the guest user as the current user.
     */
    private Authentication setGuestUserAsCurrentUser(String tenantDomain) throws AuthenticationException
    {
        if (allowGuestLogin == null)
        {
            if (implementationAllowsGuestLogin())
            {
                return setCurrentUser(getGuestUserName(tenantDomain));
            }
            else
            {
                throw new AuthenticationException("Guest authentication is not allowed");
            }
        }
        else
        {
            if (allowGuestLogin.booleanValue())
            {
            	return setCurrentUser(getGuestUserName(tenantDomain));
            }
            else
            {
                throw new AuthenticationException("Guest authentication is not allowed");
            }

        }
    }
    
    private boolean isGuestUserName(String userName)
    {
    	return ((userName != null) && tenantService.getBaseNameUser(userName).equalsIgnoreCase(PermissionService.GUEST_AUTHORITY));
    }

    protected abstract boolean implementationAllowsGuestLogin();

    /**
     * @return true if Guest user authentication is allowed, false otherwise
     */
    public boolean guestUserAuthenticationAllowed()
    {
        if (allowGuestLogin == null)
        {
            return (implementationAllowsGuestLogin());
        }
        else
        {
            return (allowGuestLogin.booleanValue());
        }
    }

    /**
     * Remove the current security information
     */
    public void clearCurrentSecurityContext()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * The default is not to support Authentication token base authentication
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Authentication via token not supported");
    }

    /**
     * The should only be supported if getNTLMMode() is NTLMMode.MD4_PROVIDER.
     */
    public String getMD4HashedPassword(String userName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the NTML mode - none - supports MD4 hash to integrate - or it can asct as an NTLM authentication
     */
    public NTLMMode getNTLMMode()
    {
        return NTLMMode.NONE;
    }

}
