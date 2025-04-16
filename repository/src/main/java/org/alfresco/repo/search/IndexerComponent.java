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
package org.alfresco.repo.search;

import java.util.Collection;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.repo.service.StoreRedirectorProxyFactory;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Component API for indexing. Delegates to the real index retrieved from the {@link #indexerAndSearcherFactory}
 * 
 * Transactional support is free.
 * 
 * @see Indexer
 * 
 * @author andyh
 * 
 */
public class IndexerComponent extends AbstractLifecycleBean implements Indexer
{
    private StoreRedirectorProxyFactory<IndexerAndSearcher> storeRedirectorProxyFactory;
    private IndexerAndSearcher indexerAndSearcherFactory;
    private static final String KEY_READ_THROUGH = IndexerComponent.class.getName() + "READ_THROUGH";

    public void setStoreRedirectorProxyFactory(StoreRedirectorProxyFactory<IndexerAndSearcher> storeRedirectorProxyFactory)
    {
        this.storeRedirectorProxyFactory = storeRedirectorProxyFactory;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        this.indexerAndSearcherFactory = storeRedirectorProxyFactory.getObject();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {}

    public void setIndexerAndSearcherFactory(IndexerAndSearcher indexerAndSearcherFactory)
    {
        this.indexerAndSearcherFactory = indexerAndSearcherFactory;
    }

    public void setReadThrough(boolean isReadThrough)
    {
        if (isReadThrough)
        {
            AlfrescoTransactionSupport.bindResource(KEY_READ_THROUGH, Boolean.TRUE);
        }
        else
        {
            AlfrescoTransactionSupport.unbindResource(KEY_READ_THROUGH);
        }
    }

    private Indexer getIndexer(StoreRef storeRef)
    {
        Indexer indexer = indexerAndSearcherFactory.getIndexer(storeRef);
        indexer.setReadThrough(AlfrescoTransactionSupport.getResource(KEY_READ_THROUGH) == Boolean.TRUE);
        return indexer;
    }

    public void createNode(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = getIndexer(relationshipRef.getChildRef().getStoreRef());
        indexer.createNode(relationshipRef);
    }

    public void updateNode(NodeRef nodeRef)
    {
        Indexer indexer = getIndexer(nodeRef.getStoreRef());
        indexer.updateNode(nodeRef);
    }

    public void deleteNode(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = getIndexer(relationshipRef.getChildRef().getStoreRef());
        indexer.deleteNode(relationshipRef);
    }

    public void createChildRelationship(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = getIndexer(relationshipRef.getChildRef().getStoreRef());
        indexer.createChildRelationship(relationshipRef);
    }

    public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef, ChildAssociationRef relationshipAfterRef)
    {
        Indexer indexer = getIndexer(relationshipBeforeRef.getChildRef().getStoreRef());
        indexer.updateChildRelationship(relationshipBeforeRef, relationshipAfterRef);
    }

    public void deleteChildRelationship(ChildAssociationRef relationshipRef)
    {
        Indexer indexer = getIndexer(relationshipRef.getChildRef().getStoreRef());
        indexer.deleteChildRelationship(relationshipRef);
    }

    public void detectNodeChanges(NodeRef nodeRef, SearchService searcher,
            Collection<ChildAssociationRef> addedParents, Collection<ChildAssociationRef> deletedParents,
            Collection<ChildAssociationRef> createdNodes, Collection<NodeRef> updatedNodes)
    {
        Indexer indexer = getIndexer(nodeRef.getStoreRef());
        indexer.detectNodeChanges(nodeRef, searcher, addedParents, deletedParents, createdNodes, updatedNodes);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.Indexer#deleteIndex(org.alfresco.service.cmr.repository.StoreRef) */
    public void deleteIndex(StoreRef storeRef)
    {
        Indexer indexer = getIndexer(storeRef);
        indexer.deleteIndex(storeRef);
    }

    public void flushPending()
    {
        indexerAndSearcherFactory.flush();
    }
}
