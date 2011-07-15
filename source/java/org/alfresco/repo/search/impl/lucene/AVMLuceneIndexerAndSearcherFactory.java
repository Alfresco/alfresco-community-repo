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
package org.alfresco.repo.search.impl.lucene;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Factory for AVM indexers and searchers
 * 
 * @author andyh
 *
 */
public class AVMLuceneIndexerAndSearcherFactory extends AbstractLuceneIndexerAndSearcherFactory implements SupportsBackgroundIndexing
{
    private DictionaryService dictionaryService;
    private NamespaceService nameSpaceService;
    private ContentService contentService;
    private AVMService avmService;
    private AVMSyncService avmSyncService;
    private NodeService nodeService;
    private ContentStore contentStore;
    private FullTextSearchIndexer fullTextSearchIndexer;
    private AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    public AVMLuceneIndexerAndSearcherFactory()
    {
        //s_logger.error("Creating AVMLuceneIndexerAndSearcherFactory");
    }
    
    /**
     * Set the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name space service
     */
    public void setNameSpaceService(NamespaceService nameSpaceService)
    {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Set the AVM service
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Set the AVM sync service
     */
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        this.avmSyncService = avmSyncService;
    }

    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     */
    public void setContentStore(ContentStore contentStore)
    {
        this.contentStore = contentStore;
    }
    
    /**
     * @param avmSnapShotTriggeredIndexingMethodInterceptor the avmSnapShotTriggeredIndexingMethodInterceptor to set
     */
    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    @Override
    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId)
    {
        AVMLuceneIndexerImpl indexer = AVMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, this);
        indexer.setDictionaryService(dictionaryService);
        indexer.setContentService(contentService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        indexer.setAvmService(avmService);
        indexer.setAvmSyncService(avmSyncService);
        indexer.setContentStore(contentStore);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        return indexer;
    }

    @Override
    protected List<StoreRef> getAllStores()
    {
        List<AVMStoreDescriptor> stores = avmService.getStores();
        List<StoreRef> storeRefs = new ArrayList<StoreRef>(stores.size());
        for(AVMStoreDescriptor storeDesc : stores)
        {
            StoreRef storeRef = AVMNodeConverter.ToStoreRef(storeDesc.getName());
            if (avmSnapShotTriggeredIndexingMethodInterceptor.getIndexMode(storeRef.getIdentifier()) == IndexMode.UNINDEXED)
            {
                // ALF-5722 fix
                continue;
            }
            storeRefs.add(storeRef);
        }
        return storeRefs;
    }

    @Override
    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException
    {
        //TODO: Store overlays
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getSearcher(storeRef, indexer, this);
        searcher.setNamespacePrefixResolver(nameSpaceService);
        // searcher.setLuceneIndexLock(luceneIndexLock);
        searcher.setNodeService(nodeService);
        searcher.setTenantService(tenantService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryRegister(getQueryRegister());
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryLanguages(getQueryLanguages());
        return searcher;
    }
    
    @Override
    protected SearchService getNodeSearcher() throws SearcherException
    {
        throw new UnsupportedOperationException();
    }

    /** 
     * Register the full text searcher (done by the seracher bean to break cyclic bean defs) 
     * 
     */
    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

}
