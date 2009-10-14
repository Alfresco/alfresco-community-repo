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

import java.util.Set;

import net.sf.acegisecurity.Authentication;

public interface AuthenticationComponent extends AuthenticationContext
{
    public enum UserNameValidationMode
    {
        CHECK, CHECK_AND_FIX;
    }
    
    /**
     * Authenticate
     * 
     * @throws AuthenticationException
     */     
    public void authenticate(String userName, char[] password) throws AuthenticationException;
    
    /**
     * Explicitly set the current user to be authenticated.
     */
    
    public Authentication setCurrentUser(String userName);
    
    /**
     * Explicitly set the current user to be authenticated.
     * Specify if the userName is to be checked and fixed
     */
    
    public Authentication setCurrentUser(String userName, UserNameValidationMode validationMode);
    
    
    /**
     * Set the guest user as the current user.
     */
    public Authentication setGuestUserAsCurrentUser();
    
    
    /**
     * True if Guest user authentication is allowed, false otherwise
     */
    public boolean guestUserAuthenticationAllowed();
    
    /**
     * Gets a set of user names who for this particular authentication system should be considered administrators by
     * default. If the security framework is case sensitive these values should be case sensitive user names. If the
     * security framework is not case sensitive these values should be the lower-case user names.
     * 
     * @return a set of user names
     */
    public Set<String> getDefaultAdministratorUserNames();
    
    /**
     * Gets a set of user names who for this particular authentication system should be considered guests by
     * default. If the security framework is case sensitive these values should be case sensitive user names. If the
     * security framework is not case sensitive these values should be the lower-case user names.
     * 
     * @return a set of user names
     */
    public Set<String> getDefaultGuestUserNames();
}
