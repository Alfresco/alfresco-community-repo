/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authentication;

import java.util.Stack;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.dao.User;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.log.NDC;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Utility helper methods to change the authenticated context for threads.
 */
@AlfrescoPublicApi
public class AuthenticationUtil implements InitializingBean
{
    static Log s_logger = LogFactory.getLog(AuthenticationUtil.class);
    @AlfrescoPublicApi
    public interface RunAsWork<Result>
    {
        /**
         * Method containing the work to be done in the user transaction.
         * 
         * @return Return the result of the operation
         */
        Result doWork() throws Exception;
    }

    private static boolean initialized = false;
    
    public static final String SYSTEM_USER_NAME = "System";
    private static String defaultAdminUserName = PermissionService.ADMINISTRATOR_AUTHORITY;
    private static String defaultGuestUserName = PermissionService.GUEST_AUTHORITY; 
    private static boolean mtEnabled = false;
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // at this point default admin and guest names have been assigned
        initialized = true;
    }
    
    public void setDefaultAdminUserName(String defaultAdminUserName)
    {
        AuthenticationUtil.defaultAdminUserName = defaultAdminUserName;
    }
    
    public void setDefaultGuestUserName(String defaultGuestUserName)
    {
        AuthenticationUtil.defaultGuestUserName = defaultGuestUserName;
    }
    
    public static void setMtEnabled(boolean mtEnabled)
    {
        if (s_logger.isDebugEnabled())
            s_logger.debug("MT is enabled: " + mtEnabled);
        AuthenticationUtil.mtEnabled = mtEnabled;
    }

    public static boolean isMtEnabled()
    {
        return AuthenticationUtil.mtEnabled;
    }

    public AuthenticationUtil()
    {
        super();
    }

    /**
     * Utility method to create an authentication token
     */
    private static UsernamePasswordAuthenticationToken getAuthenticationToken(String userName, UserDetails providedDetails)
    {
        UserDetails ud = null;
        if (userName.equals(SYSTEM_USER_NAME))
        {
            GrantedAuthority[] gas = new GrantedAuthority[1];
            gas[0] = new GrantedAuthorityImpl("ROLE_SYSTEM");
            ud = new User(SYSTEM_USER_NAME, "", true, true, true, true, gas);
        }
        else if (userName.equalsIgnoreCase(getGuestUserName()))
        {
            GrantedAuthority[] gas = new GrantedAuthority[0];
            ud = new User(getGuestUserName().toLowerCase(), "", true, true, true, true, gas);
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
     */
    private static UserDetails getDefaultUserDetails(String userName)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        UserDetails ud = new User(userName, "", true, true, true, true, gas);
        return ud;
    }

    /**
     * Extract the username from the authentication.
     */
    private static String getUserName(Authentication authentication)
    {
        if (authentication.getPrincipal() instanceof UserDetails)
        {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        else
        {
            return authentication.getPrincipal().toString();
        }
    }

    /**
     * Authenticate as the Admin user.  The Admin user will be authenticated and all operations
     * with be run in the context of this Admin user.
     * 
     * @return the authentication token
     */
    public static Authentication setAdminUserAsFullyAuthenticatedUser()
    {
        return setFullyAuthenticatedUser(getAdminUserName());
    }
    
    /**
     * Authenticate as the given user.  The user will be authenticated and all operations
     * with be run in the context of this user.
     * 
     * @param userName              the user name
     * @return                      the authentication token
     */
    public static Authentication setFullyAuthenticatedUser(String userName)
    {
        return setFullyAuthenticatedUser(userName, getDefaultUserDetails(userName));
    }
    
    private static Authentication setFullyAuthenticatedUser(String userNameIn, UserDetails providedDetails) throws AuthenticationException
    {
        if (userNameIn == null)
        {
            throw new AuthenticationException("Null user name");
        }
        
        try
        {
            Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userNameIn);
            final String userName = userTenant.getFirst();
            final String tenantDomain = userTenant.getSecond();
            
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            Authentication authentication = setFullAuthentication(auth);
            
            TenantContextHolder.setTenantDomain(tenantDomain);
            
            return authentication;
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    /**
     * Re-authenticate using a previously-created authentication.
     */
    public static Authentication setFullAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            if (s_logger.isDebugEnabled())
                s_logger.debug("Setting fully authenticated principal: " + authentication.getName());
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                if (s_logger.isDebugEnabled())
                    s_logger.debug("Creating new secure context.");
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            // Sets real and effective
            sc.setRealAuthentication(authentication);
            sc.setEffectiveAuthentication(authentication);
            return authentication;
        }
    }
    
    /**
     * <b>WARN: Advanced usage only.</b><br/>
     * Set the system user as the currently running user for authentication purposes.
     * 
     * @return Authentication
     * 
     * @see #setRunAsUser(String)
     */
    public static Authentication setRunAsUserSystem()
    {
        return setRunAsUser(SYSTEM_USER_NAME);
    }

    /**
     * <b>WARN: Advanced usage only.</b><br/>
     * Switch to the given user for all authenticated operations.  The original, authenticated user
     * can still be found using {@link #getFullyAuthenticatedUser()}.
     * 
     * @param userName          the user to run as
     * @return                  the new authentication
     */
    public static Authentication setRunAsUser(String userName)
    {
        return setRunAsUser(userName, getDefaultUserDetails(userName));
    }
    
    /*package*/ static Authentication setRunAsUser(String userName, UserDetails providedDetails) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        try
        {
            UsernamePasswordAuthenticationToken auth = getAuthenticationToken(userName, providedDetails);
            return setRunAsAuthentication(auth);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    /*package*/ static Authentication setRunAsAuthentication(Authentication authentication)
    {
        if (authentication == null)
        {
            clearCurrentSecurityContext();
            return null;
        }
        else
        {
            if (s_logger.isDebugEnabled())
                s_logger.debug("Setting RunAs principal: " + authentication.getName());
            Context context = ContextHolder.getContext();
            AlfrescoSecureContext sc = null;
            if ((context == null) || !(context instanceof AlfrescoSecureContext))
            {
                if (s_logger.isDebugEnabled())
                    s_logger.debug("Creating new secure context.");
                sc = new AlfrescoSecureContextImpl();
                ContextHolder.setContext(sc);
            }
            else
            {
                sc = (AlfrescoSecureContext) context;
            }
            authentication.setAuthenticated(true);
            if (sc.getRealAuthentication() == null)
            {
                if (s_logger.isDebugEnabled())
                    s_logger.debug("There is no fully authenticated prinipal. Setting fully authenticated principal: " + authentication.getName());
                sc.setRealAuthentication(authentication);
            }
            sc.setEffectiveAuthentication(authentication);
            return authentication;
        }
    }
    
    /**
     * Get the current authentication for application of permissions.  This includes
     * the any overlay details set by {@link #setRunAsUser(String)}.
     * 
     * @return Authentication               Returns the running authentication
     * @throws AuthenticationException
     */
    public static Authentication getRunAsAuthentication() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        return ((AlfrescoSecureContext) context).getEffectiveAuthentication();
    }
    
    /**
     * <b>WARN: Advanced usage only.</b><br/>
     * Get the authentication for that was set by an real authentication.
     * 
     * @return Authentication               Returns the real authentication
     * @throws AuthenticationException
     */
    public static Authentication getFullAuthentication() throws AuthenticationException
    {
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof AlfrescoSecureContext))
        {
            return null;
        }
        return ((AlfrescoSecureContext) context).getRealAuthentication();
    }
    
    /**
     * Get the user that is currently in effect for purposes of authentication.  This includes
     * any overlays introduced by {@link #setRunAsUser(String) runAs}.
     * 
     * @return              Returns the name of the user
     * @throws AuthenticationException
     */
    public static String getRunAsUser() throws AuthenticationException
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
    
    public static boolean isRunAsUserTheSystemUser()
    {
        String runAsUser = getRunAsUser();
        if ((runAsUser != null) && isMtEnabled())
        {
            // get base username
            int idx = runAsUser.indexOf(TenantService.SEPARATOR);
            if (idx != -1)
            {
                runAsUser = runAsUser.substring(0, idx);
            }
        }
        return EqualsHelper.nullSafeEquals(runAsUser, AuthenticationUtil.SYSTEM_USER_NAME);
    }
    
    /**
     * Get the fully authenticated user. 
     * It returns the name of the user that last authenticated and excludes any overlay authentication set
     * by {@link #runAs(org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork, String) runAs}.
     * 
     * @return              Returns the name of the authenticated user
     * @throws AuthenticationException
     */
    public static String getFullyAuthenticatedUser() throws AuthenticationException
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
    
    /**
     * Get the name of the system user
     * 
     * @return system user name
     */
    public static String getSystemUserName()
    {
        return SYSTEM_USER_NAME;
    }
    
    /**
     * Get the name of the default admin user (the admin user created during bootstrap)
     * 
     * @return admin user name
     */
    public static String getAdminUserName()
    {
        if (!initialized)
        {
            throw new IllegalStateException("AuthenticationUtil not yet initialised; default admin username not available");
        }
        
        if (isMtEnabled())
        {
            String runAsUser = AuthenticationUtil.getRunAsUser();
            if (runAsUser != null)
            {
                String tenantDomain = AuthenticationUtil.getUserTenant(runAsUser).getSecond();
                
                if (! TenantService.DEFAULT_DOMAIN.equals(tenantDomain))
                {
                    return defaultAdminUserName + TenantService.SEPARATOR + tenantDomain;
                }
            }
        }
        
        return defaultAdminUserName;
    }

    /*
     * Get the name of admin role
     */
    public static String getAdminRoleName()
    {
        return PermissionService.ADMINISTRATOR_AUTHORITY;
    }
    
    /**
     * Get the name of the Guest User
     */
    public static String getGuestUserName()
    {
        if (!initialized)
        {
            throw new IllegalStateException("AuthenticationUtil not yet initialised; default guest username not available");
        }
        return defaultGuestUserName;
    }
    
    /**
     * Get the name of the guest role
     */
    public static String getGuestRoleName()
    {
        return PermissionService.GUEST_AUTHORITY;
    }

    /**
     * Remove the current security information
     */
    public static void clearCurrentSecurityContext()
    {
        if (s_logger.isDebugEnabled())
            s_logger.debug("Removing the current security information.");
        ContextHolder.setContext(null);
        InMemoryTicketComponentImpl.clearCurrentSecurityContext();
        
        NDC.remove();
        
        TenantContextHolder.clearTenantDomain();
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
        Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
        Authentication originalRunAsAuthentication = AuthenticationUtil.getRunAsAuthentication();
        
        final R result;
        try
        {
            if (originalFullAuthentication == null)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(uid);
            }
            else
            {
                // TODO remove - this should be obsolete now we're using TenantContextHolder
                /*
                if ((originalRunAsAuthentication != null) && (isMtEnabled()))
                {
                    String originalRunAsUserName = getUserName(originalRunAsAuthentication);
                    int idx = originalRunAsUserName.indexOf(TenantService.SEPARATOR);
                    if ((idx != -1) && (idx < (originalRunAsUserName.length() - 1)))
                    {
                        if (uid.equals(AuthenticationUtil.getSystemUserName()))
                        {
                            uid = uid + TenantService.SEPARATOR + originalRunAsUserName.substring(idx + 1);
                        }
                    }
                }
                */
                AuthenticationUtil.setRunAsUser(uid);
            }
            logNDC(uid);
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
            if (originalFullAuthentication == null)
            {
                AuthenticationUtil.clearCurrentSecurityContext();
                logNDC(null);
            }
            else
            {
                AuthenticationUtil.setFullAuthentication(originalFullAuthentication);
                AuthenticationUtil.setRunAsAuthentication(originalRunAsAuthentication);
                
                logNDC(getUserName(originalFullAuthentication));
            }
        }
    }
    
    public static <R> R runAsSystem(RunAsWork<R> runAsWork)
    {
        return runAs(runAsWork, getSystemUserName());
    }
    
    static class ThreadLocalStack extends ThreadLocal<Stack<Authentication>>
    {
        /* (non-Javadoc)
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected Stack<Authentication> initialValue()
        {
            return new Stack<Authentication>();
        }
    }
    
    private static ThreadLocal<Stack<Authentication>> threadLocalFullAuthenticationStack = new ThreadLocalStack();
    private static ThreadLocal<Stack<Authentication>> threadLocalRunAsAuthenticationStack = new ThreadLocalStack();
    
    private static ThreadLocal<Stack<String>> threadLocalTenantDomainStack = new ThreadLocal<Stack<String>>()
                                                                                    {
                                                                                        @Override
                                                                                        protected Stack<String> initialValue()
                                                                                        {
                                                                                            return new Stack<String>();
                                                                                        }
                                                                                    };
    
    /**
     * Push the current authentication context onto a threadlocal stack.
     */
    public static void pushAuthentication()
    {
        Authentication originalFullAuthentication = AuthenticationUtil.getFullAuthentication();
        Authentication originalRunAsAuthentication = AuthenticationUtil.getRunAsAuthentication();
        threadLocalFullAuthenticationStack.get().push(originalFullAuthentication);
        threadLocalRunAsAuthenticationStack.get().push(originalRunAsAuthentication);
        
        threadLocalTenantDomainStack.get().push(TenantContextHolder.getTenantDomain());
    }
    
    /**
     * Pop the authentication context from a threadlocal stack.
     */
    public static void popAuthentication()
    {
        Authentication originalFullAuthentication = threadLocalFullAuthenticationStack.get().pop();
        Authentication originalRunAsAuthentication = threadLocalRunAsAuthenticationStack.get().pop();
        
        if (originalFullAuthentication == null)
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
        else
        {
            AuthenticationUtil.setFullAuthentication(originalFullAuthentication);
            AuthenticationUtil.setRunAsAuthentication(originalRunAsAuthentication);
        }
        
        String originalTenantDomain = threadLocalTenantDomainStack.get().pop();
        TenantContextHolder.setTenantDomain(originalTenantDomain);
    }

    /**
     * Logs the current authenticated users
     */
    public static void logAuthenticatedUsers()
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(
                    "Authentication: \n" +
                    "   Fully authenticated: " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                    "   Run as:              " + AuthenticationUtil.getRunAsUser());
        }
    }

    public static void logNDC(String userNameIn)
    {
        NDC.remove();
        
        if (userNameIn != null)
        {
            if (isMtEnabled())
            {
                Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userNameIn);
                final String userName = userTenant.getFirst();
                final String tenantDomain = userTenant.getSecond();
                if (! TenantService.DEFAULT_DOMAIN.equals(tenantDomain))
                {
                    NDC.push("Tenant:" +tenantDomain + " User:" + userName);
                }
                else
                {
                    NDC.push("User:" + userName);
                }
            }
            else
            {
                NDC.push("User:" + userNameIn);
            }
        }
    }
    
    //
    // Return username and current tenant domain. If current tenant domain is not set then
    // get implied tenant domain. For example: bob@acme.com  => bob@acme.com, acme.com
    //
    public static Pair<String, String> getUserTenant(String userName)
    {
        String tenantDomain = TenantContextHolder.getTenantDomain();
        if (tenantDomain == null)
        {
            tenantDomain = TenantService.DEFAULT_DOMAIN;
            
            if ((userName != null) && isMtEnabled())
            {
                // MT implied domain from username (for backwards compatibility)
                int idx = userName.indexOf(TenantService.SEPARATOR);
                if ((idx > 0) && (idx < (userName.length()-1)))
                {
                    tenantDomain = userName.substring(idx+1);
                    if (tenantDomain.indexOf(TenantService.SEPARATOR) > 0)
                    {
                        throw new AlfrescoRuntimeException("Unexpected tenant: "+tenantDomain+" (contains @)");
                    }
                    
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Tenant domain implied: userName="+userName+", tenantDomain="+tenantDomain);
                    }
                }
            }
        }
        return new Pair<String, String>(userName, tenantDomain);
    }
}
