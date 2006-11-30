/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
}
