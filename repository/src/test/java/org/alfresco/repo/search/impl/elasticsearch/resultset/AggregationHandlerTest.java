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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.FilterAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch.core.SearchResponse;

import org.alfresco.util.Pair;

/**
 * Unit tests for {@link AggregationHandler}.
 * <p>
 * Covers the core response-processing logic introduced together with the SITE facet feature: translating bucket keys via a post-processing map, skipping complementary aggregation entries in the main loop, and injecting complementary bucket counts as virtual buckets.
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregationHandlerTest
{
    private AggregationHandler handler;

    @Mock
    private SearchResponse<?> searchResponse;

    @Before
    public void setUp()
    {
        handler = new AggregationHandler();
    }

    @Test
    public void handle_withNullAggregations_returnsEmptyFacets()
    {
        when(searchResponse.aggregations()).thenReturn(null);

        Aggregation result = handler.handle(searchResponse, Map.of(), Map.of());

        assertTrue("facetQueries should be empty when response has no aggregations", result.facetQueries().isEmpty());
        assertTrue("fieldFacets should be empty when response has no aggregations", result.fieldFacets().isEmpty());
    }

    @Test
    public void handle_withEmptyAggregations_returnsEmptyFacets()
    {
        when(searchResponse.aggregations()).thenReturn(Map.of());

        Aggregation result = handler.handle(searchResponse, Map.of(), Map.of());

        assertTrue("facetQueries should be empty for empty aggregations map", result.facetQueries().isEmpty());
        assertTrue("fieldFacets should be empty for empty aggregations map", result.fieldFacets().isEmpty());
    }

    @Test
    public void handle_withFilterAggregation_populatesFacetQueries()
    {
        Aggregate filterAgg = buildFilterAggregate(42);
        when(searchResponse.aggregations()).thenReturn(Map.of("myFacetQuery", filterAgg));

        Aggregation result = handler.handle(searchResponse, Map.of(), Map.of());

        assertEquals("Filter agg should go into facetQueries", 1, result.facetQueries().size());
        assertEquals(42, result.facetQueries().get("myFacetQuery").intValue());
        assertTrue("Filter agg should not appear in fieldFacets", result.fieldFacets().isEmpty());
    }

    @Test
    public void handle_withFilterAggregation_decodesAggregationKey()
    {
        // The aggregation key stored in ES response uses URL-encoded form.
        // AggregationNameUtil.encode("my facet") = "my+facet" (space encoded as +).
        String encodedKey = AggregationNameUtil.encode("my facet");
        Aggregate filterAgg = buildFilterAggregate(7);
        when(searchResponse.aggregations()).thenReturn(Map.of(encodedKey, filterAgg));

        Aggregation result = handler.handle(searchResponse, Map.of(), Map.of());

        assertNotNull("Decoded key 'my facet' must be present in facetQueries", result.facetQueries().get("my facet"));
        assertEquals(7, result.facetQueries().get("my facet").intValue());
    }

    @Test
    public void handle_withStringTermsAggregation_populatesFieldFacets()
    {
        Aggregate termsAgg = buildStringTermsAggregate(List.of(
                new Pair<>("bucket-a", 10),
                new Pair<>("bucket-b", 3)));
        when(searchResponse.aggregations()).thenReturn(Map.of("myField", termsAgg));

        Aggregation result = handler.handle(searchResponse, Map.of(), Map.of());

        assertTrue("Terms agg should not appear in facetQueries", result.facetQueries().isEmpty());
        List<Pair<String, Integer>> buckets = result.fieldFacets().get("myField");
        assertNotNull("fieldFacets should contain the agg name key", buckets);
        assertEquals(2, buckets.size());
        assertEquals("bucket-a", buckets.get(0).getFirst());
        assertEquals(10, buckets.get(0).getSecond().intValue());
        assertEquals("bucket-b", buckets.get(1).getFirst());
        assertEquals(3, buckets.get(1).getSecond().intValue());
    }

    @Test
    public void handle_withBucketsTranslator_translatesBucketKey()
    {
        String uuid = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        String siteName = "my-site";
        Aggregate termsAgg = buildStringTermsAggregate(List.of(new Pair<>(uuid, 5)));
        when(searchResponse.aggregations()).thenReturn(Map.of("SITE", termsAgg));

        Map<String, String> bucketsTranslator = Map.of(uuid, siteName);

        Aggregation result = handler.handle(searchResponse, bucketsTranslator, Map.of());

        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull(siteBuckets);
        assertEquals(1, siteBuckets.size());
        assertEquals("Bucket key should be translated from UUID to site name", siteName, siteBuckets.get(0).getFirst());
    }

    @Test
    public void handle_withBucketsTranslator_unknownKeyIsReturnedUnchanged()
    {
        String unknownKey = "unknown-key";
        Aggregate termsAgg = buildStringTermsAggregate(List.of(new Pair<>(unknownKey, 2)));
        when(searchResponse.aggregations()).thenReturn(Map.of("SITE", termsAgg));

        Aggregation result = handler.handle(searchResponse, Map.of("other-key", "other-value"), Map.of());

        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull(siteBuckets);
        assertEquals("Unknown bucket key should remain unchanged", unknownKey, siteBuckets.get(0).getFirst());
    }

    @Test
    public void handle_complementaryAggregation_isSkippedInMainProcessing()
    {
        // The complementary agg is a filter aggregate that should NOT end up in facetQueries.
        Aggregate repositoryAgg = buildFilterAggregate(10);
        Map<String, Aggregate> aggregations = Map.of("SITE__REPOSITORY__", repositoryAgg);
        when(searchResponse.aggregations()).thenReturn(aggregations);

        Map<String, Pair<String, String>> complementaryBucketsTranslator = Map.of(
                "SITE__REPOSITORY__", new Pair<>("SITE", "_REPOSITORY_"));

        Aggregation result = handler.handle(searchResponse, Map.of(), complementaryBucketsTranslator);

        assertFalse("Complementary agg should not appear in facetQueries",
                result.facetQueries().containsKey("SITE__REPOSITORY__"));
    }

    @Test
    public void handle_complementaryAggregation_addsVirtualBucketToOwnerFacet()
    {
        String siteUuid = "site-uuid-1";
        String siteName = "marketing";

        // Main terms agg (SITE → primary hierarchy buckets)
        Aggregate termsAgg = buildStringTermsAggregate(List.of(new Pair<>(siteUuid, 5)));

        // Repository complementary filter agg
        Aggregate repositoryAgg = buildFilterAggregate(10);

        Map<String, Aggregate> aggregations = new LinkedHashMap<>();
        aggregations.put("SITE", termsAgg);
        aggregations.put("SITE__REPOSITORY__", repositoryAgg);
        when(searchResponse.aggregations()).thenReturn(aggregations);

        Map<String, String> bucketsTranslator = Map.of(siteUuid, siteName);
        Map<String, Pair<String, String>> complementaryBucketsTranslator = Map.of(
                "SITE__REPOSITORY__", new Pair<>("SITE", "_REPOSITORY_"));

        Aggregation result = handler.handle(searchResponse, bucketsTranslator, complementaryBucketsTranslator);

        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull("SITE field facet must exist", siteBuckets);
        assertEquals("Should have both site bucket and _REPOSITORY_ virtual bucket", 2, siteBuckets.size());

        assertTrue("Site bucket should be translated to name",
                siteBuckets.stream().anyMatch(p -> siteName.equals(p.getFirst()) && p.getSecond() == 5));
        assertTrue("_REPOSITORY_ virtual bucket should be present with correct count",
                siteBuckets.stream().anyMatch(p -> "_REPOSITORY_".equals(p.getFirst()) && p.getSecond() == 10));
    }

    @Test
    public void handle_complementaryAggregation_withZeroDocCount_doesNotAddVirtualBucket()
    {
        Aggregate termsAgg = buildStringTermsAggregate(List.of(new Pair<>("site-uuid", 5)));
        Aggregate repositoryAgg = buildFilterAggregate(0); // zero docs

        Map<String, Aggregate> aggregations = new LinkedHashMap<>();
        aggregations.put("SITE", termsAgg);
        aggregations.put("SITE__REPOSITORY__", repositoryAgg);
        when(searchResponse.aggregations()).thenReturn(aggregations);

        Map<String, Pair<String, String>> complementaryBucketsTranslator = Map.of(
                "SITE__REPOSITORY__", new Pair<>("SITE", "_REPOSITORY_"));

        Aggregation result = handler.handle(searchResponse, Map.of(), complementaryBucketsTranslator);

        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull(siteBuckets);
        assertEquals("_REPOSITORY_ bucket with zero docs should not be added", 1, siteBuckets.size());
        assertFalse("_REPOSITORY_ should not appear when doc count is 0",
                siteBuckets.stream().anyMatch(p -> "_REPOSITORY_".equals(p.getFirst())));
    }

    @Test
    public void handle_complementaryAggregation_whenOwnerFacetAbsent_createsNewFacetList()
    {
        // Only the complementary agg is in the response (e.g. terms agg returned zero buckets and was absent)
        Aggregate repositoryAgg = buildFilterAggregate(15);
        when(searchResponse.aggregations()).thenReturn(Map.of("SITE__REPOSITORY__", repositoryAgg));

        Map<String, Pair<String, String>> complementaryBucketsTranslator = Map.of(
                "SITE__REPOSITORY__", new Pair<>("SITE", "_REPOSITORY_"));

        Aggregation result = handler.handle(searchResponse, Map.of(), complementaryBucketsTranslator);

        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull("SITE facet list should be created even when main agg was absent", siteBuckets);
        assertEquals(1, siteBuckets.size());
        assertEquals("_REPOSITORY_", siteBuckets.get(0).getFirst());
        assertEquals(15, siteBuckets.get(0).getSecond().intValue());
    }

    @Test
    public void handle_withMixedAggregations_routesCorrectly()
    {
        // Filter agg → facetQueries; terms agg → fieldFacets; complementary filter agg → virtual bucket only
        Aggregate filterAgg = buildFilterAggregate(20);
        Aggregate termsAgg = buildStringTermsAggregate(List.of(new Pair<>("uuid-x", 8)));
        Aggregate repositoryAgg = buildFilterAggregate(4);

        Map<String, Aggregate> aggregations = new LinkedHashMap<>();
        aggregations.put("sizeQuery", filterAgg);
        aggregations.put("SITE", termsAgg);
        aggregations.put("SITE__REPOSITORY__", repositoryAgg);
        when(searchResponse.aggregations()).thenReturn(aggregations);

        Map<String, Pair<String, String>> complementaryBucketsTranslator = Map.of(
                "SITE__REPOSITORY__", new Pair<>("SITE", "_REPOSITORY_"));

        Aggregation result = handler.handle(searchResponse, Map.of(), complementaryBucketsTranslator);

        // Filter → facetQueries
        assertEquals(1, result.facetQueries().size());
        assertEquals(20, result.facetQueries().get("sizeQuery").intValue());

        // Terms → fieldFacets (with virtual _REPOSITORY_ added)
        List<Pair<String, Integer>> siteBuckets = result.fieldFacets().get("SITE");
        assertNotNull(siteBuckets);
        assertEquals(2, siteBuckets.size()); // uuid-x + _REPOSITORY_

        // Complementary agg itself must not appear as a standalone facet query
        assertNull(result.facetQueries().get("SITE__REPOSITORY__"));
        assertNull(result.fieldFacets().get("SITE__REPOSITORY__"));
    }

    private Aggregate buildFilterAggregate(long docCount)
    {
        return Aggregate.of(a -> a.filter(FilterAggregate.of(f -> f.docCount(docCount))));
    }

    private Aggregate buildStringTermsAggregate(List<Pair<String, Integer>> keyAndCount)
    {
        List<StringTermsBucket> buckets = keyAndCount.stream()
                .map(p -> StringTermsBucket.of(b -> b.key(p.getFirst()).docCount(p.getSecond())))
                .toList();
        StringTermsAggregate sterms = StringTermsAggregate.of(s -> s
                .buckets(bk -> bk.array(buckets))
                .sumOtherDocCount(0));
        return Aggregate.of(a -> a.sterms(sterms));
    }
}
