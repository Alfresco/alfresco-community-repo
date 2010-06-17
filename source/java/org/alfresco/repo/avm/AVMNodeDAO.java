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
package org.alfresco.repo.avm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;

/**
 * DAO for AVMNodes interface.
 * @author britt
 */
public interface AVMNodeDAO
{
    /**
     * Save the given node, having never been saved before.
     */
    public void save(AVMNode node);

    /**
     * Delete a single node.
     * @param node The node to delete.
     */
    public void delete(AVMNode node);
    
    public void createAspect(long nodeId, QName aspectQName);
    
    public void deleteAspect(long nodeId, QName aspectQName);
    
    public void deleteAspects(long nodeId);
    
    public Set<QName> getAspects(long nodeId);
    
    public void createOrUpdateProperty(long nodeId, QName propQName, PropertyValue value);
    
    public void deleteProperty(long nodeId, QName propQName);
    
    public void deleteProperties(long nodeId);
    
    public Map<QName, PropertyValue> getProperties(long nodeId);
    
    /**
     * Get by ID.
     * @param id The id to get.
     */
    public AVMNode getByID(long id);

    /**
     * Get the root of a particular version.
     * @param store The store we're querying.
     * @param version The version.
     * @return The VersionRoot or null.
     */
    public DirectoryNode getAVMStoreRoot(AVMStore store, int version);

    /**
     * Update a node that has been dirtied.
     * @param node The node.
     */
    public void update(AVMNode node);
    
    // update optimisation, eg. when creating files
    public void updateModTimeAndGuid(AVMNode node);
    
    /**
     * Get the ancestor of a node.
     * @param node The node whose ancestor is desired.
     * @return The ancestor or null.
     */
    public AVMNode getAncestor(AVMNode node);

    /**
     * Get the node the given node was merged from.
     * @param node The node whose merged from is desired.
     * @return The merged from node or null.
     */
    public AVMNode getMergedFrom(AVMNode node);

    /**
     * Get up to batchSize orphans.
     * @param batchSize Get no more than this number.
     * @return A List of orphaned AVMNodes.
     */
    public List<AVMNode> getOrphans(int batchSize);

    
    /**
     * Get all the nodes that are new in the given store.
     * @param store The store to query.
     * @return A List of AVMNodes.
     */
    public List<AVMNode> getNewInStore(AVMStore store);
    
    /**
     * Clear newInStore field for a store. (Snapshot)
     * @param store
     */
    public void clearNewInStore(AVMStore store);

    /**
     * Get any new layered entries in a store.
     * @param store
     * @return
     */
    public List<Long> getNewLayeredInStoreIDs(AVMStore store);
    
    public List<Layered> getNewLayeredInStore(AVMStore store);

    /**
     * Clear the cache.
     */
    public void clear();
}
