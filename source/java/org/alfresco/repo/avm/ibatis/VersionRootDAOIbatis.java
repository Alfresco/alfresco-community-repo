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
import java.util.Date;
import java.util.List;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.avm.VersionRoot;
import org.alfresco.repo.avm.VersionRootDAO;
import org.alfresco.repo.avm.VersionRootImpl;
import org.alfresco.repo.domain.avm.AVMVersionRootEntity;

/**
 * iBATIS DAO wrapper for VersionRoot
 * 
 * @author janv
 */
class VersionRootDAOIbatis implements VersionRootDAO
{
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#save(org.alfresco.repo.avm.VersionRoot)
     */
    public void save(VersionRoot vr)
    {
        AVMVersionRootEntity vrEntity = AVMDAOs.Instance().newAVMVersionRootDAO.createVersionRoot(
                vr.getAvmStore().getId(),
                vr.getRoot().getId(),
                vr.getVersionID(),
                vr.getCreator(),
                vr.getTag(),
                vr.getDescription());
        
        ((VersionRootImpl)vr).setId(vrEntity.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#update(org.alfresco.repo.avm.VersionRoot)
     */
    public void update(VersionRoot vr)
    {
        // note: tag and description only
        AVMDAOs.Instance().newAVMVersionRootDAO.updateVersionRoot(convertVersionRootToVersionRootEntity(vr));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#delete(org.alfresco.repo.avm.VersionRoot)
     */
    public void delete(VersionRoot vr)
    {
        AVMDAOs.Instance().newAVMVersionRootDAO.deleteVersionRoot(vr.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getAllInAVMStore(org.alfresco.repo.avm.AVMStore)
     */
    public List<VersionRoot> getAllInAVMStore(AVMStore store)
    {
        List<AVMVersionRootEntity> vrEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getAllInStore(store.getId());
        List<VersionRoot> vrs = new ArrayList<VersionRoot>(vrEntities.size());
        for (AVMVersionRootEntity vrEntity : vrEntities)
        {
            vrs.add(convertVersionRootEntityToVersionRoot(vrEntity));
        }
        return vrs;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getByDates(org.alfresco.repo.avm.AVMStore, java.util.Date, java.util.Date)
     */
    public List<VersionRoot> getByDates(AVMStore store, Date from, Date to)
    {
        List<AVMVersionRootEntity> vrEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getByDates(store.getId(), from, to);
        List<VersionRoot> vrs = new ArrayList<VersionRoot>(vrEntities.size());
        for (AVMVersionRootEntity vrEntity : vrEntities)
        {
            vrs.add(convertVersionRootEntityToVersionRoot(vrEntity));
        }
        return vrs;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getByVersionID(org.alfresco.repo.avm.AVMStore, int)
     */
    public synchronized VersionRoot getByVersionID(AVMStore store, int id)
    {
        AVMVersionRootEntity vrEntity = AVMDAOs.Instance().newAVMVersionRootDAO.getByVersionID(store.getId(), id);
        return convertVersionRootEntityToVersionRoot(vrEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getByRoot(org.alfresco.repo.avm.AVMNode)
     */
    public VersionRoot getByRoot(AVMNode root)
    {
        AVMVersionRootEntity vrEntity = AVMDAOs.Instance().newAVMVersionRootDAO.getByRoot(root.getId());
        return convertVersionRootEntityToVersionRoot(vrEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getMaxVersion(org.alfresco.repo.avm.AVMStore)
     */
    public VersionRoot getMaxVersion(AVMStore rep)
    {
        AVMVersionRootEntity vrEntity = AVMDAOs.Instance().newAVMVersionRootDAO.getMaxVersion(rep.getId());
        return convertVersionRootEntityToVersionRoot(vrEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionRootDAO#getMaxVersionID(org.alfresco.repo.avm.AVMStore)
     */
    public Integer getMaxVersionID(AVMStore store)
    {
        Long maxVersionId = AVMDAOs.Instance().newAVMVersionRootDAO.getMaxVersionID(store.getId());
        if (maxVersionId == null)
        {
            return null;
        }
        return new Integer(maxVersionId.intValue());
    }
    
    
    
    public List<VersionRoot> getByVersionsTo(AVMStore store, int version)
    {
        List<AVMVersionRootEntity> vrEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getByVersionsTo(store.getId(), version);
        List<VersionRoot> vrs = new ArrayList<VersionRoot>(vrEntities.size());
        for (AVMVersionRootEntity vrEntity : vrEntities)
        {
            vrs.add(convertVersionRootEntityToVersionRoot(vrEntity));
        }
        return vrs;
    }

    public List<VersionRoot> getByVersionsFrom(AVMStore store, int version)
    {
        List<AVMVersionRootEntity> vrEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getByVersionsFrom(store.getId(), version);
        List<VersionRoot> vrs = new ArrayList<VersionRoot>(vrEntities.size());
        for (AVMVersionRootEntity vrEntity : vrEntities)
        {
            vrs.add(convertVersionRootEntityToVersionRoot(vrEntity));
        }
        return vrs;
    }

    
    
    public List<VersionRoot> getByVersionsBetween(AVMStore store, int startVersion, int endVersion)
    {
        List<AVMVersionRootEntity> vrEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getByVersionsBetween(store.getId(), startVersion, endVersion);
        List<VersionRoot> vrs = new ArrayList<VersionRoot>(vrEntities.size());
        for (AVMVersionRootEntity vrEntity : vrEntities)
        {
            vrs.add(convertVersionRootEntityToVersionRoot(vrEntity));
        }
        return vrs;
    }

    private AVMVersionRootEntity convertVersionRootToVersionRootEntity(VersionRoot vr)
    {
        if (vr == null)
        {
            return null;
        }
        
        AVMVersionRootEntity vrEntity = new AVMVersionRootEntity();
        vrEntity.setCreatedDate(vr.getCreateDate());
        vrEntity.setCreator(vr.getCreator());
        vrEntity.setDescription(vr.getDescription());
        vrEntity.setId(vr.getId());
        vrEntity.setRootNodeId(vr.getRoot().getId());
        vrEntity.setStoreId(vr.getAvmStore().getId());
        vrEntity.setTag(vr.getTag());
        vrEntity.setVersion(vr.getVersionID());
        
        return vrEntity;
    }
    
    private VersionRoot convertVersionRootEntityToVersionRoot(AVMVersionRootEntity vrEntity)
    {
        if (vrEntity == null)
        {
            return null;
        }
        
        AVMStore store = AVMDAOs.Instance().fAVMStoreDAO.getByID(vrEntity.getStoreId());
        AVMNode rootNode = AVMDAOs.Instance().fAVMNodeDAO.getByID(vrEntity.getRootNodeId());
        
        VersionRootImpl vr = new VersionRootImpl(
                store,
                (DirectoryNode)rootNode,
                vrEntity.getVersion().intValue(),
                vrEntity.getCreatedDate(),
                vrEntity.getCreator(),
                vrEntity.getTag(),
                vrEntity.getDescription());
        
        vr.setId(vrEntity.getId());
        
        return vr;
    }
}
