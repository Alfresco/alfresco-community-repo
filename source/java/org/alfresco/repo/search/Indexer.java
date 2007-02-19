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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search;

import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexerImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

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

  
    

}
