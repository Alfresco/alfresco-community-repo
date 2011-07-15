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

import java.util.List;

import org.alfresco.cmis.CMISQueryService;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.SupportsBackgroundIndexing;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Factory for ADM indxers and searchers
 * @author andyh
 *
 */
public class ADMLuceneIndexerAndSearcherFactory extends AbstractLuceneIndexerAndSearcherFactory implements SupportsBackgroundIndexing
{
    protected DictionaryService dictionaryService;

    private NamespaceService nameSpaceService;

    protected NodeService nodeService;

    protected FullTextSearchIndexer fullTextSearchIndexer;

    protected ContentService contentService;

    /**
     * Set the dictinary service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name space service
     * @param nameSpaceService
     */
    public void setNameSpaceService(NamespaceService nameSpaceService)
    {
        this.nameSpaceService = nameSpaceService;
    }

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFullTextSearchIndexer(FullTextSearchIndexer fullTextSearchIndexer)
    {
        this.fullTextSearchIndexer = fullTextSearchIndexer;
    }

    /**
     * Set the content service
     * @param contentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    protected LuceneIndexer createIndexer(StoreRef storeRef, String deltaId)
    {
        storeRef = tenantService.getName(storeRef);
        
        ADMLuceneIndexerImpl indexer = ADMLuceneIndexerImpl.getUpdateIndexer(storeRef, deltaId, this);
        indexer.setNodeService(nodeService);
        indexer.setTenantService(tenantService);
        indexer.setDictionaryService(dictionaryService);
        // indexer.setLuceneIndexLock(luceneIndexLock);
        indexer.setFullTextSearchIndexer(fullTextSearchIndexer);
        indexer.setContentService(contentService);
        indexer.setMaxAtomicTransformationTime(getMaxTransformationTime());
        return indexer;
    }

    protected LuceneSearcher getSearcher(StoreRef storeRef, LuceneIndexer indexer) throws SearcherException
    {
        storeRef = tenantService.getName(storeRef);

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
    
    protected SearchService getNodeSearcher() throws SearcherException
    {
        ADMLuceneSearcherImpl searcher = ADMLuceneSearcherImpl.getNodeSearcher();
        searcher.setNamespacePrefixResolver(nameSpaceService);
        searcher.setNodeService(nodeService);
        searcher.setTenantService(tenantService);
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryRegister(getQueryRegister());
        searcher.setDictionaryService(dictionaryService);
        searcher.setQueryLanguages(getQueryLanguages());
        return searcher;
    }

   
    protected List<StoreRef> getAllStores()
    {
        return nodeService.getStores();
    }
}
