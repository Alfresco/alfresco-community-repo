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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHits;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;

@SuppressWarnings("PMD.UnusedPrivateMethod")
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchResultSetBuilderTest
{
    private static final String TEST_NODE_ID_1 = "test-node-id-1";
    private static final String TEST_NODE_ID_2 = "test-node-id-2";
    private static final String TEST_NODE_ID_3 = "test-node-id-3";
    private static final float TEST_SCORE_1 = 1.5f;
    private static final float TEST_SCORE_2 = 2.5f;
    private static final float TEST_SCORE_3 = 0.5f;
    private static final long TEST_QUERY_TIME = 100L;
    private static final long TEST_TOTAL_HITS = 3L;
    private static final int TEST_SKIP_COUNT = 0;

    private ElasticsearchResultSetBuilder builder;

    @Mock
    private NodeService nodeService;

    @Mock
    private NodeDAO nodeDAO;

    @Mock
    private HighlightsHandler highlightsHandler;

    @Mock
    private AggregationHandler aggregationHandler;

    @Mock
    private SearchResponse<Object> searchResponse;

    @Mock
    private SearchParameters searchParameters;

    @Mock
    private HitsMetadata<Object> hitsMetadata;

    @Mock
    private TotalHits totalHits;

    private NodeRef nodeRef1;
    private NodeRef nodeRef2;
    private NodeRef nodeRef3;

    @Before
    public void setUp()
    {
        builder = new ElasticsearchResultSetBuilder(nodeService, nodeDAO, highlightsHandler, aggregationHandler);

        nodeRef1 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TEST_NODE_ID_1);
        nodeRef2 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TEST_NODE_ID_2);
        nodeRef3 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, TEST_NODE_ID_3);

        // Default search parameters setup
        when(searchParameters.getSkipCount()).thenReturn(TEST_SKIP_COUNT);
        when(searchParameters.isBulkFetchEnabled()).thenReturn(false);
    }

    @Test
    public void testBuild_withValidSearchResponse_returnsResultSet()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(
                createHit(TEST_NODE_ID_1, TEST_SCORE_1),
                createHit(TEST_NODE_ID_2, TEST_SCORE_2),
                createHit(TEST_NODE_ID_3, TEST_SCORE_3));
        setupSearchResponse(hits);
        setupNodeExistence(true, true, true);
        setupAggregationAndHighlights();

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        assertEquals("Result set length should match existing nodes", 3, result.length());
        assertEquals("Total hits should match", TEST_TOTAL_HITS, result.getNumberFound());
        verifyNodeRefsAndScores(result,
                new Pair<>(nodeRef1, TEST_SCORE_1),
                new Pair<>(nodeRef2, TEST_SCORE_2),
                new Pair<>(nodeRef3, TEST_SCORE_3));
    }

    @Test
    public void testBuild_withNonExistentNodes_filtersOutNonExistentNodes()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(
                createHit(TEST_NODE_ID_1, TEST_SCORE_1),
                createHit(TEST_NODE_ID_2, TEST_SCORE_2),
                createHit(TEST_NODE_ID_3, TEST_SCORE_3));
        setupSearchResponse(hits);
        setupNodeExistence(true, false, true); // Node 2 doesn't exist
        setupAggregationAndHighlights();

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertEquals("Result set should only include existing nodes", 2, result.length());
        verifyNodeRefsAndScores(result,
                new Pair<>(nodeRef1, TEST_SCORE_1),
                new Pair<>(nodeRef3, TEST_SCORE_3));
    }

    @Test
    public void testBuild_withEmptyHits_returnsEmptyResultSet()
    {
        // Given
        setupSearchResponse(Collections.emptyList());
        setupAggregationAndHighlights();

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        assertEquals("Result set should be empty", 0, result.length());
        assertEquals("Total hits should be from search response", TEST_TOTAL_HITS, result.getNumberFound());
        verifyNoInteractions(nodeService);
    }

    @Test
    public void testBuild_withBulkFetchEnabled_cachesNodes()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(
                createHit(TEST_NODE_ID_1, TEST_SCORE_1),
                createHit(TEST_NODE_ID_2, TEST_SCORE_2));
        setupSearchResponse(hits);
        setupNodeExistence(true, true);
        setupAggregationAndHighlights();
        when(searchParameters.isBulkFetchEnabled()).thenReturn(true);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        InOrder inOrder = Mockito.inOrder(nodeDAO, nodeService);
        inOrder.verify(nodeDAO).setCheckNodeConsistency();
        inOrder.verify(nodeDAO).cacheNodes(anyList());
        inOrder.verify(nodeService).exists(nodeRef1);
        inOrder.verify(nodeService).exists(nodeRef2);
    }

    @Test
    public void testBuild_withBulkFetchDisabled_doesNotCacheNodes()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(createHit(TEST_NODE_ID_1, TEST_SCORE_1));
        setupSearchResponse(hits);
        setupNodeExistence(true);
        setupAggregationAndHighlights();
        when(searchParameters.isBulkFetchEnabled()).thenReturn(false);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        verify(nodeDAO, never()).setCheckNodeConsistency();
        verify(nodeDAO, never()).cacheNodes(anyList());
    }

    @Test
    public void testBuild_withAggregations_includesFacetsInResultSet()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(createHit(TEST_NODE_ID_1, TEST_SCORE_1));
        setupSearchResponse(hits);
        setupNodeExistence(true);

        Map<String, Integer> facetQueries = Map.of("query1", 10, "query2", 20);
        Map<String, List<Pair<String, Integer>>> fieldFacets = Map.of(
                "field1", List.of(new Pair<>("value1", 5)));
        Aggregation aggregation = new Aggregation(facetQueries, fieldFacets);
        when(aggregationHandler.handle(searchResponse)).thenReturn(aggregation);

        Map<NodeRef, List<Pair<String, List<String>>>> highlights = Collections.emptyMap();
        when(highlightsHandler.handle(searchParameters, searchResponse)).thenReturn(highlights);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        assertEquals("Facet queries should be included", 10, result.getFacetQueries().get("query1").intValue());
        assertEquals("Field facets should be included", 1, result.getFieldFacet("field1").size());
    }

    @Test
    public void testBuild_withHighlights_includesHighlightsInResultSet()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(createHit(TEST_NODE_ID_1, TEST_SCORE_1));
        setupSearchResponse(hits);
        setupNodeExistence(true);

        Map<NodeRef, List<Pair<String, List<String>>>> highlights = Map.of(
                nodeRef1, List.of(new Pair<>("field1", List.of("highlight1"))));
        when(highlightsHandler.handle(searchParameters, searchResponse)).thenReturn(highlights);

        Aggregation aggregation = new Aggregation(Collections.emptyMap(), Collections.emptyMap());
        when(aggregationHandler.handle(searchResponse)).thenReturn(aggregation);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, searchResponse);

        // Then
        assertNotNull("Result set should not be null", result);
        var allHighlights = result.getHighlighting();
        assertNotNull("Highlights should be present", allHighlights);
        assertTrue("Highlights should contain node", allHighlights.containsKey(nodeRef1));
        var nodeHighlights = allHighlights.get(nodeRef1);
        assertNotNull("Node highlights should not be null", nodeHighlights);
        assertTrue("Highlight field should be present", nodeHighlights.stream()
                .anyMatch(pair -> "field1".equals(pair.getFirst())));
    }

    @Test
    public void testBuildWithHitsList_doesNotIncludeAggregationsOrHighlights()
    {
        // Given
        List<Hit<Object>> hits = createHitsList(createHit(TEST_NODE_ID_1, TEST_SCORE_1));
        setupNodeExistence(true);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, hits, TEST_TOTAL_HITS, TEST_QUERY_TIME);

        // Then
        assertNotNull("Result set should not be null", result);
        assertTrue("Facet queries should be empty", result.getFacetQueries().isEmpty());
        assertTrue("Field facets should be empty", result.getFieldFacets().isEmpty());
        verifyNoInteractions(aggregationHandler);
        verifyNoInteractions(highlightsHandler);
    }

    @Test
    public void testBuildWithHitsList_respectsSkipCount()
    {
        // Given
        int expectedSkipCount = 10;
        when(searchParameters.getSkipCount()).thenReturn(expectedSkipCount);
        List<Hit<Object>> hits = createHitsList(createHit(TEST_NODE_ID_1, TEST_SCORE_1));
        setupNodeExistence(true);

        // When
        ElasticsearchResultSet result = builder.build(searchParameters, hits, TEST_TOTAL_HITS, TEST_QUERY_TIME);

        // Then
        assertNotNull("Result set should not be null", result);
        assertEquals("Start value should match skip count", expectedSkipCount, result.getStart());
    }

    @SuppressWarnings("unchecked")
    private Hit<Object> createHit(String id, float score)
    {
        Hit<Object> hit = (Hit<Object>) org.mockito.Mockito.mock(Hit.class);
        when(hit.id()).thenReturn(id);
        when(hit.score()).thenReturn((double) score);
        return hit;
    }

    @SafeVarargs
    private List<Hit<Object>> createHitsList(Hit<Object>... hits)
    {
        List<Hit<Object>> hitsList = new ArrayList<>();
        Collections.addAll(hitsList, hits);
        return hitsList;
    }

    private void setupSearchResponse(List<Hit<Object>> hits)
    {
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(hits);
        when(searchResponse.took()).thenReturn(TEST_QUERY_TIME);
        setupTotalHits();
    }

    private void setupTotalHits()
    {
        when(hitsMetadata.total()).thenReturn(totalHits);
        when(totalHits.value()).thenReturn(TEST_TOTAL_HITS);
    }

    private void setupNodeExistence(boolean... exists)
    {
        if (exists.length >= 1)
        {
            when(nodeService.exists(nodeRef1)).thenReturn(exists[0]);
        }
        if (exists.length >= 2)
        {
            when(nodeService.exists(nodeRef2)).thenReturn(exists[1]);
        }
        if (exists.length >= 3)
        {
            when(nodeService.exists(nodeRef3)).thenReturn(exists[2]);
        }
    }

    private void setupAggregationAndHighlights()
    {
        Aggregation emptyAggregation = new Aggregation(Collections.emptyMap(), Collections.emptyMap());
        when(aggregationHandler.handle(searchResponse)).thenReturn(emptyAggregation);
        when(highlightsHandler.handle(searchParameters, searchResponse)).thenReturn(Collections.emptyMap());
    }

    @SafeVarargs
    private void verifyNodeRefsAndScores(ElasticsearchResultSet result, Pair<NodeRef, Float>... expectedPairs)
    {
        assertEquals("Result set should have expected number of entries", expectedPairs.length, result.length());

        for (int i = 0; i < expectedPairs.length; i++)
        {
            NodeRef expectedNodeRef = expectedPairs[i].getFirst();
            Float expectedScore = expectedPairs[i].getSecond();

            assertEquals("NodeRef at index " + i + " should match", expectedNodeRef, result.getNodeRef(i));
            assertEquals("Score at index " + i + " should match", expectedScore, result.getScore(i), 0.001f);
        }
    }
}
