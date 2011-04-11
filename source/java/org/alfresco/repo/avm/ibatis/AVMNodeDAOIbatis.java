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
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMNodeDAO;
import org.alfresco.repo.avm.AVMNodeImpl;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.BasicAttributes;
import org.alfresco.repo.avm.BasicAttributesImpl;
import org.alfresco.repo.avm.DeletedNode;
import org.alfresco.repo.avm.DeletedNodeImpl;
import org.alfresco.repo.avm.DirectoryNode;
import org.alfresco.repo.avm.Layered;
import org.alfresco.repo.avm.LayeredDirectoryNode;
import org.alfresco.repo.avm.LayeredDirectoryNodeImpl;
import org.alfresco.repo.avm.LayeredFileNode;
import org.alfresco.repo.avm.LayeredFileNodeImpl;
import org.alfresco.repo.avm.PlainDirectoryNode;
import org.alfresco.repo.avm.PlainDirectoryNodeImpl;
import org.alfresco.repo.avm.PlainFileNode;
import org.alfresco.repo.avm.PlainFileNodeImpl;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;
import org.alfresco.repo.domain.avm.AVMMergeLinkEntity;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.avm.AVMVersionRootEntity;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.service.namespace.QName;

/**
 * iBATIS DAO wrapper for AVMNode
 * 
 * @author janv
 *
 */
class AVMNodeDAOIbatis implements AVMNodeDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#save(org.alfresco.repo.avm.AVMNode)
     */
    public void save(AVMNode node)
    {
        AVMNodeEntity nodeEntity = AVMDAOs.Instance().newAVMNodeDAO.createNode(convertNodeToNodeEntity(node));
        ((AVMNodeImpl)node).setId(nodeEntity.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#delete(org.alfresco.repo.avm.AVMNode)
     */
    public void delete(AVMNode node)
    {
        AVMDAOs.Instance().newAVMNodeDAO.deleteNode(node.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#createAspect(long, long)
     */
    public void createAspect(long nodeId, QName aspectQName)
    {
        AVMDAOs.Instance().newAVMNodeDAO.createAspect(nodeId, aspectQName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#deleteAspect(long, long)
     */
    public void deleteAspect(long nodeId, QName aspectQName)
    {
        AVMDAOs.Instance().newAVMNodeDAO.deleteAspect(nodeId, aspectQName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#deleteAspects(long)
     */
    public void deleteAspects(long nodeId)
    {
        AVMDAOs.Instance().newAVMNodeDAO.deleteAspects(nodeId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getAspects(long)
     */
    public Set<QName> getAspects(long nodeId)
    {
        return AVMDAOs.Instance().newAVMNodeDAO.getAspects(nodeId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#createOrUpdateProperty(long, QName, org.alfresco.repo.domain.PropertyValue)
     */
    public void createOrUpdateProperty(long nodeId, QName qname, PropertyValue value)
    {
        AVMDAOs.Instance().newAVMNodeDAO.createOrUpdateNodeProperty(nodeId, qname, value);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#deleteProperty(long, QName)
     */
    public void deleteProperty(long nodeId, QName propQName)
    {
        AVMDAOs.Instance().newAVMNodeDAO.deleteNodeProperty(nodeId, propQName);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#deleteProperties(long)
     */
    public void deleteProperties(long nodeId)
    {
        AVMDAOs.Instance().newAVMNodeDAO.deleteNodeProperties(nodeId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getProperties(long)
     */
    public Map<QName, PropertyValue> getProperties(long nodeId)
    {
        return AVMDAOs.Instance().newAVMNodeDAO.getNodeProperties(nodeId);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getByID(long)
     */
    public AVMNode getByID(long id)
    {
        return convertNodeEntityToNode(AVMDAOs.Instance().newAVMNodeDAO.getNode(id));
    }
    
    /* package */ AVMNode getRootNodeByID(AVMStore store, long rootNodeId)
    {
        AVMNodeEntity rootNodeEntity = AVMDAOs.Instance().newAVMNodeDAO.getNode(rootNodeId);
        
        if (rootNodeEntity == null)
        {
            return null;
        }
        
        AVMNode rootNode = null;
        if (rootNodeEntity.getStoreNewId() != null)
        {
            rootNode = convertNodeEntityToNode(rootNodeEntity, false);
            rootNode.setStoreNew(store);
        }
        else
        {
            rootNode = convertNodeEntityToNode(rootNodeEntity);
        }
        
        return rootNode;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#update(org.alfresco.repo.avm.AVMNode)
     */
    public void update(AVMNode node)
    {
        AVMNodeEntity nodeEntity = convertNodeToNodeEntity(node);
        AVMDAOs.Instance().newAVMNodeDAO.updateNode(nodeEntity);
        ((AVMNodeImpl)node).setVers(nodeEntity.getVers());
    }
    
    /**
     * TODO review
     * 
     * @deprecated
     */
    public void updateModTimeAndGuid(AVMNode node)
    {
        AVMNodeEntity nodeEntity = convertNodeToNodeEntity(node);
        AVMDAOs.Instance().newAVMNodeDAO.updateNodeModTimeAndGuid(nodeEntity);
        ((AVMNodeImpl)node).setVers(nodeEntity.getVers());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getAVMStoreRoot(org.alfresco.repo.avm.AVMStore, int)
     */
    public DirectoryNode getAVMStoreRoot(AVMStore store, int version)
    {
        AVMVersionRootEntity vrEntity = AVMDAOs.Instance().newAVMVersionRootDAO.getByVersionID(store.getId(), version);
        
        if (vrEntity == null)
        {
            return null;
        }
        
        return (DirectoryNode) getByID(vrEntity.getRootNodeId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getAncestor(org.alfresco.repo.avm.AVMNode)
     */
    public AVMNode getAncestor(AVMNode descendent)
    {
        AVMHistoryLinkEntity hlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinkByDescendent(descendent.getId());
        if (hlEntity == null)
        {
            return null;
        }
        return AVMDAOs.Instance().fAVMNodeDAO.getByID(hlEntity.getAncestorNodeId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getMergedFrom(org.alfresco.repo.avm.AVMNode)
     */
    public AVMNode getMergedFrom(AVMNode mTo)
    {
        AVMMergeLinkEntity mlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getMergeLinkByTo(mTo.getId());
        if (mlEntity == null)
        {
            return null;
        }
        return AVMDAOs.Instance().fAVMNodeDAO.getByID(mlEntity.getMergeFromNodeId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getOrphans(int)
     */
    public List<AVMNode> getOrphans(int batchSize)
    {
        List<AVMNodeEntity> nodeEntities = AVMDAOs.Instance().newAVMNodeDAO.getNodeOrphans(batchSize);
        
        if (nodeEntities == null)
        {
           return new ArrayList<AVMNode>(0);
        }
        
        List<AVMNode> nodes = new ArrayList<AVMNode>(nodeEntities.size());
        for (AVMNodeEntity nodeEntity : nodeEntities)
        {
            nodes.add(convertNodeEntityToNode(nodeEntity));
        }
        return nodes;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getNewInStore(org.alfresco.repo.avm.AVMStore)
     */
    public List<AVMNode> getNewInStore(AVMStore store)
    {
        List<AVMNodeEntity> nodeEntities = AVMDAOs.Instance().newAVMNodeDAO.getNodesNewInStore(store.getId());
        
        if (nodeEntities == null)
        {
           return new ArrayList<AVMNode>(0);
        }
        
        List<AVMNode> nodes = new ArrayList<AVMNode>(nodeEntities.size());
        for (AVMNodeEntity nodeEntity : nodeEntities)
        {
            nodes.add(convertNodeEntityToNode(nodeEntity));
        }
        return nodes;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#clear()
     */
    public void clear()
    {
        AVMDAOs.Instance().newAVMNodeDAO.clearNodeEntityCache();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#clearNewInStore(org.alfresco.repo.avm.AVMStore)
     */
    public void clearNewInStore(AVMStore store)
    {
        AVMDAOs.Instance().newAVMNodeDAO.updateNodesClearNewInStore(store.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getNewLayeredInStoreIDs(org.alfresco.repo.avm.AVMStore)
     */
    public List<Long> getNewLayeredInStoreIDs(AVMStore store)
    {
        return AVMDAOs.Instance().newAVMNodeDAO.getLayeredNodesNewInStoreIDs(store.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodeDAO#getNewLayeredInStore(org.alfresco.repo.avm.AVMStore)
     */
    public List<Layered> getNewLayeredInStore(AVMStore store)
    {
        List<AVMNodeEntity> nodeEntities = AVMDAOs.Instance().newAVMNodeDAO.getLayeredNodesNewInStore(store.getId());
        
        if (nodeEntities == null)
        {
           return new ArrayList<Layered>(0);
        }
        
        List<Layered> nodes = new ArrayList<Layered>(nodeEntities.size());
        for (AVMNodeEntity nodeEntity : nodeEntities)
        {
            nodes.add((Layered)convertNodeEntityToNode(nodeEntity));
        }
        return nodes;
    }
    
    private AVMNodeEntity convertNodeToNodeEntity(AVMNode node)
    {
        AVMNodeEntity nodeEntity = new AVMNodeEntity();
        
        nodeEntity.setId(node.getId());
        
        nodeEntity.setAccessDate(node.getBasicAttributes().getAccessDate());
        nodeEntity.setOwner(node.getBasicAttributes().getOwner());
        nodeEntity.setCreator(node.getBasicAttributes().getCreator());
        nodeEntity.setCreatedDate(node.getBasicAttributes().getCreateDate());
        nodeEntity.setModifier(node.getBasicAttributes().getLastModifier());
        nodeEntity.setModifiedDate(node.getBasicAttributes().getModDate());
        
        nodeEntity.setType(node.getType());
        nodeEntity.setVersion(new Long(node.getVersionID()));
        nodeEntity.setVers(((AVMNodeImpl)node).getVers());
        nodeEntity.setAclId((node.getAcl() == null ? null : node.getAcl().getId()));
        
        nodeEntity.setGuid(node.getGuid());
        nodeEntity.setStoreNewId(node.getStoreNew() == null ? null : node.getStoreNew().getId());
        nodeEntity.setRoot(node.getIsRoot());
        
        if (node instanceof PlainFileNode)
        {
            PlainFileNode pfNode = (PlainFileNode)node;
            nodeEntity.setEncoding(pfNode.getEncoding());
            nodeEntity.setLength(pfNode.getLength());
            nodeEntity.setMimetype(pfNode.getMimeType());
            nodeEntity.setContentUrl(pfNode.getContentURL());
        }
        else if (node instanceof LayeredFileNode)
        {
            LayeredFileNode lfNode = (LayeredFileNode)node;
            nodeEntity.setIndirection(lfNode.getIndirection());
            nodeEntity.setIndirectionVersion(lfNode.getIndirectionVersion());
        } 
        else if (node instanceof PlainDirectoryNode)
        {
            // no additional
        }
        else if (node instanceof LayeredDirectoryNode)
        {
            LayeredDirectoryNode ldNode = (LayeredDirectoryNode)node;
            nodeEntity.setIndirection(ldNode.getIndirection());
            nodeEntity.setIndirectionVersion(ldNode.getIndirectionVersion());
            nodeEntity.setLayerId(ldNode.getLayerID());
            nodeEntity.setPrimaryIndirection(ldNode.getPrimaryIndirection());
            nodeEntity.setOpacity(ldNode.getOpacity());
        }
        else if (node instanceof DeletedNode)
        {
            DeletedNode dNode = (DeletedNode)node;
            nodeEntity.setDeletedType(dNode.getDeletedType());
        }
        
        return nodeEntity;
    }
    
    private AVMNode convertNodeEntityToNode(AVMNodeEntity nodeEntity)
    {
        return convertNodeEntityToNode(nodeEntity, true);
    }
    
    private AVMNode convertNodeEntityToNode(AVMNodeEntity nodeEntity, boolean withStore)
    {
        if (nodeEntity == null)
        {
            return null;
        }
        
        AVMNodeImpl node = null;
        if (nodeEntity.getType() == AVMNodeType.PLAIN_FILE)
        {
            node = new PlainFileNodeImpl();
            PlainFileNodeImpl pfNode = (PlainFileNodeImpl) node;
            pfNode.setMimeType(nodeEntity.getMimetype());
            pfNode.setEncoding(nodeEntity.getEncoding());
            pfNode.setLength(nodeEntity.getLength());
            pfNode.setContentURL(nodeEntity.getContentUrl());
        }
        else if (nodeEntity.getType() == AVMNodeType.PLAIN_DIRECTORY)
        {
            node = new PlainDirectoryNodeImpl();
            
            // no additional
        }
        else if (nodeEntity.getType() == AVMNodeType.LAYERED_FILE)
        {
            node = new LayeredFileNodeImpl();
            
            ((LayeredFileNodeImpl)node).setIndirection(nodeEntity.getIndirection());
            ((LayeredFileNodeImpl)node).setIndirectionVersion(nodeEntity.getIndirectionVersion());
        }
        else if (nodeEntity.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            node = new LayeredDirectoryNodeImpl();
            
            ((LayeredDirectoryNodeImpl)node).setIndirection(nodeEntity.getIndirection());
            ((LayeredDirectoryNodeImpl)node).setIndirectionVersion(nodeEntity.getIndirectionVersion());
            ((LayeredDirectoryNodeImpl)node).setPrimaryIndirection(nodeEntity.isPrimaryIndirection());
            ((LayeredDirectoryNodeImpl)node).setLayerID(nodeEntity.getLayerId());
            ((LayeredDirectoryNodeImpl)node).setOpacity(nodeEntity.getOpacity());
        }
        else if (nodeEntity.getType() == AVMNodeType.DELETED_NODE)
        {
            node = new DeletedNodeImpl();
            ((DeletedNodeImpl)node).setDeletedType(nodeEntity.getDeletedType());
        }
        else
        {
            // belts-and-braces
            throw new AlfrescoRuntimeException("Unexpected node type: "+nodeEntity.getType());
        }
        
        node.setId(nodeEntity.getId());
        node.setIsRoot(nodeEntity.isRoot());
        node.setGuid(nodeEntity.getGuid());
        node.setVersionID(nodeEntity.getVersion().intValue());
        node.setVers(nodeEntity.getVers());
        
        BasicAttributes ba = new BasicAttributesImpl();
        ba.setAccessDate(nodeEntity.getAccessDate());
        ba.setModDate(nodeEntity.getModifiedDate());
        ba.setCreateDate(nodeEntity.getCreatedDate());
        ba.setLastModifier(nodeEntity.getModifier());
        ba.setCreator(nodeEntity.getCreator());
        ba.setOwner(nodeEntity.getOwner());
        
        node.setBasicAttributes(ba);
        
        AVMStore store = null;
        if (withStore)
        {
            if (nodeEntity.getStoreNewId() != null)
            {
                store = AVMDAOs.Instance().fAVMStoreDAO.getByID(nodeEntity.getStoreNewId());
            }
        }
        
        node.setStoreNew(store);
        
        Acl acl = null;
        if (nodeEntity.getAclId() != null)
        {
            acl = AVMDAOs.Instance().fAclDAO.getAcl(nodeEntity.getAclId());
        }
        node.setAcl(acl);
        
        return node;
    }
}
