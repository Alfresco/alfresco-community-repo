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
package org.alfresco.repo.search.impl;

import java.util.Collection;

import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * A no action indexer - the indexing is done automatically along with persistence
 * 
 * TODO: Rename to Adaptor?
 * 
 * @author andyh
 * 
 */
public class NoActionIndexer implements Indexer
{
    public void setReadThrough(boolean isReadThrough)
    {
        return;
    }

    public void createNode(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void updateNode(NodeRef nodeRef)
    {
        return;
    }

    public void deleteNode(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef)
    {
        return;
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef)
    {
        return;
    }

    public void detectNodeChanges(NodeRef nodeRef, SearchService searcher,
            Collection<ChildAssociationRef> addedParents, Collection<ChildAssociationRef> deletedParents,
            Collection<ChildAssociationRef> createdNodes, Collection<NodeRef> updatedNodes)
    {
        return;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.Indexer#deleteIndex(org.alfresco.service.cmr.repository.StoreRef) */
    public void deleteIndex(StoreRef storeRef)
    {
        return;
    }

    public void flushPending()
    {
        return;
    }
}
