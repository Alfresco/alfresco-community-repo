/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.ShardFailure;
import org.opensearch.client.opensearch._types.ShardStatistics;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class SearchExecutionStrategyTest
{

    private final SearchExecutionStrategy strategy = new DummyStrategyImpl();

    private static class DummyStrategyImpl extends SearchExecutionStrategy
    {
        @Override
        public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions)
        {
            return null;
        }
    }

    @Test
    public void validateResponse_noFailures_doesNothing()
    {
        SearchResponse<Object> response = new SearchResponse.Builder<Object>()
                .took(1)
                .timedOut(false)
                .shards(new ShardStatistics.Builder()
                        .total(1).successful(1).skipped(0).failed(0).build())
                .hits(h -> h.maxScore(1.0).hits(List.of()))
                .build();

        // Should not throw
        strategy.validateResponse(response);
        assertNotNull(response);
    }

    @Test
    public void validateResponse_highlightError_throwsOpenSearchException()
    {
        ErrorCause cause = new ErrorCause.Builder()
                .type("illegal_argument_exception")
                .reason("index.highlight.max_analyzed_offset: Highlight error")
                .build();
        ShardFailure failure = new ShardFailure.Builder()
                .reason(cause)
                .shard(0)
                .build();
        SearchResponse<Object> response = new SearchResponse.Builder<Object>()
                .took(1)
                .timedOut(false)
                .shards(new ShardStatistics.Builder()
                        .total(1).successful(0).skipped(0).failed(1)
                        .failures(List.of(failure)).build())
                .hits(h -> h.maxScore(1.0).hits(List.of()))
                .build();

        try
        {
            strategy.validateResponse(response);
            fail("Expected OpenSearchException");
        }
        catch (OpenSearchException e)
        {
            assertTrue(e.getMessage().contains("illegal_argument_exception"));
        }
    }

    @Test
    public void validateResponse_otherError_logsButDoesNotThrow()
    {
        ErrorCause cause = new ErrorCause.Builder()
                .type("some_other_type")
                .reason("some other error")
                .build();
        ShardFailure failure = new ShardFailure.Builder()
                .reason(cause)
                .shard(0)
                .build();
        SearchResponse<Object> response = new SearchResponse.Builder<Object>()
                .took(1)
                .timedOut(false)
                .shards(new ShardStatistics.Builder()
                        .total(1).successful(0).skipped(0).failed(1)
                        .failures(List.of(failure)).build())
                .hits(h -> h.maxScore(1.0).hits(List.of()))
                .build();

        // Should not throw
        strategy.validateResponse(response);
        assertNotNull(response);
    }
}
