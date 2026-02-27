/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch;

import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.QueryRegisterComponent;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.AbstractIndexerAndSearcher;
import org.alfresco.repo.search.impl.NoActionIndexer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Factory for Indexer and Searcher instances for Elasticsearch
 */
public class ElasticsearchSearchServiceFactory extends AbstractIndexerAndSearcher
{
    private QueryRegisterComponent queryRegister;
    private NodeService nodeService;
    private DictionaryService dictionaryService;

    /**
     * Elasticsearch is indexing the repository listening queue messages from an external service, so no Indexer is available for the Repository.
     */
    @Override
    public Indexer getIndexer(StoreRef storeRef) throws IndexerException
    {
        return new NoActionIndexer();
    }

    @Override
    public SearchService getSearcher(StoreRef storeRef, boolean searchDelta) throws SearcherException
    {
        return new ElasticsearchSearchService(queryRegister, getQueryLanguages(), nodeService, dictionaryService);
    }

    @Override
    public void flush()
    {}

    public void setQueryRegister(QueryRegisterComponent queryRegister)
    {
        this.queryRegister = queryRegister;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}
