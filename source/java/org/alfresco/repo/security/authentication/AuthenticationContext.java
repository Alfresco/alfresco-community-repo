package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.UserDetails;

/**
 * Low-level interface allowing control and retrieval of the authentication information held for the current thread.
 * 
 * @author dward
 */
public interface AuthenticationContext
{
    /**
     * Remove the current security information
     */
    public void clearCurrentSecurityContext();

    /**
     * Explicitly set the current suthentication. If the authentication is <tt>null</tt> the the current authentication
     * is {@link #clearCurrentSecurityContext() cleared}.
     * 
     * @param authentication
     *            the current authentication (may be <tt>null</tt>).
     * @return Returns the modified authentication instance or <tt>null</tt> if it was cleared.
     */
    public Authentication setCurrentAuthentication(Authentication authentication);

    /**
     * Explicitly set the given validated user details to be authenticated.
     * 
     * @param ud
     *            the User Details
     * @return Authentication
     */
    public Authentication setUserDetails(UserDetails ud);

    /**
     * @throws AuthenticationException
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException;

    /**
     * Set the system user as the current user.
     */
    public Authentication setSystemUserAsCurrentUser();

    /**
     * Set the system user as the current user.
     */
    public Authentication setSystemUserAsCurrentUser(String tenantDomain);

    /**
     * Get the name of the system user. Note: for MT, will get system for default domain only
     */
    public String getSystemUserName();

    /**
     * Get the name of the system user
     */
    public String getSystemUserName(String tenantDomain);

    /**
     * True if this is the System user ?
     */
    public boolean isSystemUserName(String userName);
    
    /**
     * Is the current user the system user?
     */
    public boolean isCurrentUserTheSystemUser();

    /**
     * Get the name of the Guest User. Note: for MT, will get guest for default domain only
     */
    public String getGuestUserName();

    /**
     * Get the name of the guest user
     */
    public String getGuestUserName(String tenantDomain);

    /**
     * True if this is a guest user ?
     */
    public boolean isGuestUserName(String userName);

    /**
     * Get the current user name.
     * 
     * @throws AuthenticationException
     */
    public String getCurrentUserName() throws AuthenticationException;

    /**
     * Extracts the tenant domain name from a user name
     * 
     * @param userName
     *            a user name
     * @return a tenant domain name
     */
    public String getUserDomain(String userName);

}
