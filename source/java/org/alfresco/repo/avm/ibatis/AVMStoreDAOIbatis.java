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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.AVMStoreDAO;
import org.alfresco.repo.avm.AVMStoreImpl;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.domain.avm.AVMStoreEntity;
import org.alfresco.repo.domain.permissions.Acl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * iBATIS DAO wrapper for AVMStore
 * 
 * @author janv
 */
class AVMStoreDAOIbatis implements AVMStoreDAO
{
    private static Log logger = LogFactory.getLog(AVMStoreDAOIbatis.class);
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#save(org.alfresco.repo.avm.AVMStore)
     */
    public void save(AVMStore store)
    {
        AVMStoreEntity storeEntity = AVMDAOs.Instance().newAVMStoreDAO.createStore(store.getName());
        ((AVMStoreImpl)store).setId(storeEntity.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#delete(org.alfresco.repo.avm.AVMStore)
     */
    public void delete(AVMStore store)
    {
        AVMDAOs.Instance().newAVMStoreDAO.deleteStore(store.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#getAll()
     */
    public List<AVMStore> getAll()
    {
        List<AVMStoreEntity> storeEntities = AVMDAOs.Instance().newAVMStoreDAO.getAllStores();
        List<AVMStore> result = new ArrayList<AVMStore>(storeEntities.size());
        for (AVMStoreEntity storeEntity : storeEntities)
        {
            result.add(getByID(storeEntity.getId()));
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#getByName(java.lang.String)
     */
    public AVMStore getByName(String name)
    {
        AVMStoreEntity storeEntity = AVMDAOs.Instance().newAVMStoreDAO.getStore(name);
        return convertStoreEntityToStore(storeEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#getByRoot(org.alfresco.repo.avm.AVMNode)
     */
    public AVMStore getByRoot(AVMNode root)
    {
        AVMStoreEntity storeEntity = AVMDAOs.Instance().newAVMStoreDAO.getStoreByRoot(root.getId());
        return convertStoreEntityToStore(storeEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#update(org.alfresco.repo.avm.AVMStore)
     */
    public void update(AVMStore store)
    {
        AVMStoreEntity storeEntity = convertStoreToStoreEntity(store);
        AVMDAOs.Instance().newAVMStoreDAO.updateStore(storeEntity);
        ((AVMStoreImpl)store).setVers(storeEntity.getVers());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#getByID(long)
     */
    public AVMStore getByID(long id)
    {
        AVMStoreEntity storeEntity = AVMDAOs.Instance().newAVMStoreDAO.getStore(id);
        return convertStoreEntityToStore(storeEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStoreDAO#invalidateCache()
     */
    public void invalidateCache() 
    {
        AVMDAOs.Instance().newAVMStoreDAO.clearStoreEntityCache();
    }
    
    private AVMStore convertStoreEntityToStore(AVMStoreEntity storeEntity)
    {
        if (storeEntity == null)
        {
            return null;
        }
        
        AVMStoreImpl store = new AVMStoreImpl();
        store.setId(storeEntity.getId());
        store.setName(storeEntity.getName());
        
        store.setNextVersionID(storeEntity.getVersion().intValue());
        store.setVers(storeEntity.getVers());
        
        Acl acl = null;
        if (storeEntity.getAclId() != null)
        {
            acl = AVMDAOs.Instance().fAclDAO.getAcl(storeEntity.getAclId());
        }
        store.setStoreAcl(acl);
        
        Long rootNodeId = storeEntity.getRootNodeId();
        if (rootNodeId == null)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Root node id is null for store: "+storeEntity);
            }
            return null;
        }
        
        DirectoryNode rootNode = (DirectoryNode) ((AVMNodeDAOIbatis)AVMDAOs.Instance().fAVMNodeDAO).getRootNodeByID(store, rootNodeId);
        if (rootNode == null)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Root node ("+rootNodeId+") not found for store: "+storeEntity);
            }
            return null;
        }
        
        store.setRoot(rootNode);
        
        return store;
    }
    
    private AVMStoreEntity convertStoreToStoreEntity(AVMStore store)
    {
        AVMStoreEntity storeEntity = new AVMStoreEntity();
        
        storeEntity.setId(store.getId());
        storeEntity.setName(store.getName());
        storeEntity.setRootNodeId(store.getRoot().getId());
        storeEntity.setVersion(new Long(store.getNextVersionID()));
        storeEntity.setVers(((AVMStoreImpl)store).getVers());
        storeEntity.setAclId((store.getStoreAcl() == null ? null : store.getStoreAcl().getId()));
        
        return storeEntity;
    }
}
