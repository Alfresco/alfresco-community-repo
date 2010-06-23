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
package org.alfresco.repo.domain.avm;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.locking.AVMLockingServiceImpl;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.util.Pair;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstract implementation for AVMLock DAO.
 *
 * @author janv
 * @since 3.4
 */
public abstract class AbstractAVMLockDAOImpl implements AVMLockDAO
{
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private PropertyValueDAO propertyValueDAO;
    
    public void setPropertyValueDAO(PropertyValueDAO propertyValueDAO)
    {
        this.propertyValueDAO = propertyValueDAO;
    }
    
    /**
     * Default constructor.
     */
    public AbstractAVMLockDAOImpl()
    {
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeLocks(String avmStore, String dirPathToMatch, final Map<String, String> lockDataToMatch)
    {
        Long avmLocksValueId = null;
        Pair<Long, Serializable> valuePair = propertyValueDAO.getPropertyValue(AVMLockingServiceImpl.KEY_AVM_LOCKS);
        if (valuePair == null)
        {
            // No such value, so no need to delete
            return;
        }
        avmLocksValueId = valuePair.getFirst();
        
        Long avmStoreNameId = null;
        valuePair = propertyValueDAO.getPropertyValue(avmStore);
        if (valuePair == null)
        {
            // No such value, so no need to delete
            return;
        }
        avmStoreNameId = valuePair.getFirst();
        
        String lockDataStoreKey = null;
        String lockDataStoreValue = null;
        
        if ((lockDataToMatch != null) && (lockDataToMatch.size() > 0))
        {
            lockDataStoreKey = WCMUtil.LOCK_KEY_STORE_NAME;
            
            if ((lockDataToMatch.size() != 1) || (! lockDataToMatch.containsKey(lockDataStoreKey)))
            {
                throw new AlfrescoRuntimeException("Expected lockData to contain either no entries or only one entry with key: "+lockDataStoreKey);
            }
            
            lockDataStoreValue = lockDataToMatch.get(lockDataStoreKey);
        }
        
        int deleted = deletePropertyUniqueContexts(avmLocksValueId, avmStoreNameId, dirPathToMatch, lockDataStoreKey, lockDataStoreValue);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Deleted " + deleted + " unique property contexts: \n" +
                    "   dirPathToMatch:  " + dirPathToMatch + "\n" +
                    "   lockDataToMatch: " + lockDataToMatch);
        }
    }
    
    protected abstract int deletePropertyUniqueContexts(Long avmLocksValueId, Long avmStoreNameId, String dirPathToMatch, String lockDataStoreKey, String lockDataStoreValue);
}
