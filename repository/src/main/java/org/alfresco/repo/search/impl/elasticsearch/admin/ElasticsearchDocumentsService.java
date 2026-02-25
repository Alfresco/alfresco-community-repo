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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Class to retrieve documents from an Elasticsearch cluster.
 */
public class ElasticsearchDocumentsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchDocumentsService.class);

    private ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;

    public List<ElasticsearchDocument> getDocuments(List<String> id, int size)
    {
        return getDocuments(id, Collections.emptyList(), size);
    }

    /**
     * Get the documents currently indexed by the Elasticsearch cluster.
     *
     * @param ids
     *            - The IDs of the documents to retrieve.
     * @param sourceFields
     *            - The fields to be included in the contents of the document _source.
     * @param size
     *            - Option that determines the number of search hits to return.
     * @return The documents or empty list if it was not possible to obtain documents.
     */
    public List<ElasticsearchDocument> getDocuments(List<String> ids, List<String> sourceFields, int size)
    {
        String index = elasticsearchHttpClientFactory.getIndexName();
        SearchRequest.Builder sourceBuilder = getSearchSourceBuilder(ids, sourceFields, size);
        sourceBuilder.index(index);

        try
        {
            SearchResponse<Map> searchResponse = elasticsearchHttpClientFactory.getElasticsearchClient()
                    .search(sourceBuilder.build(), Map.class);

            return Optional.ofNullable(searchResponse)
                    .map(searchHits -> searchResponse.hits().hits().stream()
                            .map(ElasticsearchDocument::new)
                            .collect(toList()))
                    .orElseThrow(() -> new NoSuchElementException("No data found in search results"));
        }
        catch (IOException | NoSuchElementException e)
        {
            LOGGER.warn("Failed to get documents in index {}", index, e);
        }
        return Collections.emptyList();
    }

    private static SearchRequest.Builder getSearchSourceBuilder(List<String> ids, List<String> sourceFields, int size)
    {
        SearchRequest.Builder sourceBuilder = new SearchRequest.Builder().query(QueryBuilders.ids().values(ids).build().toQuery()).size(size);

        if (sourceFields.isEmpty())
        {
            sourceBuilder.source(new SourceConfig.Builder().fetch(true).build());
        }
        else
        {
            sourceBuilder.source(new SourceConfig.Builder().filter(new SourceFilter.Builder().includes(sourceFields).build()).build());
        }

        return sourceBuilder;
    }

    public void setElasticsearchHttpClientFactory(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory)
    {
        this.elasticsearchHttpClientFactory = elasticsearchHttpClientFactory;
    }
}
