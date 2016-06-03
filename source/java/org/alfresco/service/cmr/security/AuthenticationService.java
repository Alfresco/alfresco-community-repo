package org.alfresco.service.cmr.security;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.Auditable;

/**
 * The authentication service defines the API for managing authentication information 
 * against a user id. 
 *  
 * @author Andy Hind
 */
@AlfrescoPublicApi
public interface AuthenticationService
{
    /**
     * Is an authentication enabled or disabled?
     */
    @Auditable(parameters = {"userName"})
    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException;
    
    /**
     * Carry out an authentication attempt. If successful the user is set to the current user.
     * The current user is a part of the thread context.
     * 
     * @param userName the username
     * @param password the passowrd
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "password"}, recordable = {true, false})
    public void authenticate(String userName, char[] password) throws AuthenticationException;
    
    /**
     * Authenticate as the guest user. This may not be allowed and throw an exception.
     * 
     * @throws AuthenticationException
     */
    @Auditable
    public void authenticateAsGuest() throws AuthenticationException;
    
    /**
     * Check if Guest user authentication is allowed.
     * 
     * @return true if Guest user authentication is allowed, false otherwise
     */
    @Auditable
    public boolean guestUserAuthenticationAllowed();
    
    /**
     * Check if the given authentication exists.
     * 
     * @param userName the username
     * @return Returns <tt>true</tt> if the authentication exists
     */
    @Auditable(parameters = {"userName"})
    public boolean authenticationExists(String userName);
    
    /**
     * Get the name of the currently authenticated user.
     * 
     * @return String
     * @throws AuthenticationException
     */
    @Auditable
    public String getCurrentUserName() throws AuthenticationException;
    
    /**
     * Invalidate any tickets held by the user.
     */
    @Auditable(parameters = {"userName"})
    public void invalidateUserSession(String userName) throws AuthenticationException;
    
   /**
    * Invalidate a single ticket by ID
    * 
    * @param ticket String
    * @throws AuthenticationException
    */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public void invalidateTicket(String ticket) throws AuthenticationException;
    
   /**
    * Validate a ticket. Set the current user name accordingly. 
    * 
    * @param ticket String
    * @throws AuthenticationException
    */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public void validate(String ticket) throws AuthenticationException;
    
    /**
     * Get the current ticket as a string
     * @return String
     */
    @Auditable
    public String getCurrentTicket();
    
    /**
     * Get a new ticket as a string
     * @return String
     */
    @Auditable
    public String getNewTicket();
    
    /**
     * Remove the current security information
     */
    @Auditable
    public void clearCurrentSecurityContext();
    
    /**
     * Is the current user the system user?
     */
    @Auditable
    public boolean isCurrentUserTheSystemUser();
 
    /**
     * Get the domain to which this instance of an authentication service applies.
     * 
     * @return The domain name
     */
    @Auditable
    public Set<String> getDomains();
    
    /**
     * Does this instance alow user to be created?
     */
    @Auditable
    public Set<String> getDomainsThatAllowUserCreation();
    
    /**
     * Does this instance allow users to be deleted?
     */
    @Auditable
    public Set<String>  getDomainsThatAllowUserDeletion();
    
    /**
     * Does this instance allow users to update their passwords?
     */
    @Auditable
    public Set<String> getDomiansThatAllowUserPasswordChanges();
    
    /**
     * Gets a set of user names who should be considered 'administrators' by default.
     * 
     * @return a set of user names
     */
    @Auditable
    public Set<String> getDefaultAdministratorUserNames();
    
    /**
     * Gets a set of user names who should be considered 'guests' by default.
     * 
     * @return a set of user names
     */
    @Auditable
    public Set<String> getDefaultGuestUserNames();
}

