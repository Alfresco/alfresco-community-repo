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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.reflections.scanners.Scanners.SubTypes;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.*;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.reflections.Reflections;

@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyMethods"})
public class QueryBoosterTest
{
    private static final Set<Class<?>> TESTED_SUB_CLASSES = ConcurrentHashMap.newKeySet();
    private static Set<Class<?>> baseQuerySubtypes;

    @BeforeClass
    public static void identifyChildClassesOfQueryBaseClass()
    {
        Reflections reflections = new Reflections("org.opensearch.client");
        baseQuerySubtypes = reflections.get(SubTypes.of(QueryBase.class).asClass())
                .stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .collect(Collectors.toSet());
    }

    @AfterClass
    public static void verifyAllQueryBaseSubClassesAreTested()
    {
        assertThat(TESTED_SUB_CLASSES).containsExactlyInAnyOrderElementsOf(baseQuerySubtypes);
    }

    @Test
    public void testBoostBoolQuery()
    {
        // given
        BoolQuery originalQuery = new BoolQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());
        Query query = new Query.Builder().bool(originalQuery).build();

        // when
        Query boostedQuery = QueryBooster.boost(query, 2.0f);

        // then
        assertEquals(2.0f, ((BoolQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testBoostTermQuery()
    {
        // given
        TermQuery originalQuery = new TermQuery.Builder().field("test").value(FieldValue.TRUE).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());
        Query query = new Query.Builder().term(originalQuery).build();

        // when
        Query boostedQuery = QueryBooster.boost(query, 2.0f);

        // then
        assertEquals(2.0f, ((TermQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testBoostQueryStringQuery()
    {
        // given
        QueryStringQuery originalQuery = new QueryStringQuery.Builder().query("test").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());
        Query query = new Query.Builder().queryString(originalQuery).build();

        // when
        Query boostedQuery = QueryBooster.boost(query, 2.0f);

        // then
        assertEquals(2.0f, ((QueryStringQuery) boostedQuery._get()).boost(), 1.0);
    }

    @Test
    public void testBoostMatchAllQuery()
    {
        // given
        MatchAllQuery originalQuery = new MatchAllQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());
        Query query = new Query.Builder().matchAll(originalQuery).build();

        // when
        Query boostedQuery = QueryBooster.boost(query, 2.0f);

        // then
        // Should not boost if the query builder does not allow it
        assertNull(((MatchAllQuery) boostedQuery._get()).boost());
    }

    @Test
    public void testBoostingQuery()
    {
        // given
        var originalQuery = new BoostingQuery.Builder()
                .negativeBoost(3.25)
                .negative(new BoolQuery.Builder().build().toQuery())
                .positive(new BoolQuery.Builder().build().toQuery())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((BoostingQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testCombinedFieldsQuery()
    {
        // given
        var originalQuery = new CombinedFieldsQuery.Builder().fields(Collections.emptyList()).query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((CombinedFieldsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testCommonTermsQuery()
    {
        // given
        var originalQuery = new CommonTermsQuery.Builder().field("field").query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((CommonTermsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testConstantScoreQuery()
    {
        // given
        var originalQuery = new ConstantScoreQuery.Builder().filter(new BoolQuery.Builder().build().toQuery()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((ConstantScoreQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testDisMaxQuery()
    {
        // given
        var originalQuery = new DisMaxQuery.Builder().queries(Collections.emptyList()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((DisMaxQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testDistanceFeatureQuery()
    {
        // given
        var originalQuery = new DistanceFeatureQuery.Builder().origin(JsonData.of("{}")).pivot(JsonData.of("{}")).field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((DistanceFeatureQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testExistsQuery()
    {
        // given
        var originalQuery = new ExistsQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((ExistsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testFunctionScoreQuery()
    {
        // given
        var originalQuery = new FunctionScoreQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((FunctionScoreQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testFuzzyQuery()
    {
        // given
        var originalQuery = new FuzzyQuery.Builder().field("field").value(FieldValue.TRUE).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((FuzzyQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testGeoBoundingBoxQuery()
    {
        // given
        var originalQuery = new GeoBoundingBoxQuery.Builder().field("field").boundingBox(
                new GeoBounds.Builder().coords(
                        new CoordsGeoBounds.Builder().top(11).bottom(13).left(17).right(19).build()).build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((GeoBoundingBoxQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testGeoDistanceQuery()
    {
        // given
        var originalQuery = new GeoDistanceQuery.Builder().field("field").location(new GeoLocation.Builder().coords(Collections.emptyList()).build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((GeoDistanceQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testGeoPolygonQuery()
    {
        // given
        var originalQuery = new GeoPolygonQuery.Builder().field("field").polygon(new GeoPolygonPoints.Builder().points(Collections.emptyList()).build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertNull(((GeoPolygonQuery) boostedQuery._get()).boost());
    }

    @Test
    public void testGeoShapeQuery()
    {
        // given
        var originalQuery = new GeoShapeQuery.Builder().field("field").shape(new GeoShapeFieldQuery.Builder().build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((GeoShapeQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testHasChildQuery()
    {
        // given
        var originalQuery = new HasChildQuery.Builder().query(new BoolQuery.Builder().build().toQuery()).type("type").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((HasChildQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testHasParentQuery()
    {
        // given
        var originalQuery = new HasParentQuery.Builder().parentType("parentType").query(new BoolQuery.Builder().build().toQuery()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((HasParentQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testHybridQuery()
    {
        // given
        var originalQuery = new HybridQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((HybridQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testIdsQuery()
    {
        // given
        var originalQuery = new IdsQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((IdsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testIntervalsQuery()
    {
        // given
        var originalQuery = new IntervalsQuery.Builder().field("field").wildcard(new IntervalsWildcard.Builder().pattern("pattern").build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((IntervalsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testKnnQuery()
    {
        // given
        var originalQuery = new KnnQuery.Builder().field("field").vector(new float[]{}).k(41).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((KnnQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMatchBoolPrefixQuery()
    {
        // given
        var originalQuery = new MatchBoolPrefixQuery.Builder().field("field").query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MatchBoolPrefixQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMatchNoneQuery()
    {
        // given
        var originalQuery = new MatchNoneQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertNull(((MatchNoneQuery) boostedQuery._get()).boost());
    }

    @Test
    public void testMatchPhrasePrefixQuery()
    {
        // given
        var originalQuery = new MatchPhrasePrefixQuery.Builder().field("field").query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MatchPhrasePrefixQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMatchPhraseQuery()
    {
        // given
        var originalQuery = new MatchPhraseQuery.Builder().field("field").query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MatchPhraseQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMatchQuery()
    {
        // given
        var originalQuery = new MatchQuery.Builder().field("field").query(FieldValue.TRUE).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MatchQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMoreLikeThisQuery()
    {
        // given
        var originalQuery = new MoreLikeThisQuery.Builder().like(Collections.emptyList()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MoreLikeThisQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testMultiMatchQuery()
    {
        // given
        var originalQuery = new MultiMatchQuery.Builder().query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((MultiMatchQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testNestedQuery()
    {
        // given
        var originalQuery = new NestedQuery.Builder().path("path").query(new BoolQuery.Builder().build().toQuery()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((NestedQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testNeuralQuery()
    {
        // given
        var originalQuery = new NeuralQuery.Builder().field("field").queryText("queryText").queryImage("queryImage").k(19_999_999).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((NeuralQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testParentIdQuery()
    {
        // given
        var originalQuery = new ParentIdQuery.Builder().build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((ParentIdQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testPercolateQuery()
    {
        // given
        var originalQuery = new PercolateQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((PercolateQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testBoostPinnedQuery()
    {
        // given
        var originalQuery = new PinnedQuery.Builder().ids(Arrays.asList("1", "2")).organic(new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());
        // when

        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        // Should not boost if the query builder does not allow it
        assertNull(((PinnedQuery) boostedQuery._get()).boost());
    }

    @Test
    public void testPrefixQuery()
    {
        // given
        var originalQuery = new PrefixQuery.Builder().field("field").value("value").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((PrefixQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testRangeQuery()
    {
        // given
        var originalQuery = new RangeQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((RangeQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testRangeQueryBase()
    {
        // given
        var originalQuery = new RangeQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((RangeQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testRankFeatureQuery()
    {
        // given
        var originalQuery = new RankFeatureQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((RankFeatureQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testRegexpQuery()
    {
        // given
        var originalQuery = new RegexpQuery.Builder().field("field").value("value").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((RegexpQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testScriptQuery()
    {
        // given
        var originalQuery = new ScriptQuery.Builder().script(
                new Script.Builder().inline(
                        new InlineScript.Builder().options(Collections.emptyMap()).source("soruce").build())
                        .build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((ScriptQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testScriptScoreQuery()
    {
        // given
        var originalQuery = new ScriptScoreQuery.Builder()
                .query(new BoolQuery.Builder().build().toQuery())
                .script(new Script.Builder().inline(new InlineScript.Builder().source("source").options(Collections.emptyMap()).build()).build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((ScriptScoreQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSimpleQueryStringQuery()
    {
        // given
        var originalQuery = new SimpleQueryStringQuery.Builder().query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SimpleQueryStringQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanContainingQuery()
    {
        // given
        var originalQuery = new SpanContainingQuery.Builder()
                .big(new SpanQuery.Builder().spanTerm(
                        new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .little(new SpanQuery.Builder().spanTerm(
                        new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanContainingQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanFieldMaskingQuery()
    {
        // given
        var originalQuery = new SpanFieldMaskingQuery.Builder().field("field").query(new SpanQuery.Builder().spanTerm(
                new SpanTermQuery.Builder().field("field").value("value").build()).build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanFieldMaskingQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanFirstQuery()
    {
        // given
        var originalQuery = new SpanFirstQuery.Builder()
                .match(
                        new SpanQuery.Builder().spanTerm(
                                new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .end(1)
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanFirstQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanMultiTermQuery()
    {
        // given
        var originalQuery = new SpanMultiTermQuery.Builder().match(new BoolQuery.Builder().build().toQuery()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanMultiTermQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanNearQuery()
    {
        // given
        var originalQuery = new SpanNearQuery.Builder().clauses(Collections.emptyList()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanNearQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanNotQuery()
    {
        // given
        var originalQuery = new SpanNotQuery.Builder()
                .exclude(new SpanQuery.Builder().spanTerm(new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .include(new SpanQuery.Builder().spanTerm(new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanNotQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanOrQuery()
    {
        // given
        var originalQuery = new SpanOrQuery.Builder().clauses(Collections.emptyList()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanOrQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanTermQuery()
    {
        // given
        var originalQuery = new SpanTermQuery.Builder().field("field").value("value").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanTermQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testSpanWithinQuery()
    {
        // given
        var originalQuery = new SpanWithinQuery.Builder()
                .big(new SpanQuery.Builder().spanTerm(new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .little(new SpanQuery.Builder().spanTerm(new SpanTermQuery.Builder().field("field").value("value").build()).build())
                .build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((SpanWithinQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testTermQuery()
    {
        // given
        var originalQuery = new TermQuery.Builder().field("field").value(FieldValue.TRUE).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((TermQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testTermsQuery()
    {
        // given
        var originalQuery = new TermsQuery.Builder().field("field").terms(new TermsQueryField.Builder().value(Collections.emptyList()).build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((TermsQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testTermsSetQuery()
    {
        // given
        var originalQuery = new TermsSetQuery.Builder().terms(Collections.emptyList()).field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((TermsSetQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testTypeQuery()
    {
        // given
        var originalQuery = new TypeQuery.Builder().value("value").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((TypeQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testWildcardQuery()
    {
        // given
        var originalQuery = new WildcardQuery.Builder().field("field").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((WildcardQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testWrapperQuery()
    {
        // given
        var originalQuery = new WrapperQuery.Builder().query("query").build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((WrapperQuery) boostedQuery._get()).boost(), 0.1);
    }

    @Test
    public void testXyShapeQuery()
    {
        // given
        var originalQuery = new XyShapeQuery.Builder().field("field").xyShape(new XyShapeFieldQuery.Builder().build()).build();
        TESTED_SUB_CLASSES.add(originalQuery.getClass());

        // when
        Query boostedQuery = QueryBooster.boost(originalQuery.toQuery(), 2.0f);

        // then
        assertEquals(2.0f, ((XyShapeQuery) boostedQuery._get()).boost(), 0.1);
    }

}
