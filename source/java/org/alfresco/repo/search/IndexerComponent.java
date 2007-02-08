/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Component API for indexing. Delegates to the real index retrieved from the
 * {@link #indexerAndSearcherFactory}
 * 
 * Transactional support is free.
 * 
 * @see Indexer
 * 
 * @author andyh
 * 
 */
public class IndexerComponent implements Indexer
{
    private IndexerAndSearcher indexerAndSearcherFactory;

    public void setIndexerAndSearcherFactory(IndexerAndSearcher indexerAndSearcherFactory)
    {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    public void createNode(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef());
        indexer.createNode(relationshipRef);
    }

    public void updateNode(NodeRef nodeRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(nodeRef.getStoreRef());
        indexer.updateNode(nodeRef);
    }

    public void deleteNode(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef());
        indexer.deleteNode(relationshipRef);
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef());
        indexer.createChildRelationship(relationshipRef);
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipBeforeRef.getChildRef().getStoreRef());
        indexer.updateChildRelationship(relationshipBeforeRef, relationshipAfterRef);
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(
                relationshipRef.getChildRef().getStoreRef());
        indexer.deleteChildRelationship(relationshipRef);
    }

}
