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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This interface abstracts how indexing is used from within the node service
 * implementation.
 * 
 * It has to optionally offer transactional integration For example, the lucene
 * indexer
 * 
 * @author andyh
 */

public interface Indexer
{
    /**
     * Create an index entry when a new node is created. A node is always
     * created with a name in a given parent and so a relationship ref is
     * required.
     * 
     * @param relationshipRef
     */
    public void createNode(ChildAssociationRef relationshipRef);

    /**
     * Update an index entry due to property changes on a node. There are no
     * strucural impications from such a change.
     * 
     * @param nodeRef
     */
    public void updateNode(NodeRef nodeRef);

    /**
     * Delete a node entry from an index. This implies structural change. The
     * node will be deleted from the index. This will also remove any remaining
     * refernces to the node from the index. The index has no idea of the
     * primary link.
     * 
     * @param relationshipRef
     */
    public void deleteNode(ChildAssociationRef relationshipRef);

    /**
     * Create a refernce link between a parent and child. Implies only
     * (potential) structural changes
     * 
     * @param relationshipRef
     */
    public void createChildRelationship(ChildAssociationRef relationshipRef);

    /**
     * Alter the relationship between parent and child nodes in the index.
     * 
     * This can be used for:
     * <OL>
     * <LI> rename,
     * <LI> move,
     * <LI> move and rename,
     * <LI> replace
     * </OL>
     * 
     * This could be implemented as a delete and add but some implementations
     * may be able to optimise this operation.
     * 
     * @param relationshipBeforeRef
     * @param relationshipAfterRef
     */
    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef);

    /**
     * Delete a relationship between a parent and child.
     * 
     * This will remove a structural route through the index. The index has no
     * idea of reference and primary relationships and will happily remove the
     * primary relationship before refernces which could remain.
     * 
     * Use delete to ensure all strctural references are removed or call this
     * sure you are doing an unlink (remove a hard link in the unix file system
     * world).
     * 
     * @param relationshipRef
     */
    public void deleteChildRelationship(ChildAssociationRef relationshipRef);

    /**
     * Delete the index for a store
     * @param storeRef
     */
    public void deleteIndex(StoreRef storeRef);

  
    public void flushPending();
    
    /**
     * Activates 'read through' behaviour for this indexer. Rather than accessing the database through the current
     * (potentially old) transaction, it will use a discrete read only transaction for each node it indexes. This avoids
     * 'stale' nodes building up in the caches during long reindex runs.
     * 
     * @param isReadThrough
     */
    public void setReadThrough(boolean isReadThrough);
}
