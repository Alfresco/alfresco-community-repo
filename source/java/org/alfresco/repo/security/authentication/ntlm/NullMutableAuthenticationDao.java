/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.security.authentication.ntlm;

import java.util.Date;

import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.dao.DataAccessException;

/**
 * Null Mutable Authentication Dao Class
 * 
 * <p>Mutable authentication implementation that does nothing.
 * 
 * @author GKSpencer
 */
public class NullMutableAuthenticationDao implements MutableAuthenticationDao
{
    /**
     * Method kept just for backward compatibility with older configurations that
     * might have been passing in a value.
     * 
     * @param nodeService ignored
     */
    public void setNodeService(NodeService nodeService)
    {
        // do nothing
    }

    /**
     * Create a user with the given userName and password
     * 
     * @param userName
     * @param rawPassword
     * @throws AuthenticationException
     */
    public void createUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Update a user's password.
     * 
     * @param userName
     * @param rawPassword
     * @throws AuthenticationException
     */
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Delete a user.
     * 
     * @param userName
     * @throws AuthenticationException
     */
    public void deleteUser(String userName) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Check is a user exists.
     * 
     * @param userName
     * @return
     */
    public boolean userExists(String userName)
    {
        return true;
    }
    
    /**
     * Enable/disable a user.
     * 
     * @param userName
     * @param enabled
     */
    public void setEnabled(String userName, boolean enabled)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Getter for user enabled
     * 
     * @param userName
     * @return
     */
    public boolean getEnabled(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return true;
    }
    
    /**
     * Set if the account should expire
     * 
     * @param userName
     * @param expires
     */
    public void setAccountExpires(String userName, boolean expires)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Does the account expire?
     * 
     * @param userName
     * @return
     */
            
    public boolean getAccountExpires(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return false;
    }
    
    /**
     * Has the account expired?
     * 
     * @param userName
     * @return
     */
    public boolean getAccountHasExpired(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return false;
    }
  
    /**
     * Set if the password expires.
     * 
     * @param userName
     * @param expires
     */
    public void setCredentialsExpire(String userName, boolean expires)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
  
    /**
     * Do the credentials for the user expire?
     * 
     * @param userName
     * @return
     */
    public boolean getCredentialsExpire(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return false;
    }
    
    /**
     * Have the credentials for the user expired?
     * 
     * @param userName
     * @return
     */
    public boolean getCredentialsHaveExpired(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return false;
    }
    
    /**
     * Set if the account is locked.
     * 
     * @param userName
     * @param locked
     */
    public void setLocked(String userName, boolean locked)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Is the account locked?
     * 
     * @param userName
     * @return
     */
    public boolean getAccountlocked(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return false;
    }
    
    /**
     * Set the date on which the account expires
     * 
     * @param userName
     * @param exipryDate
     */
    public void setAccountExpiryDate(String userName, Date exipryDate)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /** 
     * Get the date when this account expires.
     * 
     * @param userName
     * @return
     */
    public Date getAccountExpiryDate(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return null;
    }
    
    /**
     * Set the date when credentials expire.
     * 
     * @param userName
     * @param exipryDate
     */
    public void setCredentialsExpiryDate(String userName, Date exipryDate)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
        // Nothing to do
    }
    
    /**
     * Get the date when the credentials/password expire.
     * 
     * @param userName
     * @return
     */
    public Date getCredentialsExpiryDate(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return null;
    }
    
    /**
     * Get the MD4 password hash
     * 
     * @param userName
     * @return
     */
    public String getMD4HashedPassword(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return null;
    }

    /**
     * Return the user details for the specified user
     * 
     * @param user String
     * @return UserDetails
     * @exception UsernameNotFoundException
     * @exception DataAccessException
     */
    public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException, DataAccessException
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return null;
    }

    /**
     * Return salt for user
     * 
     * @param user UserDetails
     * @return Object
     */
    public Object getSalt(UserDetails user)
    {
        throw new AlfrescoRuntimeException("Not implemented");
        
//        return null;
    }
}
