/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.archive;

import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A service interface providing methods that map onto the low-level node restore functionality.
 * <p>
 * Node stores <i>may</i> be mapped to an associated archive node store. For example, Alfresco ships with the mapping<br>
 * {@link StoreRef#STORE_REF_WORKSPACE_SPACESSTORE workspace://SpacesStore} .. maps to .. {@link StoreRef#STORE_REF_ARCHIVE_SPACESSTORE archive://SpacesStore}.<br>
 * When a node is {@link NodeService#deleteNode(NodeRef) deleted} from a regular workspace, it is moved to an archive store if there is a mapping.
 * <p>
 * This service operates <b>only on nodes that have been archived</b> by either
 * <ul>
 * <li>{@link #getArchivedNode(NodeRef) retrieving archived nodes},</li>
 * <li>{@link #purgeArchivedNode(NodeRef) permanently deleting archived nodes},</li>
 * <li>or {@link #restoreArchivedNode(NodeRef) restoring archived nodes back to their original location}.</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public interface NodeArchiveService
{
    /** Static 'sys:archivedItem' path for all archived nodes. */
    public static final QName QNAME_ARCHIVED_ITEM = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "archivedItem");

    /**
     * Get the parent node that holds all nodes archived from the given store.
     * 
     * @param originalStoreRef
     *            the original store of the archived nodes. This is the store where the currently archived nodes could originally be found.
     * @return Returns the parent of the archived nodes, or null if archiving is not configured for the store
     */
    public NodeRef getStoreArchiveNode(StoreRef originalStoreRef);

    /**
     * Get the likely node reference for the original node. There is no guarantee that the node exists in the archive store.
     * 
     * @param originalNodeRef
     *            the original node reference
     * @return Returns the node ref of the node if it was archived.
     */
    public NodeRef getArchivedNode(NodeRef originalNodeRef);

    /**
     * Attempt to restore the given archived node into its original location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRef
     *            the node's reference in the archive
     * @return Returns the results of the restore operation
     */
    public RestoreNodeReport restoreArchivedNode(NodeRef archivedNodeRef);

    /**
     * Attempt to restore the given archived node into a new location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRef
     *            the node's reference in the archive. This must be valid.
     * @param destinationNodeRef
     *            the parent of the restored node, or <tt>null</tt> to use the original parent node reference
     * @param assocTypeQName
     *            the type of the primary association to link the restored node to the destination parent, or <tt>null</tt> to use the orginal association type
     * @param assocQName
     *            the name of the primary association to be created, or <tt>null</tt> to use the original association name
     * @return Returns the results of the restore operation
     */
    public RestoreNodeReport restoreArchivedNode(
            NodeRef archivedNodeRef,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName);

    /**
     * Attempt to restore a list of archived nodes into their original locations, using the original association types and names.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRefs
     *            the nodes' references in the archive. These must be valid.
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreArchivedNodes(List<NodeRef> archivedNodeRefs);

    /**
     * Attempt to restore a list of archived nodes into a new location.
     * <p>
     * <b>TRANSACTIONS:</b> This method will execute in a new transaction.
     * 
     * @param archivedNodeRefs
     *            the nodes' references in the archive. These must be valid.
     * @param destinationNodeRef
     *            the parent of the restored nodes, or <tt>null</tt> to use the original parent node references
     * @param assocTypeQName
     *            the type of the primary associations to link the restored node to the destination parent, or <tt>null</tt> to use the orginal association types
     * @param assocQName
     *            the name of the primary associations to be created, or <tt>null</tt> to use the original association names
     * @return Returns the results of the each attempted restore operation
     */
    public List<RestoreNodeReport> restoreArchivedNodes(
            List<NodeRef> archivedNodeRefs,
            NodeRef destinationNodeRef,
            QName assocTypeQName,
            QName assocQName);

    /**
     * Permanently delete the archived node.
     * 
     * @param archivedNodeRef
     *            the archived node to delete.
     * 
     * @see NodeService#deleteNode(NodeRef)
     */
    public void purgeArchivedNode(NodeRef archivedNodeRef);

    /**
     * Permanently delete the archived nodes.
     * 
     * @param archivedNodes
     *            the archived nodes to delete.
     */
    public void purgeArchivedNodes(List<NodeRef> archivedNodes);

    /**
     * Permanently delete all archived nodes.
     * 
     * @param originalStoreRef
     *            the store that the items originally came from
     */
    public void purgeAllArchivedNodes(StoreRef originalStoreRef);

    /**
     * Get the archived nodes deleted by the current user. If the current user is an Administrator, then all the deleted nodes are fetched.
     * 
     * @param cannedQueryBuilder
     *            the object that holds the required and optional parameters to perform the canned query
     * @return the results of the attempted search
     * @since 4.2
     */
    public PagingResults<NodeRef> listArchivedNodes(ArchivedNodesCannedQueryBuilder cannedQueryBuilder);

    /**
     * Check if the current user has authority to access the archived node.
     * 
     * @param nodeRef
     *            NodeRef
     * @return true if the currently authenticated user has authority to access the archived node, otherwise false.
     */
    public boolean hasFullAccess(NodeRef nodeRef);
}
