package org.alfresco.service.cmr.security;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.Auditable;

/**
 * An extended {@link AuthenticationService} that allows mutation of some or all of its user accounts.
 * 
 * @author dward
 */
@AlfrescoPublicApi
public interface MutableAuthenticationService extends AuthenticationService
{
    /**
     * Determines whether this user's authentication may be mutated via the other methods.
     * 
     * @param userName the user ID
     * @return <code>true</code> if this user's authentication may be mutated via the other methods.
     */
    @Auditable(parameters = {"userName"}, recordable = {true})
    public boolean isAuthenticationMutable(String userName);
    
    /**
     * Determines whether authentication creation is allowed.
     * 
     * @return <code>true</code> if authentication creation is allowed
     */
    @Auditable
    public boolean isAuthenticationCreationAllowed();
    
    /**
     * Create an authentication for the given user.
     * 
     * @param userName String
     * @param password char[]
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "password"}, recordable = {true, false})
    public void createAuthentication(String userName, char[] password) throws AuthenticationException;
    
    /**
     * Update the login information for the user (typically called by the user)
     * 
     * @param userName String
     * @param oldPassword char[]
     * @param newPassword char[]
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "oldPassword", "newPassword"}, recordable = {true, false, false})
    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword) throws AuthenticationException;
    
    /**
     * Set the login information for a user (typically called by an admin user) 
     * 
     * @param userName String
     * @param newPassword char[]
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "newPassword"}, recordable = {true, false})
    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException;
    

    /**
     * Delete an authentication entry
     * 
     * @param userName String
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName"})
    public void deleteAuthentication(String userName) throws AuthenticationException;
    
    /**
     * Enable or disable an authentication entry
     * 
     * @param userName String
     * @param enabled boolean
     */
    @Auditable(parameters = {"userName", "enabled"})
    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException;
    
    
}
