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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * DAO services for
 *     <b>avm_nodes</b>,
 *     <b>avm_aspects</b>,
 *     <b>avm_node_properties</b>
 * tables
 *
 * @author janv
 * @since 3.2
 */
public interface AVMNodeDAO
{
    //
    // AVM Nodes
    //
    
    public AVMNodeEntity createNode(AVMNodeEntity nodeEntity);
    
    public AVMNodeEntity getNode(long nodeId);
    
    public void updateNode(AVMNodeEntity nodeEntity);
    
    public void updateNodeModTimeAndGuid(AVMNodeEntity nodeEntity);
    
    public void updateNodeModTimeAndContentData(AVMNodeEntity nodeEntity);
    
    public List<AVMNodeEntity> getNodesNewInStore(long storeId);
    
    public List<AVMNodeEntity> getLayeredNodesNewInStore(long storeId);
    
    public List<Long> getLayeredNodesNewInStoreIDs(long storeId);
    
    public List<AVMNodeEntity> getNodeOrphans(int maxSize);
    
    public void updateNodesClearNewInStore(long storeId);
    
    public void deleteNode(long nodeId);
    
    public List<AVMNodeEntity> getAllLayeredDirectories();
    
    public List<AVMNodeEntity> getAllLayeredFiles();
    
    public void clearNodeEntityCache();
    
    /**
     * Get all content urls in the AVM Repository.
     * @param contentUrlHandler the handler that will be called with the URLs
     */
    public void getContentUrls(ContentUrlHandler handler);
    
    /**
     * A callback handler for iterating over the content URLs
     */
    public interface ContentUrlHandler
    {
        void handle(String contentUrl);
    }
    
    
    //
    // AVM Node Aspects
    //
    
    /**
     * Add aspect to given Node
     * 
     * @param nodeId        the unique ID of the node entity
     * @param qname         the qname
     * @throws              ConcurrencyFailureException if the aspect already exists
     */
    public void createAspect(long nodeId, QName qname);
    
    /**
     * Get set of aspects for given Node
     * 
     * @param nodeId        the unique ID of the node entity
     * @return              the set of qnames (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    public Set<QName> getAspects(long nodeId);
    
    /**
     * Remove aspect from given Node
     * 
     * @param nodeId        the unique ID of the node entity
     * @param qnameId       the qname
     * @throws              ConcurrencyFailureException if the aspect does not exist
     */
    public void deleteAspect(long nodeId, QName qname);
    
    public void deleteAspects(long nodeId);
    
    //
    // AVM Node Properties
    //
    
    public void createOrUpdateNodeProperty(long nodeId, QName qname, PropertyValue value);
    
    public Map<QName, PropertyValue> getNodeProperties(long nodeId);
    
    public void deleteNodeProperty(long nodeId, QName qname);
    
    public void deleteNodeProperties(long nodeId);
}
