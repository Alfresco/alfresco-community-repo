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
