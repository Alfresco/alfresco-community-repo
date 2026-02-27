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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.io.IOException;

import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilder;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class StandardSearchStrategy extends SearchExecutionStrategy
{

    private final SearchRequestBuilderService requestBuilderService;
    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final ElasticsearchResultSetBuilder resultSetBuilder;
    private final int maxResultWindow;

    public StandardSearchStrategy(
            SearchRequestBuilderService requestBuilderService,
            ElasticsearchHttpClientFactory httpClientFactory,
            ElasticsearchResultSetBuilder resultSetBuilder,
            int maxResultWindow)
    {
        super();
        this.requestBuilderService = requestBuilderService;
        this.httpClientFactory = httpClientFactory;
        this.resultSetBuilder = resultSetBuilder;
        this.maxResultWindow = maxResultWindow;
    }

    /**
     * Executes a standard search.
     * 
     * @param searchParameters
     *            Search configuration.
     * @param queryWithPermissions
     *            Query including permissions.
     * @return ResultSet containing search results.
     * @throws IOException
     *             If search fails.
     */
    public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions) throws IOException
    {
        int skipCount = searchParameters.getSkipCount();
        int limit = searchParameters.getLimit();
        int effectiveLimit = limit < 0 ? maxResultWindow : limit;
        String indexName = requestBuilderService.getElasticIndex(searchParameters.getStores());
        SearchRequest searchRequest = requestBuilderService.buildSearchRequest(
                searchParameters,
                queryWithPermissions,
                skipCount,
                effectiveLimit,
                indexName);

        try
        {
            LOGGER.debug("Execute standard query request: {}", searchRequest.toJsonString());
            SearchResponse<Object> searchResponse = httpClientFactory.getElasticsearchClient().search(searchRequest, Object.class);

            LOGGER.debug("Response hits from query {}", searchResponse.hits().total().value());
            LOGGER.trace("Query response JSON: {}", searchResponse.toJsonString());

            validateResponse(searchResponse);
            return resultSetBuilder.build(searchParameters, searchResponse);
        }
        catch (IOException exception)
        {
            LOGGER.error("Error during standard search execution: " + exception);
            throw new IllegalStateException("Error during standard search execution", exception);
        }
    }
}
