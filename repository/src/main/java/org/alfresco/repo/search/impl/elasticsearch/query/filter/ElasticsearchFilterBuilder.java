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
package org.alfresco.repo.search.impl.elasticsearch.query.filter;

import static org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryHelper.isEmptyFilterQuery;

import org.apache.lucene.queryparser.classic.ParseException;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchQueryHelper;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Build a filter query and adds it to the current query returning a new query.
 */
public class ElasticsearchFilterBuilder
{

    /**
     * Take the query and the filter queries in the searchParameters and creates a new Elasticsearch query .
     *
     * @param queryBuilder
     * @param searchParameters
     *            The query to convert.
     * @param languageQueryBuilder
     *            the language query builder used to build the filter query
     * @return The Elasticsearch query builder to use in a Elasticsearch search request.
     * @throws ParseException
     *             If there is a problem interpreting the AFTS query.
     */
    public Query getFTSQueryWithFilters(Query queryBuilder, SearchParameters searchParameters,
            LanguageQueryBuilder languageQueryBuilder) throws ParseException
    {
        Query filterQueries = getFilterQueriesBuilder(searchParameters, languageQueryBuilder);
        return QueryBuilders.bool().must(queryBuilder).filter(filterQueries).build().toQuery();
    }

    private Query getFilterQueriesBuilder(SearchParameters searchParameters,
            LanguageQueryBuilder languageQueryBuilder) throws ParseException
    {
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        if (searchParameters.getFilterQueries() != null)
        {
            SearchParameters filterQueryParameters = searchParameters.copy();
            for (String filterQuery : searchParameters.getFilterQueries())
            {
                filterQueryParameters.setQuery(ElasticsearchQueryHelper.cleanUpFilterQueries(filterQuery));
                Query query = languageQueryBuilder.getQuery(filterQueryParameters);
                if (!isEmptyFilterQuery(query))
                {
                    boolQueryBuilder.must(query);
                }
            }
        }

        return boolQueryBuilder.build().toQuery();
    }

}
