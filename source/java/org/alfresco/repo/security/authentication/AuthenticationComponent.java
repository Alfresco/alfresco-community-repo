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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.authentication;

import net.sf.acegisecurity.Authentication;

public interface AuthenticationComponent
{
    
    /**
     * Authenticate
     * 
     * @param userName
     * @param password
     * @throws AuthenticationException
     */     
    public void authenticate(String userName, char[] password) throws AuthenticationException;

    /**
     * Authenticate using a token
     * 
     * @param token Authentication
     * @return Authentication
     * @throws AuthenticationException
     */
    public Authentication authenticate(Authentication token) throws AuthenticationException;
    
    /**
     * Explicitly set the current user to be authenticated.
     */
    
    public Authentication setCurrentUser(String userName);
    
    /**
     * Remove the current security information
     *
     */
    public void clearCurrentSecurityContext();
    
    /**
     * Explicitly set the current suthentication.  If the authentication is <tt>null</tt> the
     * the current authentication is {@link #clearCurrentSecurityContext() cleared}.
     * 
     * @param authentication the current authentication (may be <tt>null</tt>).
     * 
     * @return Returns the modified authentication instance or <tt>null</tt> if it was cleared.
     */
    public Authentication setCurrentAuthentication(Authentication authentication);
    
    /**
     * 
     * @return
     * @throws AuthenticationException
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException;
    
    /**
     * Set the system user as the current user.
     * 
     * @return
     */
    public Authentication setSystemUserAsCurrentUser();
    
    
    /**
     * Set the guest user as the current user.
     * 
     * @return
     */
    public Authentication setGuestUserAsCurrentUser();
    
    
    /**
     * True if Guest user authentication is allowed, false otherwise
     * 
     * @return
     */
    public boolean guestUserAuthenticationAllowed();
    
    
    /**
     * Get the name of the system user
     * 
     * @return
     */
    public String getSystemUserName();
    
    
    /**
     * Get the name of the guest user
     * 
     * @return
     */
    public String getGuestUserName();
    
    /**
     * Get the current user name.
     * 
     * @return
     * @throws AuthenticationException
     */
    public String getCurrentUserName() throws AuthenticationException;
    
    /**
     * Get the enum that describes NTLM integration
     * 
     * @return
     */
    public NTLMMode getNTLMMode();
    
    /**
     * Get the MD4 password hash, as required by NTLM based authentication methods.
     * 
     * @param userName
     * @return
     */
    public String getMD4HashedPassword(String userName);
}
