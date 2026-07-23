/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
import java.util.List;
import java.util.Optional;

import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilder;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

// Deep-pagination strategy based on Elasticsearch search_after over a Point-In-Time (PIT). The opaque cursor carried on SearchParameters.getSearchAfter() holds the pagination session's PIT id and the previous page's last sort values. On the first page (empty cursor) a PIT is opened on every page the query runs with from=0, the search_after clause and a sort that ends with the _shard_doc tiebreaker, so pages contain no gaps or duplicates and the view stays consistent for the whole session.
// Each response returns a (possibly rotated) PIT id which is folded into the next cursor. The PIT is closed on the last page (fewer hits than requested) and abandoned PITs expire via their keep_alive. Because it never asks Elasticsearch to skip documents, it is not bound by {@code index.max_result_window} and never loads-and-skips results in memory.
public class SearchAfterSearchStrategy extends SearchExecutionStrategy
{
    private final SearchRequestBuilderService requestBuilderService;
    private final ElasticsearchHttpClientFactory httpClientFactory;
    private final ElasticsearchResultSetBuilder resultSetBuilder;
    private final ElasticsearchPitService pitService;
    private final String keepAlive;
    private final int maxResultWindow;

    public SearchAfterSearchStrategy(
            SearchRequestBuilderService requestBuilderService,
            ElasticsearchHttpClientFactory httpClientFactory,
            ElasticsearchResultSetBuilder resultSetBuilder,
            ElasticsearchPitService pitService,
            String keepAlive,
            int maxResultWindow)
    {
        super();
        this.requestBuilderService = requestBuilderService;
        this.httpClientFactory = httpClientFactory;
        this.resultSetBuilder = resultSetBuilder;
        this.pitService = pitService;
        this.keepAlive = keepAlive;
        this.maxResultWindow = maxResultWindow;
    }

    /**
     * Executes a single {@code search_after} page fetch within a PIT.
     *
     * @param searchParameters
     *            Search configuration, including the opaque {@code searchAfter} cursor.
     * @param queryWithPermissions
     *            Query including permissions.
     * @return ResultSet containing the requested page; its {@code nextCursor} is the opaque cursor for the following page, or {@code null} on the last page.
     * @throws IOException
     *             If the search fails.
     */
    @Override
    public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions) throws IOException
    {
        int limit = searchParameters.getLimit();
        int size = limit < 0 ? maxResultWindow : limit;
        SearchAfterCursor.Decoded cursor = SearchAfterCursor.decode(searchParameters.getSearchAfter());
        String indexName = requestBuilderService.getElasticIndex(searchParameters.getStores());

        String pitId = cursor.pitId();
        boolean firstPage = pitId == null || pitId.isBlank();
        if (firstPage)
        {
            pitId = pitService.open(indexName, keepAlive);
        }

        try
        {
            SearchAfterContext context = new SearchAfterContext(pitId, keepAlive, cursor.sort());
            SearchRequest searchRequest = requestBuilderService.buildSearchAfterRequest(searchParameters, queryWithPermissions, size, context);

            LOGGER.debug("Execute search_after query request: {}", searchRequest.toJsonString());
            SearchResponse<Object> searchResponse = httpClientFactory.getElasticsearchClient().search(searchRequest, Object.class);

            LOGGER.debug("Response hits from search_after query {}", searchResponse.hits().total().value());
            LOGGER.trace("Query response JSON: {}", searchResponse.toJsonString());

            validateResponse(searchResponse);

            List<Hit<Object>> hits = Optional.ofNullable(searchResponse.hits()).map(HitsMetadata::hits).orElse(List.of());
            // The response may rotate the PIT id always carry the latest one forward.
            String responsePitId = searchResponse.pitId() != null ? searchResponse.pitId() : pitId;
            boolean lastPage = hits.size() < size;

            String nextCursor;
            if (lastPage)
            {
                pitService.close(responsePitId);
                nextCursor = null;
            }
            else
            {
                nextCursor = SearchAfterCursor.encode(responsePitId, lastSort(hits));
            }

            return resultSetBuilder.build(searchParameters, searchResponse, nextCursor);
        }
        catch (IOException exception)
        {
            if (firstPage)
            {
                pitService.close(pitId);
            }
            LOGGER.error("Error during search_after search execution: " + exception);
            throw new IllegalStateException("Error during search_after search execution", exception);
        }
        catch (RuntimeException exception)
        {
            if (firstPage)
            {
                pitService.close(pitId);
            }
            throw exception;
        }
    }

    private static List<String> lastSort(List<Hit<Object>> hits)
    {
        if (hits.isEmpty())
        {
            return List.of();
        }
        List<String> sort = hits.get(hits.size() - 1).sort();
        return sort == null ? List.of() : sort;
    }
}
