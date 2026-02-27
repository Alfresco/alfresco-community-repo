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
import java.util.List;

import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.ShardFailure;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public abstract class SearchExecutionStrategy implements SearchStrategy
{

    protected static final String HIGHLIGHT_ERROR_KEYWORD = "index.highlight.max_analyzed_offset";
    protected static final Logger LOGGER = LoggerFactory.getLogger(SearchExecutionStrategy.class);

    public abstract ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions) throws IOException;

    /**
     * Validates the search response for shard failures and highlight errors. Throws exception if highlight error is detected.
     * 
     * @param searchResponse
     *            The OpenSearch response.
     */
    protected void validateResponse(SearchResponse<?> searchResponse)
    {
        if (searchResponse.shards() == null)
        {
            return; // No shard failures, nothing to validate
        }

        List<ShardFailure> failures = searchResponse.shards().failures();

        if (failures == null)
        {
            return;
        }

        for (ShardFailure failure : failures)
        {
            ErrorCause cause = failure.reason();
            if (cause.reason() != null)
            {
                LOGGER.error("Shard failure: {}", cause.reason());
                if (isHighlightError(cause.reason()))
                {
                    // Throw the OpenSearchException in a similar way as ElasticSearch does so we can treat it
                    throw new OpenSearchException(new ErrorResponse.Builder()
                            .error(new ErrorCause.Builder()
                                    .type("illegal_argument_exception")
                                    .reason(cause.reason())
                                    .rootCause(List.of(cause))
                                    .build())
                            .build());
                }
            }
        }
    }

    /**
     * Checks if the error message is related to highlight offset.
     * 
     * @param message
     *            Error message.
     * @return true if highlight error, false otherwise.
     */
    protected boolean isHighlightError(String message)
    {
        return message != null && message.contains(HIGHLIGHT_ERROR_KEYWORD);
    }
}
