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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.ChildEntry;
import org.alfresco.repo.avm.ChildEntryDAO;
import org.alfresco.repo.avm.ChildEntryImpl;
import org.alfresco.repo.avm.ChildKey;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.domain.avm.AVMChildEntryEntity;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * iBATIS DAO wrapper for ChildEntry
 * 
 * @author jan
 */
class ChildEntryDAOIbatis implements ChildEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#save(org.alfresco.repo.avm.ChildEntry)
     */
    public void save(ChildEntry entry)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.createChildEntry(entry.getKey().getParent().getId(), entry.getKey().getName(), entry.getChild().getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#get(org.alfresco.repo.avm.ChildKey)
     */
    public ChildEntry get(ChildKey key)
    {
        AVMChildEntryEntity childEntryEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getChildEntry(key.getParent().getId(), key.getName());
        return getChildEntryForParent(key.getParent(), childEntryEntity);
        
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#getByParent(org.alfresco.repo.avm.DirectoryNode, java.lang.String)
     */
    public List<ChildEntry> getByParent(DirectoryNode parent, String childNamePattern)
    {
        List<AVMChildEntryEntity> childEntryEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getChildEntriesByParent(parent.getId(), childNamePattern);
        
        List<ChildEntry> result = new ArrayList<ChildEntry>(childEntryEntities.size());
        for (AVMChildEntryEntity childEntryEntity : childEntryEntities)
        {
            result.add(getChildEntryForParent(parent, childEntryEntity));
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#getByParentChild(org.alfresco.repo.avm.DirectoryNode, org.alfresco.repo.avm.AVMNode)
     */
    public boolean existsParentChild(DirectoryNode parent, AVMNode child)
    {
        AVMChildEntryEntity childEntryEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getChildEntry(parent.getId(), child.getId());
        return (childEntryEntity != null);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#getByChild(org.alfresco.repo.avm.AVMNode)
     */
    public List<ChildEntry> getByChild(AVMNode child)
    {
        List<AVMChildEntryEntity> childEntryEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getChildEntriesByChild(child.getId());
        
        List<ChildEntry> result = new ArrayList<ChildEntry>(childEntryEntities.size());
        for (AVMChildEntryEntity childEntryEntity : childEntryEntities)
        {
            result.add(getChildEntryForChild(child, childEntryEntity));
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#rename(org.alfresco.repo.avm.ChildKey, String)
     */
    public void rename(ChildKey key, String newName)
    {
        // direct rename should only be used if changing case
        if (! key.getName().equalsIgnoreCase(newName))
        {
            throw new AlfrescoRuntimeException("Invalid rename (can only change case");
        }
        
        AVMChildEntryEntity childEntryEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getChildEntry(key.getParent().getId(), key.getName());
        
        childEntryEntity.setName(newName);
        
        AVMDAOs.Instance().newAVMNodeLinksDAO.updateChildEntry(childEntryEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#delete(org.alfresco.repo.avm.ChildEntry)
     */
    public void delete(ChildEntry child)
    {
        AVMChildEntryEntity childEntryEntity = new AVMChildEntryEntity();
        childEntryEntity.setParentNodeId(child.getKey().getParent().getId());
        childEntryEntity.setName(child.getKey().getName());
        childEntryEntity.setChildNodeId(child.getChild().getId());
        
        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteChildEntry(childEntryEntity);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#deleteByParent(org.alfresco.repo.avm.AVMNode)
     */
    public void deleteByParent(AVMNode parent)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteChildEntriesByParent(parent.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.ChildEntryDAO#evict(org.alfresco.repo.avm.ChildEntry)
     */
    public void evict(ChildEntry entry)
    {
        // NOOP
    }
    
    private ChildEntry getChildEntryForParent(DirectoryNode parentNode, AVMChildEntryEntity childEntryEntity)
    {
        if (childEntryEntity == null)
        {
            return null;
        }
        
        AVMNode childNode = AVMDAOs.Instance().fAVMNodeDAO.getByID(childEntryEntity.getChildId());
        
        if (childNode == null)
        {
            throw new ConcurrencyFailureException("Child node (" + childEntryEntity.getParentNodeId() + ", " + childEntryEntity.getChildId() + ") no longer exists");
        }
        
        ChildEntry ce = new ChildEntryImpl(new ChildKey(parentNode, childEntryEntity.getName()), childNode);
        return ce;
    }
    
    private ChildEntry getChildEntryForChild(AVMNode childNode, AVMChildEntryEntity childEntryEntity)
    {
        if (childEntryEntity == null)
        {
            return null;
        }
        
        DirectoryNode parentNode = (DirectoryNode)AVMDAOs.Instance().fAVMNodeDAO.getByID(childEntryEntity.getParentNodeId());
        
        if (parentNode == null)
        {
            throw new ConcurrencyFailureException("Parent node (" + childEntryEntity.getParentNodeId() + ", " + childEntryEntity.getChildId() + ") no longer exists");
        }
        
        ChildEntry ce = new ChildEntryImpl(new ChildKey(parentNode, childEntryEntity.getName()), childNode);
        return ce;
    }
}
