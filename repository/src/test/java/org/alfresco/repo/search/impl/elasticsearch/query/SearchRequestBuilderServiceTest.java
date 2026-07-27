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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.sort.ElasticsearchSortBuilder;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;

public class SearchRequestBuilderServiceTest
{

    @Mock
    private ElasticsearchHttpClientFactory httpClientFactory;
    @Mock
    private LanguageQueryBuilder languageQueryBuilder;
    @Mock
    private ElasticsearchSortBuilder elasticsearchSortBuilder;
    @Mock
    private ElasticsearchAggregationBuilder elasticsearchAggregationBuilder;
    @Mock
    private ElasticsearchHighlightBuilder elasticsearchHighlightBuilder;

    private SearchRequestBuilderService service;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new SearchRequestBuilderService(
                languageQueryBuilder,
                httpClientFactory,
                elasticsearchSortBuilder,
                elasticsearchAggregationBuilder,
                elasticsearchHighlightBuilder);
    }

    @Test
    public void testGetElasticIndex_workspace()
    {
        StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        when(httpClientFactory.getIndexName()).thenReturn("workspace-index");
        String index = service.getElasticIndex(List.of(store));
        assertEquals("workspace-index", index);
    }

    @Test
    public void testGetElasticIndex_unsupportedProtocol()
    {
        StoreRef store = new StoreRef("unsupported", "SpacesStore");
        try
        {
            service.getElasticIndex(List.of(store));
            fail("Expected RuntimeException");
        }
        catch (RuntimeException e)
        {
            assertTrue(e.getMessage().contains("is not supported"));
        }
    }

    @Test
    public void testBuildSearchAfterRequest_pinsFromZeroSetsPitAndOmitsIndex()
    {
        stubSearchAfterCommon();

        SearchRequest request = service.buildSearchAfterRequest(
                new SearchParameters(), matchAll(), 25, new SearchAfterContext("pit-123", "1m", List.of()));

        assertEquals(Integer.valueOf(0), request.from());
        assertEquals(Integer.valueOf(25), request.size());
        assertNotNull("PIT must be set for a search_after request", request.pit());
        assertEquals("pit-123", request.pit().id());
        assertEquals("1m", request.pit().keepAlive());
        assertTrue("index must not be set on the body, it is carried by the PIT", request.index().isEmpty());
    }

    @Test
    public void testBuildSearchAfterRequest_appliesTypedSearchAfterValues()
    {
        stubSearchAfterCommon();
        // A keyword value must stay a string and a numeric tiebreaker must stay numeric, or OpenSearch rejects search_after.
        List<FieldValue> searchAfter = List.of(FieldValue.of("text/plain"), FieldValue.of(137L));

        SearchRequest request = service.buildSearchAfterRequest(
                new SearchParameters(), matchAll(), 10, new SearchAfterContext("pit-1", "1m", searchAfter));

        assertEquals(2, request.searchAfterVals().size());
        assertTrue(request.searchAfterVals().get(0).isString());
        assertEquals("text/plain", request.searchAfterVals().get(0).stringValue());
        assertTrue(request.searchAfterVals().get(1).isLong());
        assertEquals(137L, request.searchAfterVals().get(1).longValue());
    }

    @Test
    public void testBuildSearchAfterRequest_usesTiebreakerAwareSort()
    {
        stubSearchAfterCommon();

        SearchRequest request = service.buildSearchAfterRequest(
                new SearchParameters(), matchAll(), 10, new SearchAfterContext("pit-1", "1m", List.of()));

        assertEquals(1, request.sort().size());
        assertTrue(request.sort().get(0).isField());
        assertEquals("_shard_doc", request.sort().get(0).field().field());
        // The search_after path must use the tiebreaker-aware sort, never the plain one.
        verify(elasticsearchSortBuilder).getSortBuildersWithTiebreaker(any(SearchParameters.class));
        verify(elasticsearchSortBuilder, never()).getSortBuilders(any(SearchParameters.class));
    }

    private void stubSearchAfterCommon()
    {
        when(elasticsearchAggregationBuilder.filterAggregation(any(SearchParameters.class), any())).thenReturn(Map.of());
        when(elasticsearchAggregationBuilder.termsAggregations(any(SearchParameters.class), any())).thenReturn(Stream.empty());
        SortOptions shardDoc = new SortOptions.Builder()
                .field(new FieldSort.Builder().field("_shard_doc").order(SortOrder.Asc).build())
                .build();
        when(elasticsearchSortBuilder.getSortBuildersWithTiebreaker(any(SearchParameters.class))).thenReturn(List.of(shardDoc));
    }

    private static Query matchAll()
    {
        return MatchAllQuery.of(m -> m)._toQuery();
    }
}
