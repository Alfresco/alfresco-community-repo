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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import org.apache.lucene.queryparser.classic.ParseException;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchNoneQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;

import org.alfresco.repo.search.adaptor.QueryParserExpressionAdaptor;

/**
 * Query Expression Adapter for Elasticsearch This class aims to build the query to execute in Elasticsearch putting all together sub-queries defined in ElasticsearchQueryParserAdaptor.
 */
public class AFTSBooleanOperatorsAdaptor
        implements QueryParserExpressionAdaptor<Query, ParseException>
{
    private BoolQuery booleanQuery;

    AFTSBooleanOperatorsAdaptor()
    {
        this.booleanQuery = QueryBuilders.bool().build();
    }

    @Override
    public void addRequired(Query queryBuilder) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().must(queryBuilder).build();
    }

    @Override
    public void addExcluded(Query queryBuilder) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().mustNot(queryBuilder).build();
    }

    @Override
    public void addOptional(Query queryBuilder) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().should(queryBuilder).build();
    }

    @Override
    public void addRequired(Query queryBuilder, float boost) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().must(QueryBooster.boost(queryBuilder, boost)).build();
    }

    @Override
    public void addExcluded(Query queryBuilder, float boost) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().mustNot(QueryBooster.boost(queryBuilder, boost)).build();
    }

    @Override
    public void addOptional(Query queryBuilder, float boost) throws ParseException
    {
        this.booleanQuery = booleanQuery.toBuilder().should(QueryBooster.boost(queryBuilder, boost)).build();
    }

    @Override
    public Query getQuery() throws ParseException
    {
        Query result;
        if (hasSize(0, 0, 0))
        {
            result = new MatchNoneQuery.Builder().build().toQuery();
        }
        else
        {
            result = getSimplifiedQuery();
        }
        return result;
    }

    @Override
    public Query getNegatedQuery() throws ParseException
    {
        Query result;
        if (hasSize(0, 0, 0))
        {
            result = QueryBuilders.matchAll().build().toQuery();
        }
        else
        {
            result = QueryBuilders.bool().mustNot(getSimplifiedQuery()).build().toQuery();
        }
        return result;
    }

    private Query getSimplifiedQuery()
    {
        Query result;
        if (hasSize(1, 0, 0))
        {
            result = booleanQuery.must().get(0);
        }
        else if (hasSize(0, 1, 0))
        {
            result = booleanQuery.should().get(0);
        }
        else
        {
            result = booleanQuery.toQuery();
        }
        return result;
    }

    private boolean hasSize(int must, int should, int mustNot)
    {
        return booleanQuery.must().size() == must &&
                booleanQuery.should().size() == should &&
                booleanQuery.mustNot().size() == mustNot;
    }

}
