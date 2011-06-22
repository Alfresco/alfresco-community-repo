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
     * @param nodeService ignored
     */
    public void setNodeService(NodeService nodeService)
    {
        // do nothing
    }

    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void createUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void deleteUser(String userName) throws AuthenticationException
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * Check is a user exists.
     * 
     * @return              <tt>true</tt> always
     */
    @Override
    public boolean userExists(String userName)
    {
        return true;
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setEnabled(String userName, boolean enabled)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getEnabled(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setAccountExpires(String userName, boolean expires)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getAccountExpires(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getAccountHasExpired(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
  
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setCredentialsExpire(String userName, boolean expires)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
  
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getCredentialsExpire(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getCredentialsHaveExpired(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setLocked(String userName, boolean locked)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }

    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getLocked(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }

    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public boolean getAccountlocked(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setAccountExpiryDate(String userName, Date exipryDate)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public Date getAccountExpiryDate(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public void setCredentialsExpiryDate(String userName, Date exipryDate)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public Date getCredentialsExpiryDate(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
    
    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public String getMD4HashedPassword(String userName)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }

    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public UserDetails loadUserByUsername(String arg0) throws UsernameNotFoundException, DataAccessException
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }

    /**
     * @throws AlfrescoRuntimeException Not implemented
     */
    @Override
    public Object getSalt(UserDetails user)
    {
        throw new AlfrescoRuntimeException("Not implemented");
    }
}
