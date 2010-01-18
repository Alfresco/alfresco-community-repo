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
     * Invalidate a single ticket by ID or remove its association with a given session ID.
     *
     * @param ticket
     * @param sessionId
     *            the app server session ID (e.g. HttpSession ID) or <code>null</code> if not applicable.
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"ticket", "sessionId"}, recordable = {false, false})
    public void invalidateTicket(String ticket, String sessionId) throws AuthenticationException;
    
    /**
     * Validate a ticket and associate it with a given app server session ID. Set the current user name accordingly. 
     * 
     * @param ticket
     * @param sessionId
     *            the app server session ID (e.g. HttpSession ID) or <code>null</code> if not applicable.
     * @throws AuthenticationException
     */
    @Auditable(parameters = {"ticket", "sessionId"}, recordable = {false, false})
    public void validate(String ticket, String sessionId) throws AuthenticationException;

    /**
     * Gets the current ticket as a string. If there isn't an appropriate current ticket, a new ticket will be made the
     * current ticket.
     * 
     * @param sessionId
     *            the app server session ID (e.g. HttpSession ID) or <code>null</code> if not applicable. If non-null,
     *            the ticket returned is either a new one or one previously associated with the same sessionId by
     *            {@link #validate(String, String)} or {@link #getCurrentTicket(String)}.
     * @return the current ticket as a string
     */
    @Auditable(parameters = {"sessionId"}, recordable = {false})
    public String getCurrentTicket(String sessionId);
    
    /**
     * Gets the current ticket as a string. If there isn't an appropriate current ticket, a new ticket will be made the
     * current ticket.
     * 
     * @return the current ticket as a string
     */
    @Auditable
    public String getCurrentTicket();

    /**
     * Get a new ticket as a string
     * @param sessionId
     *            the app server session ID (e.g. HttpSession ID) or <code>null</code> if not applicable.
     * @return
     */
    @Auditable(parameters = {"sessionId"}, recordable = {false})
    public String getNewTicket(String sessionId);
    
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

