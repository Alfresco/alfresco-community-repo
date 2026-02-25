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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.ClearScrollRequest;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilder;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class ScrollSearchStrategy extends SearchExecutionStrategy
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrollSearchStrategy.class);

    private final SearchRequestBuilderService requestBuilderService;
    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final ElasticsearchResultSetBuilder resultSetBuilder;
    private final Time scrollTime;
    private final int batchSize;

    public ScrollSearchStrategy(
            SearchRequestBuilderService requestBuilderService,
            ElasticsearchHttpClientFactory httpClientFactory,
            ElasticsearchResultSetBuilder resultSetBuilder,
            String scrollTime,
            int batchSize)
    {
        super();
        this.requestBuilderService = requestBuilderService;
        this.httpClientFactory = httpClientFactory;
        this.resultSetBuilder = resultSetBuilder;
        this.scrollTime = new Time.Builder().time(scrollTime).build();
        this.batchSize = batchSize;
    }

    /**
     * Executes a search using the scroll API. Fetches results in batches and handles scroll context cleanup.
     * 
     * @param searchParameters
     *            Search configuration.
     * @param queryWithPermissions
     *            Query including permissions.
     * @return ResultSet containing search results.
     * @throws IOException
     *             If search or scroll fails.
     */
    @Override
    public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions) throws IOException
    {
        AtomicInteger skipCount = new AtomicInteger(searchParameters.getSkipCount());
        int limit = searchParameters.getLimit();
        String indexName = requestBuilderService.getElasticIndex(searchParameters.getStores());

        SearchRequest searchRequest = requestBuilderService.buildSearchRequest(
                searchParameters,
                queryWithPermissions,
                batchSize,
                scrollTime,
                indexName);
        LOGGER.debug("Execute standard query request: {}", searchRequest.toJsonString());
        OpenSearchClient client = httpClientFactory.getElasticsearchClient();
        SearchResponse<Object> searchResponse = client.search(searchRequest, Object.class);
        LOGGER.debug("Response hits from query {}", searchResponse.hits().total().value());
        LOGGER.trace("Query response JSON: {}", searchResponse.toJsonString());

        validateResponse(searchResponse);

        long totalHits = searchResponse.hits().total().value();
        List<Hit<Object>> resultList = new ArrayList<>(skipHits(searchResponse.hits().hits(), skipCount, limit));
        String scrollId = searchResponse.scrollId();
        long startingScroll = System.nanoTime();
        try
        {
            while (resultList.size() < limit && scrollId != null)
            {
                ScrollRequest scrollRequest = new ScrollRequest.Builder()
                        .scrollId(scrollId)
                        .scroll(scrollTime)
                        .build();
                ScrollResponse<Object> scrollResponse = client.scroll(scrollRequest, Object.class);
                LOGGER.debug("Response hits from scroll {}", scrollResponse.hits().total().value());
                LOGGER.trace("Scroll response JSON: {}", scrollResponse.toJsonString());
                validateResponse(scrollResponse);

                scrollId = scrollResponse.scrollId();
                resultList.addAll(skipHits(scrollResponse.hits().hits(), skipCount, limit - resultList.size()));
            }
        }
        catch (IOException exception)
        {
            LOGGER.error("Error during scroll execution", exception);
            throw new IllegalStateException("Error during scroll execution", exception);
        }
        finally
        {
            clearScrollContext(scrollId, client);
        }
        long timeTaken = (System.nanoTime() - startingScroll) / 1_000_000L;
        return resultSetBuilder.build(searchParameters, resultList, totalHits, timeTaken);
    }

    /**
     * Skip requested hits and cap to remaining limit.
     */
    private List<Hit<Object>> skipHits(List<Hit<Object>> hits, AtomicInteger skipCount, int remainingLimit)
    {
        if (hits == null || hits.isEmpty() || remainingLimit <= 0)
        {
            return Collections.emptyList();
        }

        int toSkip = skipCount.get();
        if (toSkip > 0)
        {
            if (toSkip >= hits.size())
            {
                skipCount.addAndGet(-hits.size());
                return Collections.emptyList();
            }
            hits = hits.subList(toSkip, hits.size());
            skipCount.addAndGet(-toSkip);
        }

        if (hits.size() > remainingLimit)
        {
            return new ArrayList<>(hits.subList(0, remainingLimit));
        }
        return new ArrayList<>(hits);
    }

    /**
     * Clears the scroll context in OpenSearch to release resources.
     * 
     * @param scrollId
     *            The scroll identifier.
     * @param client
     *            The OpenSearch client.
     */
    private void clearScrollContext(String scrollId, OpenSearchClient client)
    {
        if (scrollId != null)
        {
            try
            {
                ClearScrollRequest clearScrollRequest = new ClearScrollRequest.Builder()
                        .scrollId(scrollId)
                        .build();
                client.clearScroll(clearScrollRequest);
                LOGGER.debug("Successfully cleared scroll context for scrollId={}", scrollId);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to clear scroll context for scrollId=" + scrollId, exception);
            }
        }
    }
}
