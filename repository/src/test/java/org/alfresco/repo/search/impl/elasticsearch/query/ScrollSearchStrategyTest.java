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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ShardStatistics;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSet;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSetBuilder;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class ScrollSearchStrategyTest
{
    @Mock
    private SearchRequestBuilderService requestBuilderService;
    @Mock
    private ElasticsearchHttpClientFactory httpClientFactory;
    @Mock
    private ElasticsearchResultSetBuilder resultSetBuilder;
    @Mock
    private OpenSearchClient client;
    @Mock
    private SearchParameters searchParameters;
    @Mock
    private Query queryWithPermissions;

    private ScrollSearchStrategy strategy;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        when(httpClientFactory.getElasticsearchClient()).thenReturn(client);
        when(requestBuilderService.getElasticIndex(any())).thenReturn("test-index");
        strategy = new ScrollSearchStrategy(
                requestBuilderService,
                httpClientFactory,
                resultSetBuilder,
                "1m",
                3);
    }

    @Test
    public void search_singlePage_returnsHits() throws Exception
    {
        when(searchParameters.getStores()).thenReturn(new ArrayList<>());
        when(searchParameters.getSkipCount()).thenReturn(0);
        when(searchParameters.getLimit()).thenReturn(2);

        SearchRequest built = new SearchRequest.Builder().build();
        when(requestBuilderService.buildSearchRequest(any(), any(), anyInt(), any(Time.class), anyString()))
                .thenReturn(built);

        List<Hit<Object>> hits = List.of(
                new Hit.Builder<>().id("1").build(),
                new Hit.Builder<>().id("2").build());

        SearchResponse<Object> response = new SearchResponse.Builder<Object>()
                .took(1).timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(hb -> hb.total(new TotalHits.Builder().value(2).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0).hits(hits))
                .scrollId(null)
                .build();

        when(client.search(any(SearchRequest.class), eq(Object.class))).thenReturn(response);

        ElasticsearchResultSet rsMock = mock(ElasticsearchResultSet.class);
        when(resultSetBuilder.build(eq(searchParameters), anyList(), anyLong(), anyLong())).thenReturn(rsMock);

        ResultSet resultSet = strategy.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertNotNull(resultSet);
            verify(resultSetBuilder).build(eq(searchParameters), anyList(), eq(2L), anyLong());
            verify(client, never()).scroll(any(ScrollRequest.class), eq(Object.class));
        }
        finally
        {
            resultSet.close();
        }
    }

    @Test
    public void search_scrolls_whenResultsExceedBatchSize() throws Exception
    {
        when(searchParameters.getStores()).thenReturn(new ArrayList<>());
        when(searchParameters.getSkipCount()).thenReturn(0);
        when(searchParameters.getLimit()).thenReturn(3);

        when(requestBuilderService.buildSearchRequest(any(), any(), anyInt(), any(Time.class), anyString()))
                .thenReturn(new SearchRequest.Builder().build());

        List<Hit<Object>> batch1 = List.of(new Hit.Builder<>().id("1").build());
        List<Hit<Object>> batch2 = List.of(new Hit.Builder<>().id("2").build(), new Hit.Builder<>().id("3").build());

        SearchResponse<Object> first = new SearchResponse.Builder<Object>()
                .took(1).timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(hb -> hb.total(new TotalHits.Builder().value(3).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0).hits(batch1))
                .scrollId("scroll-1")
                .build();

        ScrollResponse<Object> second = new ScrollResponse.Builder<Object>()
                .took(1L).timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(hb -> hb.total(new TotalHits.Builder().value(3).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0).hits(batch2))
                .scrollId("scroll-2")
                .build();

        when(client.search(any(SearchRequest.class), eq(Object.class))).thenReturn(first);
        when(client.scroll(any(ScrollRequest.class), eq(Object.class))).thenReturn(second);

        ElasticsearchResultSet rsMock = mock(ElasticsearchResultSet.class);
        when(resultSetBuilder.build(eq(searchParameters), anyList(), eq(3L), anyLong())).thenReturn(rsMock);

        ResultSet resultSet = strategy.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertNotNull(resultSet);
            verify(client).scroll(any(ScrollRequest.class), eq(Object.class));
            verify(resultSetBuilder).build(eq(searchParameters), anyList(), eq(3L), anyLong());
        }
        finally
        {
            resultSet.close();
        }
    }

    @Test
    public void search_skipCount_appliesCorrectly() throws Exception
    {
        // Verify first two hits are skipped and only the third is collected.
        when(searchParameters.getStores()).thenReturn(new ArrayList<>());
        when(searchParameters.getSkipCount()).thenReturn(2);
        when(searchParameters.getLimit()).thenReturn(1);

        when(requestBuilderService.buildSearchRequest(any(), any(), anyInt(), any(Time.class), anyString()))
                .thenReturn(new SearchRequest.Builder().build());

        List<Hit<Object>> hits = List.of(
                new Hit.Builder<>().id("skip1").build(),
                new Hit.Builder<>().id("skip2").build(),
                new Hit.Builder<>().id("collect1").build());

        SearchResponse<Object> response = new SearchResponse.Builder<Object>()
                .took(1).timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(hb -> hb.total(new TotalHits.Builder().value(3).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0).hits(hits))
                .scrollId("dummy-scroll-id")
                .build();

        when(client.search(any(SearchRequest.class), eq(Object.class))).thenReturn(response);

        ArgumentCaptor<List> captured = ArgumentCaptor.forClass(List.class);
        ElasticsearchResultSet rsMock = mock(ElasticsearchResultSet.class);
        when(resultSetBuilder.build(eq(searchParameters), captured.capture(), eq(3L), anyLong())).thenReturn(rsMock);

        ResultSet resultSet = strategy.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            List<?> collected = captured.getValue();
            assertEquals(1, collected.size());
            assertEquals("collect1", ((Hit<?>) collected.get(0)).id());
            // No additional scroll request needed (limit reached in first processed batch)
            verify(client, never()).scroll(any(ScrollRequest.class), eq(Object.class));
        }
        finally
        {
            resultSet.close();
        }
    }

    @Test
    public void search_noHits_returnsEmptyResultSet() throws Exception
    {
        when(searchParameters.getStores()).thenReturn(new ArrayList<>());
        when(searchParameters.getSkipCount()).thenReturn(0);
        when(searchParameters.getLimit()).thenReturn(10);
        SearchRequest searchRequest = new SearchRequest.Builder().build();
        when(requestBuilderService.buildSearchRequest(any(), any(), anyInt(), any(Time.class), anyString()))
                .thenReturn(searchRequest);

        List<Hit<Object>> hits = List.of();
        SearchResponse<Object> searchResponse = new SearchResponse.Builder<Object>()
                .took(1)
                .timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(h -> h
                        .total(new TotalHits.Builder().value(0).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0)
                        .hits(hits))
                .scrollId(null)
                .build();
        when(client.search(any(SearchRequest.class), eq(Object.class))).thenReturn(searchResponse);

        ElasticsearchResultSet mockResultSet = mock(ElasticsearchResultSet.class);
        when(resultSetBuilder.build(eq(searchParameters), anyList(), anyLong(), anyLong())).thenReturn(mockResultSet);

        ResultSet resultSet = strategy.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertNotNull(resultSet);
            verify(resultSetBuilder).build(eq(searchParameters), anyList(), eq(0L), anyLong());
            verify(client, never()).scroll(any(ScrollRequest.class), eq(Object.class));
        }
        finally
        {
            resultSet.close();
        }
    }
}
