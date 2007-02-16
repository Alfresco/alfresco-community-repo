/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 *
 */
@PublicService
public interface AuthenticationService
{
    /**
     * Create an authentication for the given user.
     * 
     * @param userName
     * @param password
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "password"}, recordable = {true, false})
    public void createAuthentication(String userName, char[] password) throws AuthenticationException;
    
    /**
     * Update the login information for the user (typically called by the user)
     * 
     * @param userName
     * @param oldPassword
     * @param newPassword
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "oldPassword", "newPassword"}, recordable = {true, false, false})
    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword) throws AuthenticationException;
    
    /**
     * Set the login information for a user (typically called by an admin user) 
     * 
     * @param userName
     * @param newPassword
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName", "newPassword"}, recordable = {true, false})
    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException;
    

    /**
     * Delete an authentication entry
     * 
     * @param userName
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"userName"})
    public void deleteAuthentication(String userName) throws AuthenticationException;
    
    /**
     * Enable or disable an authentication entry
     * 
     * @param userName
     * @param enabled
     */
    @Auditable(parameters = {"userName", "enabled"})
    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException;
    
    /**
     * Is an authentication enabled or disabled?
     * 
     * @param userName
     * @return
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
     * 
     * @param userName
     * @throws AuthenticationException
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
     * Remove the current security information
     *
     */
    @Auditable
    public void clearCurrentSecurityContext();
    
    /**
     * Is the current user the system user?
     * 
     * @return
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
     * 
     * @return
     */
    @Auditable
    public Set<String> getDomainsThatAllowUserCreation();
    
    /**
     * Does this instance allow users to be deleted?
     * 
     * @return
     */
    @Auditable
    public Set<String>  getDomainsThatAllowUserDeletion();
    
    /**
     * Does this instance allow users to update their passwords?
     * 
     * @return
     */
    @Auditable
    public Set<String> getDomiansThatAllowUserPasswordChanges();
}

