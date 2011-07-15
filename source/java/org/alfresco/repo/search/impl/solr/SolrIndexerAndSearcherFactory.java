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
package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.NoActionIndexer;
import org.alfresco.repo.search.impl.lucene.ADMLuceneSearcherImpl;
import org.alfresco.repo.search.impl.lucene.AbstractIndexerAndSearcher;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * @author Andy
 *
 */
public class SolrIndexerAndSearcherFactory extends AbstractIndexerAndSearcher
{

    private DictionaryService dictionaryService;
    private NamespacePrefixResolver namespacePrefixResolver;
    private NodeService nodeService;
    private QueryRegisterComponent queryRegister;
    private TenantService tenantService;
    private String baseUrl;
    
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public QueryRegisterComponent getQueryRegister()
    {
        return queryRegister;
    }

    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    public TenantService getTenantService()
    {
        return tenantService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#getIndexer(org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
    public Indexer getIndexer(StoreRef storeRef) throws IndexerException
    {
        return new NoActionIndexer();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#getSearcher(org.alfresco.service.cmr.repository.StoreRef, boolean)
     */
    @Override
    public SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException
    {
        //storeRef = tenantService.getName(storeRef);

         SolrSearchService searchService = new SolrSearchService();
         searchService.setDictionaryService(dictionaryService);
         searchService.setNamespacePrefixResolver(namespacePrefixResolver);
         searchService.setNodeService(nodeService);
         searchService.setQueryLanguages(getQueryLanguages());
         searchService.setQueryRegister(queryRegister);
         searchService.setTenantService(tenantService);
         return searchService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.IndexerAndSearcher#flush()
     */
    @Override
    public void flush()
    {
        // Nothing to do
    }

}
