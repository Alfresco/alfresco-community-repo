/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.security;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * The authentication service defines the API for managing authentication information 
 * against a user id. 
 *  
 * @author Andy Hind
 */
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
     * @return
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
    * @param ticket
    * @throws AuthenticationException
    */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public void invalidateTicket(String ticket) throws AuthenticationException;
    
   /**
    * Validate a ticket. Set the current user name accordingly. 
    * 
    * @param ticket
    * @throws AuthenticationException
    */
    @Auditable(parameters = {"ticket"}, recordable = {false})
    public void validate(String ticket) throws AuthenticationException;
    
    /**
     * Get the current ticket as a string
     * @return
     */
    @Auditable
    public String getCurrentTicket();
    
    /**
     * Get a new ticket as a string
     * @return
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

