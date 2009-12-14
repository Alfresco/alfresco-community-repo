/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm.ibatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreProperty;
import org.alfresco.repo.avm.AVMStorePropertyDAO;
import org.alfresco.repo.avm.AVMStorePropertyImpl;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.QNameDAO;
import org.alfresco.repo.domain.avm.AVMStorePropertyEntity;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * iBATIS DAO wrapper for AVMStoreProperty
 * 
 * @author janv
 */
class AVMStorePropertyDAOIbatis extends HibernateDaoSupport implements AVMStorePropertyDAO
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
