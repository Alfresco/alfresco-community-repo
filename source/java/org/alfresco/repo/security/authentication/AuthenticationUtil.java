/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.context.security.SecureContextImpl;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.log4j.NDC;

public abstract class AuthenticationUtil
{

    public interface RunAsWork<Result>
    {
        /**
         * Method containing the work to be done in the user transaction.
         * 
         * @return Return the result of the operation
         */
        Result doWork() throws Exception;
    }

    public static final String SYSTEM_USER_NAME = "System";

    private static boolean mtEnabled = false;

    private AuthenticationUtil()
    {
        super();
    }

    public static void setMtEnabled(boolean mtEnabled)
    {
        if (!AuthenticationUtil.mtEnabled)
        {
            AuthenticationUtil.mtEnabled = mtEnabled;
        }
    }

    public static boolean isMtEnabled()
    {
        return AuthenticationUtil.mtEnabled;
    }

    public static Authentication setCurrentUser(String userName)
    {
        return setCurrentUser(userName, getDefaultUserDetails(userName));
    }

    public static Authentication setCurrentRealUser(String userName)
    {
        return setCurrentRealUser(userName, getDefaultUserDetails(userName));
    }

    public static Authentication setCurrentEffectiveUser(String userName)
    {
        return setCurrentEffectiveUser(userName, getDefaultUserDetails(userName));
    }

    public static Authentication setCurrentStoredUser(String userName)
    {
        return setCurrentStoredUser(userName, getDefaultUserDetails(userName));
    }

    /**
     * Explicitly set the current user to be authenticated.
     * 
     * @param userName -
     *            String user id
     * @param providedDetails -
     *            provided details for the user
     * @return Authentication
     */
    public static Authentication setCurrentUser(String userName, UserDetails providedDetails) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            return setCurrentAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    public static Authentication setCurrentRealUser(String userName, UserDetails providedDetails) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            return setCurrentRealAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    public static Authentication setCurrentEffectiveUser(String userName, UserDetails providedDetails) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            return setCurrentEffectiveAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    public static Authentication setCurrentStoredUser(String userName, UserDetails providedDetails) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            return setCurrentStoredAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    private static UsernamePasswordAuthenticationToken getAuthenticationToken(String userName, UserDetails providedDetails)
    {
        UserDetails ud = null;
        if (userName.equals(SYSTEM_USER_NAME))
        {
            GrantedAuthority[] gas = new GrantedAuthority[1];
            gas[0] = new GrantedAuthorityImpl("ROLE_SYSTEM");
            ud = new User(SYSTEM_USER_NAME, "", true, true, true, true, gas);
        }
        else if (userName.equalsIgnoreCase(PermissionService.GUEST_AUTHORITY))
        {
            GrantedAuthority[] gas = new GrantedAuthority[0];
            ud = new User(PermissionService.GUEST_AUTHORITY.toLowerCase(), "", true, true, true, true, gas);
        }
        else
        {
            if (providedDetails.getUsername().equals(userName))
            {
                ud = providedDetails;
            }
            else
            {
                throw new AuthenticationException("Provided user details do not match the user name");
            }
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, "", ud.getAuthorities());
        auth.setDetails(ud);
        auth.setAuthenticated(true);
        return auth;
    }

    /**
     * Default implementation that makes an ACEGI object on the fly
     * 
     * @param userName
     * @return
     */
    private static UserDetails getDefaultUserDetails(String userName)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        UserDetails ud = new User(userName, "", true, true, true, true, gas);
        return ud;
    }

    /**
     * Explicitly set the current authentication.
     * 
     * @param authentication
     *            Authentication
     */
    public static Authentication setCurrentAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            // Sets real and effective
            sc.setAuthentication(authentication);

            // Support for logging tenant domain / username (via log4j NDC)
            String userName = SYSTEM_USER_NAME;
            if (authentication.getPrincipal() instanceof UserDetails)
            {
                userName = ((UserDetails) authentication.getPrincipal()).getUsername();
            }

            logNDC(userName);

            return authentication;
        }
    }

    public static void logNDC(String userName)
    {
        NDC.remove();

        if (isMtEnabled())
        {
            int idx = userName.indexOf(TenantService.SEPARATOR);
            if ((idx != -1) && (idx < (userName.length() - 1)))
            {
                NDC.push("Tenant:" + userName.substring(idx + 1) + " User:" + userName.substring(0, idx));
            }
            else
            {
                NDC.push("User:" + userName);
            }
        }
        else
        {
            NDC.push("User:" + userName);
        }
    }

    public static Authentication setCurrentRealAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            sc.setRealAuthentication(authentication);
            return authentication;
        }
    }

    public static Authentication setCurrentEffectiveAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            sc.setEffectiveAuthentication(authentication);
            return authentication;
        }
    }

    public static Authentication setCurrentStoredAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            sc.setStoredAuthentication(authentication);
            return authentication;
        }
    }

    /**
     * Get the current authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public static Authentication getCurrentAuthentication() throws AuthenticationException
    {
        return getCurrentRealAuthentication();
    }

    /**
     * Get the current real authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public static Authentication getCurrentRealAuthentication() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        return ((AlfrescoSecureContext) context).getRealAuthentication();
    }

    /**
     * Get the current effective authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public static Authentication getCurrentEffectiveAuthentication() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        return ((AlfrescoSecureContext) context).getEffectiveAuthentication();
    }

    /**
     * Get the current stored authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public static Authentication getCurrentStoredAuthentication() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        return ((AlfrescoSecureContext) context).getStoredAuthentication();
    }

    /**
     * Get the current user name.
     * 
     * @return String
     * @throws AuthenticationException
     */
    public static String getCurrentUserName() throws AuthenticationException
    {
        return getCurrentRealUserName();
    }

    public static String getCurrentRealUserName() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        AlfrescoSecureContext ctx = (AlfrescoSecureContext) context;
        if (ctx.getRealAuthentication() == null)
        {
            return null;
        }
        return getUserName(ctx.getRealAuthentication());
    }

    public static String getCurrentEffectiveUserName() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        AlfrescoSecureContext ctx = (AlfrescoSecureContext) context;
        if (ctx.getEffectiveAuthentication() == null)
        {
            return null;
        }
        return getUserName(ctx.getEffectiveAuthentication());
    }

    public static String getCurrentStoredUserName() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        AlfrescoSecureContext ctx = (AlfrescoSecureContext) context;
        if (ctx.getStoredAuthentication() == null)
        {
            return null;
        }
        return getUserName(ctx.getStoredAuthentication());
    }

    /**
     * Get the current user name
     * 
     * @param authentication
     *            Authentication
     * @return String
     */
    private static String getUserName(Authentication authentication)
    {
        String username;
        if (authentication.getPrincipal() instanceof UserDetails)
        {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        else
        {
            username = authentication.getPrincipal().toString();
        }

        return username;
    }

    /**
     * Set the system user as the current user.
     * 
     * @return Authentication
     */
    public static Authentication setSystemUserAsCurrentUser()
    {
        return setCurrentUser(SYSTEM_USER_NAME);
    }

    /**
     * Get the name of the system user
     * 
     * @return String
     */
    public static String getSystemUserName()
    {
        return SYSTEM_USER_NAME;
    }

    /**
     * Get the name of the Guest User
     */
    public static String getGuestUserName()
    {
        return PermissionService.GUEST_AUTHORITY.toLowerCase();
    }

    /**
     * Remove the current security information
     */
    public static void clearCurrentSecurityContext()
    {
        ContextHolder.setContext(null);
        InMemoryTicketComponentImpl.clearCurrentSecurityContext();
        NDC.remove();
    }

    /**
     * Execute a unit of work as a given user. The thread's authenticated user will be returned to its normal state
     * after the call.
     * 
     * @param runAsWork
     *            the unit of work to do
     * @param uid
     *            the user ID
     * @return Returns the work's return value
     */
    public static <R> R runAs(RunAsWork<R> runAsWork, String uid)
    {
        String effectiveUser = AuthenticationUtil.getCurrentEffectiveUserName();
        String realUser = AuthenticationUtil.getCurrentRealUserName();

        R result = null;
        try
        {

            if ((realUser != null) && (isMtEnabled()))
            {
                int idx = realUser.indexOf(TenantService.SEPARATOR);
                if ((idx != -1) && (idx < (realUser.length() - 1)))
                {
                    if (uid.equals(AuthenticationUtil.getSystemUserName()))
                    {
                        uid = uid + TenantService.SEPARATOR + realUser.substring(idx + 1);
                    }
                }
            }

            if (realUser == null)
            {
                AuthenticationUtil.setCurrentRealUser(uid);
            }
            AuthenticationUtil.setCurrentEffectiveUser(uid);

            result = runAsWork.doWork();
            return result;
        }
        catch (Throwable exception)
        {

            // Re-throw the exception
            if (exception instanceof RuntimeException)
            {
                throw (RuntimeException) exception;
            }
            else
            {
                throw new RuntimeException("Error during run as.", exception);
            }
        }
        finally
        {
            if (realUser == null)
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
            else
            {
                AuthenticationUtil.setCurrentEffectiveUser(effectiveUser);
            }
        }
    }
}
