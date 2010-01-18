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
package org.alfresco.service.cmr.security;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.Auditable;

/**
 * An extended {@link AuthenticationService} that allows mutation of some or all of its user accounts.
 * 
 * @author dward
 */
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
    
    
}
