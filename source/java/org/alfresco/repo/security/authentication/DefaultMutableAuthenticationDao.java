/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.authentication;

import java.util.Date;

import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.dao.DataAccessException;

/**
 * An authority DAO that has no implementation and should not be called.
 * 
 * @author Andy Hind
 */
public class DefaultMutableAuthenticationDao implements MutableAuthenticationDao
{
    
    
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
    }
    
    /**
     * Are user names case sensitive?
     * 
     * @return
     */
    public boolean getUserNamesAreCaseSensitive()
    {
        throw new AlfrescoRuntimeException("Not implemented");
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
    }
}
