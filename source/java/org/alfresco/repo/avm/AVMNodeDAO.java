/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.avm;

import java.util.List;

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
     * Get all content urls in he AVM Repository.
     * @return A List of URL Strings.
     */
    public List<String> getContentUrls();
    
    /**
     * Get all the nodes that are new in the given store.
     * @param store The store to query.
     * @return A List of AVMNodes.
     */
    public List<AVMNode> getNewInStore(AVMStore store);
    
    /**
     * Inappropriate hack to get Hibernate to play nice.
     */
    public void flush();
    
    /**
     * Get a batch 
     * @return An iterator over all nodes.
     */
    List<AVMNode> getEmptyGUIDS(int count);
    
    /**
     * Get a batch of LayeredDirectories which have null indirectionVersions.
     * @param count
     * @return
     */
    List<LayeredDirectoryNode> getNullVersionLayeredDirectories(int count);
    
    /**
     * Get a batch of LayeredFiles which have null indirectionVersions.
     * @param count
     * @return
     */
    List<LayeredFileNode> getNullVersionLayeredFiles(int count);
}
