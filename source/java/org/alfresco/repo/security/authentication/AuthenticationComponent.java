/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import java.util.Set;

import net.sf.acegisecurity.Authentication;

public interface AuthenticationComponent extends AuthenticationContext
{
    public enum UserNameValidationMode
    {
        NONE, CHECK, CHECK_AND_FIX;
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
