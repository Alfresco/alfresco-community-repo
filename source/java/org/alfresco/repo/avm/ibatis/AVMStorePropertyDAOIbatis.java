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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm.ibatis;

import java.util.Map;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreProperty;
import org.alfresco.repo.avm.AVMStorePropertyDAO;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * iBATIS DAO wrapper for AVMStoreProperty
 * 
 * @author janv
 */
class AVMStorePropertyDAOIbatis implements AVMStorePropertyDAO
{
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#save(org.alfresco.repo.avm.AVMStoreProperty)
     */
    public void save(AVMStoreProperty prop)
    {
        AVMDAOs.Instance().newAVMStoreDAO.createOrUpdateStoreProperty(prop.getStore().getId(), prop.getQname(), prop.getValue());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#get(org.alfresco.repo.avm.AVMStore, org.alfresco.service.namespace.QName)
     */
    public PropertyValue get(AVMStore store, QName name)
    {
        return AVMDAOs.Instance().newAVMStoreDAO.getStoreProperty(store.getId(), name);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#get(org.alfresco.repo.avm.AVMStore)
     */
    public Map<QName, PropertyValue> get(AVMStore store)
    {
        return AVMDAOs.Instance().newAVMStoreDAO.getStoreProperties(store.getId());
    }

    /**
     * Query store properties by key pattern.
     * @param store The store.
     * @param keyPattern An sql 'like' pattern wrapped up in a QName
     * @return A List of matching AVMStoreProperties.
     */
    public Map<QName, PropertyValue> queryByKeyPattern(AVMStore store, QName keyPattern)
    {
        // Get the URI and LocalName parts
        String uri = keyPattern.getNamespaceURI();
        if (uri == null || uri.length() == 0)
        {
            uri = "%";
        }
        String localName = keyPattern.getLocalName();
        if (localName == null || localName.length() == 0)
        {
            localName = "%";
        }
        
        return AVMDAOs.Instance().newAVMStoreDAO.getStorePropertiesByStoreAndKeyPattern(store.getId(), uri, localName);
    }

    /**
     * Query all stores' properties by key pattern.
     * @param keyPattern The sql 'like' pattern wrapped up in a QName
     * @return A List of match AVMStoreProperties.
     */
    public Map<String, Map<QName, PropertyValue>> queryByKeyPattern(QName keyPattern)
    {
        // Get the URI and LocalName parts
        String uri = keyPattern.getNamespaceURI();
        if (uri == null || uri.length() == 0)
        {
            uri = "%";
        }
        String localName = keyPattern.getLocalName();
        if (localName == null || localName.length() == 0)
        {
            localName = "%";
        }
        
        return AVMDAOs.Instance().newAVMStoreDAO.getStorePropertiesByKeyPattern(uri, localName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#update(org.alfresco.repo.avm.AVMStoreProperty)
     */
    public void update(AVMStoreProperty prop)
    {
        // NOOP
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#delete(org.alfresco.repo.avm.AVMStore, org.alfresco.service.namespace.QName)
     */
    public void delete(AVMStore store, QName name)
    {
        AVMDAOs.Instance().newAVMStoreDAO.deleteStoreProperty(store.getId(), name);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStorePropertyDAO#delete(org.alfresco.repo.avm.AVMStore)
     */
    public void delete(AVMStore store)
    {
        AVMDAOs.Instance().newAVMStoreDAO.deleteStoreProperties(store.getId());
    }
}
