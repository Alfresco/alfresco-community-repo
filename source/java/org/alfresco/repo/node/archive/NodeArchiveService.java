/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.node.archive;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * A service interface providing methods that map onto the low-level
 * node restore functionality.
 * 
 * @author Derek Hulley
 */
public interface NodeArchiveService
{
    /**
     * Get the parent node that holds all nodes archived from the given store.
     * 
     * @param storeRef the original store of the archived nodes
     * @return Returns the parent of the archived nodes, or null if archiving
     *      is not configured for the store
     */
    public NodeRef getStoreArchiveNode(StoreRef storeRef);
    
    /**
     * Attempt to restore the given archived node into its original location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRef the node's reference in the archive
     * @return Returns the results of the restore operation
     */
    public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef);
    
    /**
     * Attempt to restore the given archived node into a new location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRef the node's reference in the archive.  This
     *      must be valid.
     * @param destinationNodeRef the parent of the restored node, or
     *      <tt>null</tt> to use the original parent node reference
     * @param assocTypeQName the type of the primary association to link the
     *      restored node to the destination parent, or <tt>null</tt> to use
     *      the orginal association type
     * @param assocQName the name of the primary association to be created,
     *      or <tt>null</tt> to use the original association name
     * @return Returns the results of the restore operation
     */
    public RestoreNodeReport restoreArchivedNode(
            NodeRef archivedNodeRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * Attempt to restore a list of archived nodes into their original locations,
     * using the original association types and names.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRefs the nodes' references in the archive.  These
     *      must be valid.
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs);
    
    /**
     * Attempt to restore a list of archived nodes into a new location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRefs the nodes' references in the archive.  These
     *      must be valid.
     * @param destinationNodeRef the parent of the restored nodes, or
     *      <tt>null</tt> to use the original parent node references
     * @param assocTypeQName the type of the primary associations to link the
     *      restored node to the destination parent, or <tt>null</tt> to use
     *      the orginal association types
     * @param assocQName the name of the primary associations to be created,
     *      or <tt>null</tt> to use the original association names
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreArchivedNodes(
            List<NodeRef> archivedNodeRefs,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * Attempt to restore all archived nodes into their original locations.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param originalStoreRef the store that the items originally came from
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(StoreRef originalStoreRef);
    
    /**
     * Attempt to restore all archived nodes into a new location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param originalStoreRef the store that the items originally came from
     * @param destinationNodeRef the parent of the restored nodes, or
     *      <tt>null</tt> to use the original parent node references
     * @param assocTypeQName the type of the primary associations to link the
     *      restored node to the destination parent, or <tt>null</tt> to use
     *      the orginal association types
     * @param assocQName the name of the primary associations to be created,
     *      or <tt>null</tt> to use the original association names
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreAllArchivedNodes(
            StoreRef originalStoreRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName);
    
    /**
     * Permanently delete the archived node.
     * 
     * @param archivedNodeRef the archived node to delete.
     */
    public void purgeArchivedNode(NodeRef archivedNodeRef);
    
    /**
     * Permanently delete the archived nodes.
     * 
     * @param archivedNodes the archived nodes to delete.
     */
    public void purgeArchivedNodes(List<NodeRef> archivedNodes);
    
    /**
     * Permanently delete all archived nodes.
     * 
     * @param originalStoreRef the store that the items originally came from
     */
    public void purgeAllArchivedNodes(StoreRef originalStoreRef);
}
