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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;

public class AFTSBooleanOperatorsAdaptorTest
{

    private AFTSBooleanOperatorsAdaptor expressionAdaptor;

    @Before
    public void setUp()
    {
        expressionAdaptor = new AFTSBooleanOperatorsAdaptor();
    }

    @Test
    public void whenAddQueryAsRequired() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addRequired(query.toQuery());
        Query result = expressionAdaptor.getQuery();
        assertEquals(query._queryKind().jsonValue(), result._kind().jsonValue());

        TermQuery anotherQuery = QueryBuilders.term().field("a").value(FieldValue.of("b")).build().toQuery().term();
        expressionAdaptor.addRequired(anotherQuery.toQuery());
        BoolQuery booleanQueryResult = expressionAdaptor.getQuery().bool();
        assertEquals(query._queryKind().jsonValue(), booleanQueryResult.must().get(0)._kind().jsonValue());
        assertEquals(anotherQuery._queryKind().jsonValue(), booleanQueryResult.must().get(1)._kind().jsonValue());
    }

    @Test
    public void whenAddQueryAsExcluded() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addExcluded(query.toQuery());
        Query result = expressionAdaptor.getQuery();
        BoolQuery boolQueryBuilder = result.bool();
        assertEquals(0L, boolQueryBuilder.must().size());
        assertEquals(1L, boolQueryBuilder.mustNot().size());
        assertEquals(0L, boolQueryBuilder.should().size());
        assertEquals(query._queryKind().jsonValue(), boolQueryBuilder.mustNot().get(0)._kind().jsonValue());

        TermQuery anotherQuery = QueryBuilders.term().field("a").value(FieldValue.of("b")).build().toQuery().term();
        expressionAdaptor.addExcluded(anotherQuery.toQuery());
        BoolQuery booleanQueryResult = expressionAdaptor.getQuery().bool();
        assertEquals(query._queryKind().jsonValue(), booleanQueryResult.mustNot().get(0)._kind().jsonValue());
        assertEquals(anotherQuery._queryKind().jsonValue(), booleanQueryResult.mustNot().get(1)._kind().jsonValue());
    }

    @Test
    public void whenAddQueryAsOptional() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addOptional(query.toQuery());
        Query result = expressionAdaptor.getQuery();
        assertEquals(query._queryKind().jsonValue(), result._kind().jsonValue());

        TermQuery anotherQuery = QueryBuilders.term().field("a").value(FieldValue.of("b")).build().toQuery().term();
        expressionAdaptor.addOptional(anotherQuery.toQuery());
        BoolQuery booleanQueryResult = expressionAdaptor.getQuery().bool();
        assertEquals(query._queryKind().jsonValue(), booleanQueryResult.should().get(0)._kind().jsonValue());
        assertEquals(anotherQuery._queryKind().jsonValue(), booleanQueryResult.should().get(1)._kind().jsonValue());
    }

    @Test
    public void whenAddQueryAsRequiredWithBoost() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().boost(1.5f).build();
        expressionAdaptor.addRequired(query.toQuery(), 1.5f);
        Query result = expressionAdaptor.getQuery();
        assertEquals(1.5f, result.matchAll().boost(), 0.1);
    }

    @Test
    public void whenAddQueryAsExcludedWithBoost() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().boost(1.5f).build();
        expressionAdaptor.addExcluded(query.toQuery(), 1.5f);
        BoolQuery result = expressionAdaptor.getQuery().bool();
        assertEquals(1.5f, result.mustNot().get(0).matchAll().boost(), 0.1);
    }

    @Test
    public void whenAddQueryAsOptionalWithBoost() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().boost(1.5f).build();
        expressionAdaptor.addOptional(query.toQuery(), 1.5f);
        Query result = expressionAdaptor.getQuery();
        assertEquals(1.5f, result.matchAll().boost(), 0.1);
    }

    @Test
    public void whenGetRequiredNegatedQuery() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addRequired(query.toQuery());
        Query result = expressionAdaptor.getNegatedQuery();
        // assertEquals(1.0f, result.bool().boost(), 0.1);
        BoolQuery boolQueryBuilder = result.bool();
        assertEquals(0L, boolQueryBuilder.must().size());
        assertEquals(1L, boolQueryBuilder.mustNot().size());
        assertEquals(0L, boolQueryBuilder.should().size());
        Query negatedQuery = boolQueryBuilder.mustNot().get(0);
        assertEquals(query._queryKind().jsonValue(), negatedQuery._kind().jsonValue());
    }

    @Test
    public void whenGetOptionalNegatedQuery() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addOptional(query.toQuery());
        Query result = expressionAdaptor.getNegatedQuery();
        BoolQuery boolQueryBuilder = result.bool();
        assertEquals(0L, boolQueryBuilder.must().size());
        assertEquals(1L, boolQueryBuilder.mustNot().size());
        assertEquals(0L, boolQueryBuilder.should().size());
        Query negatedQuery = boolQueryBuilder.mustNot().get(0);
        assertEquals(query._queryKind().jsonValue(), negatedQuery._kind().jsonValue());
    }

    @Test
    public void whenGetExcludedNegatedQuery() throws ParseException
    {
        MatchAllQuery query = QueryBuilders.matchAll().build();
        expressionAdaptor.addExcluded(query.toQuery());
        Query result = expressionAdaptor.getNegatedQuery();
        BoolQuery boolQueryBuilder = result.bool();
        assertEquals(0L, boolQueryBuilder.must().size());
        assertEquals(1L, boolQueryBuilder.mustNot().size());
        assertEquals(0L, boolQueryBuilder.should().size());
        Query negatedQuery = boolQueryBuilder.mustNot().get(0);
        assertEquals(query._queryKind().jsonValue(), negatedQuery.bool().mustNot().get(0)._kind().jsonValue());
    }

}
