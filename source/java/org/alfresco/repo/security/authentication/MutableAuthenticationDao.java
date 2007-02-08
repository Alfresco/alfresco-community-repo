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

import java.util.Date;

import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import net.sf.acegisecurity.providers.dao.SaltSource;

/**
 * A service provider interface to provide both acegi integration via AuthenticationDao and SaltSource
 * and mutability support for user definitions. 
 * 
 * @author Andy Hind
 */
public interface MutableAuthenticationDao extends AuthenticationDao, SaltSource
{
    /**
     * Create a user with the given userName and password
     * 
     * @param userName
     * @param rawPassword
     * @throws AuthenticationException
     */
    public void createUser(String userName, char[] rawPassword) throws AuthenticationException;
    
    /**
     * Update a user's password.
     * 
     * @param userName
     * @param rawPassword
     * @throws AuthenticationException
     */
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException;
    
    /**
     * Delete a user.
     * 
     * @param userName
     * @throws AuthenticationException
     */
    public void deleteUser(String userName) throws AuthenticationException;
    
    /**
     * CHeck is a user exists.
     * 
     * @param userName
     * @return
     */
    public boolean userExists(String userName);
    
    /**
     * Enable/disable a user.
     * 
     * @param userName
     * @param enabled
     */
    public void setEnabled(String userName, boolean enabled);
    
    /**
     * Getter for user enabled
     * 
     * @param userName
     * @return
     */
    public boolean getEnabled(String userName);
    
    /**
     * Set if the account should expire
     * 
     * @param userName
     * @param expires
     */
    public void setAccountExpires(String userName, boolean expires);
    
    /**
     * Does the account expire?
     * 
     * @param userName
     * @return
     */
            
    public boolean getAccountExpires(String userName);
    
    /**
     * Has the account expired?
     * 
     * @param userName
     * @return
     */
    public boolean getAccountHasExpired(String userName);
  
    /**
     * Set if the password expires.
     * 
     * @param userName
     * @param expires
     */
    public void setCredentialsExpire(String userName, boolean expires);
  
    /**
     * Do the credentials for the user expire?
     * 
     * @param userName
     * @return
     */
    public boolean getCredentialsExpire(String userName);
    
    /**
     * Have the credentials for the user expired?
     * 
     * @param userName
     * @return
     */
    public boolean getCredentialsHaveExpired(String userName);
    
    /**
     * Set if the account is locked.
     * 
     * @param userName
     * @param locked
     */
    public void setLocked(String userName, boolean locked);
    
    /**
     * Is the account locked?
     * 
     * @param userName
     * @return
     */
    public boolean getAccountlocked(String userName);
    
    /**
     * Set the date on which the account expires
     * 
     * @param userName
     * @param exipryDate
     */
    public void setAccountExpiryDate(String userName, Date exipryDate);
    
    /** 
     * Get the date when this account expires.
     * 
     * @param userName
     * @return
     */
    public Date getAccountExpiryDate(String userName);
    
    /**
     * Set the date when credentials expire.
     * 
     * @param userName
     * @param exipryDate
     */
    public void setCredentialsExpiryDate(String userName, Date exipryDate);
    
    /**
     * Get the date when the credentials/password expire.
     * 
     * @param userName
     * @return
     */
    public Date getCredentialsExpiryDate(String userName);
    
    /**
     * Get the MD4 password hash
     * 
     * @param userName
     * @return
     */
    public String getMD4HashedPassword(String userName);
    
}
