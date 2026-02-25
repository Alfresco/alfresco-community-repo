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

import java.util.ArrayList;
import java.util.List;

import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.search.impl.elasticsearch.permission.ElasticsearchPermissionQueryFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.filter.ElasticsearchFilterBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryLanguage;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * This class aims to search a query to elasticsearch adding additional parameters based on the execution context like permission filtering and pagination. Supports plain http and secured https connections.
 */
public class ElasticsearchQueryExecutor extends AbstractLuceneQueryLanguage
{
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchQueryExecutor.class);

    private final ElasticsearchPermissionQueryFactory elasticsearchPermissionQueryFactory;
    private final LanguageQueryBuilder languageQueryBuilder;
    private final LanguageQueryBuilder defaultLanguageQueryBuilder;
    private final ElasticsearchFilterBuilder elasticsearchFilterBuilder;
    private final boolean includeGroupsForRoleAdmin;

    public static final int TRACK_TOTAL_HITS_ACCURATE = 2147483647;
    public static final int TRACK_TOTAL_HITS_DISABLED = -1;
    public static final int DEFAULT_TRACK_TOTAL_HITS_UP_TO = 10000;
    private static final String HIGHLIGHT_ERROR_KEYWORD = "index.highlight.max_analyzed_offset";
    private final SearchStrategy strategySelector;

    public ElasticsearchQueryExecutor(ElasticsearchPermissionQueryFactory elasticsearchPermissionQueryFactory,
            LanguageQueryBuilder languageQueryBuilder,
            LanguageQueryBuilder defaultLanguageQueryBuilder,
            ElasticsearchFilterBuilder elasticsearchFilterBuilder,
            boolean includeGroupsForRoleAdmin, SearchStrategy strategySelector)
    {
        this.elasticsearchPermissionQueryFactory = elasticsearchPermissionQueryFactory;
        this.languageQueryBuilder = languageQueryBuilder;
        this.elasticsearchFilterBuilder = elasticsearchFilterBuilder;
        this.includeGroupsForRoleAdmin = includeGroupsForRoleAdmin;
        this.defaultLanguageQueryBuilder = defaultLanguageQueryBuilder;
        this.strategySelector = strategySelector;
    }

    /**
     * Return search results for query specified in searchParameters
     *
     * @param searchParameters
     *            Query string and additional searching parameters
     */
    @Override
    public ResultSet executeQuery(SearchParameters searchParameters)
    {
        try
        {
            Query query = languageQueryBuilder.getQuery(searchParameters);

            Query filteredQuery = elasticsearchFilterBuilder
                    .getFTSQueryWithFilters(query, searchParameters, defaultLanguageQueryBuilder);

            Query queryWithPermissions = elasticsearchPermissionQueryFactory
                    .getQueryWithPermissionFilter(filteredQuery, this.includeGroupsForRoleAdmin);

            return strategySelector.executeSearch(searchParameters, queryWithPermissions);
        }
        catch (UnsupportedOperationException exception)
        {
            throw exception;
        }
        catch (OpenSearchException exception)
        {
            return handleOpenSearchExceptions(exception, searchParameters);
        }
        catch (Exception exception)
        {
            LOGGER.debug("Exception while executing query.", exception);
            throw new RuntimeException(exception);
        }
    }

    public LanguageQueryBuilder getLanguageQueryBuilder()
    {
        return languageQueryBuilder;
    }

    /**
     * Handle OpenSearch exceptions
     *
     * The thrown error may be a generic "all shards failed" error, so we need to check the root cause to log it. If the exception is related to the configured index.highlight.max_analyzed_offset, it retries the query without highlighting. If the exception is not related to highlighting, it throws a RuntimeException.
     *
     * @param exception
     *            The Exception to handle
     * @param searchParameters
     *            The SearchParameters used for the query
     * @return ResultSet
     */
    private ResultSet handleOpenSearchExceptions(OpenSearchException exception, SearchParameters searchParameters)
    {
        List<String> errorMessages = aggregateOpenSearchErrors(exception);

        // If our search request contains highlighting and an error relating to highlighting occured, we retry the query
        // without highlighting
        if (searchParameters.getHighlight() != null && containsHighlightError(errorMessages))
        {
            LOGGER.warn("Retrying the query without highlighting.");
            searchParameters.setHighlight(null);
            return executeQuery(searchParameters);
        }

        throw exception;
    }

    /**
     * Check if the list of error messages contain a highlight error
     * 
     * @param errorMessages
     * @return
     */
    private boolean containsHighlightError(List<String> errorMessages)
    {
        return errorMessages.stream().anyMatch(this::isHightlightError);
    }

    /**
     * Check if the error message string is a highlight error
     * 
     * @param message
     * @return
     */
    private boolean isHightlightError(String message)
    {
        return message != null && message.contains(HIGHLIGHT_ERROR_KEYWORD);
    }

    /**
     * This method gathers all the error messages thrown to a list and logs them.
     *
     * @param exception
     * @return
     */
    private List<String> aggregateOpenSearchErrors(OpenSearchException exception)
    {
        List<String> errors = new ArrayList<>();

        exception.error().rootCause().stream()
                .map(cause -> cause.reason())
                .filter(reason -> reason != null)
                .forEach(reason -> {
                    errors.add(reason);
                    LOGGER.error("Root cause: {}", reason);
                });

        return errors;
    }
}
