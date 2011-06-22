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
     */
    public void createUser(String userName, char[] rawPassword) throws AuthenticationException;
    
    /**
     * Update a user's password.
     */
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException;
    
    /**
     * Delete a user.
     */
    public void deleteUser(String userName) throws AuthenticationException;
    
    /**
     * Check is a user exists.
     */
    public boolean userExists(String userName);
    
    /**
     * Enable/disable a user.
     */
    public void setEnabled(String userName, boolean enabled);
    
    /**
     * Getter for user enabled
     */
    public boolean getEnabled(String userName);
    
    /**
     * Set if the account should expire
     */
    public void setAccountExpires(String userName, boolean expires);
    
    /**
     * Does the account expire?
     */
    public boolean getAccountExpires(String userName);
    
    /**
     * Has the account expired?
     */
    public boolean getAccountHasExpired(String userName);
  
    /**
     * Set if the password expires.
     */
    public void setCredentialsExpire(String userName, boolean expires);
  
    /**
     * Do the credentials for the user expire?
     */
    public boolean getCredentialsExpire(String userName);
    
    /**
     * Have the credentials for the user expired?
     */
    public boolean getCredentialsHaveExpired(String userName);
    
    /**
     * Set if the account is locked.
     */
    public void setLocked(String userName, boolean locked);
    
    /**
     * Check if the account is locked
     * 
     * @param userName          the username
     * 
     * @since 4.0
     */
    public boolean getLocked(String userName);
    
    /**
     * Is the account locked?
     * 
     * @deprecated Use {@link #getLocked(String)}
     */
    public boolean getAccountlocked(String userName);
    
    /**
     * Set the date on which the account expires
     */
    public void setAccountExpiryDate(String userName, Date exipryDate);
    
    /** 
     * Get the date when this account expires.
     */
    public Date getAccountExpiryDate(String userName);
    
    /**
     * Set the date when credentials expire.
     */
    public void setCredentialsExpiryDate(String userName, Date exipryDate);
    
    /**
     * Get the date when the credentials/password expire.
     */
    public Date getCredentialsExpiryDate(String userName);
    
    /**
     * Get the MD4 password hash
     */
    public String getMD4HashedPassword(String userName);
}
