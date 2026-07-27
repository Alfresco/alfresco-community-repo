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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.ShardStatistics;
import org.opensearch.client.opensearch._types.query_dsl.Query;
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

/**
 * Unit tests for the deep-pagination execution flow for SearchAfterSearchStrategy, the Point-In-Time (PIT) lifecycle (open on the first page, reuse on subsequent pages, close on the last page or on failure), the pagination token emitted for the next page, and PIT id rotation carried forward from each response.
 */
public class SearchAfterSearchStrategyTest
{
    private static final String INDEX = "test-index";
    private static final String KEEP_ALIVE = "1m";
    private static final int MAX_RESULT_WINDOW = 10000;
    private static final String OPENED_PIT = "opened-pit";

    @Mock
    private SearchRequestBuilderService requestBuilderService;
    @Mock
    private ElasticsearchHttpClientFactory httpClientFactory;
    @Mock
    private ElasticsearchResultSetBuilder resultSetBuilder;
    @Mock
    private ElasticsearchPitService pitService;
    @Mock
    private OpenSearchClient client;
    @Mock
    private SearchParameters searchParameters;
    @Mock
    private Query queryWithPermissions;
    @Mock
    private ElasticsearchResultSet resultSet;

    private SearchAfterSearchStrategy strategy;

    @Before
    public void setUp() throws IOException
    {
        MockitoAnnotations.openMocks(this);
        when(httpClientFactory.getElasticsearchClient()).thenReturn(client);
        when(requestBuilderService.getElasticIndex(any())).thenReturn(INDEX);
        when(requestBuilderService.buildSearchAfterRequest(any(), any(), anyInt(), any(SearchAfterContext.class)))
                .thenReturn(new SearchRequest.Builder().build());
        when(pitService.open(anyString(), anyString())).thenReturn(OPENED_PIT);
        when(resultSetBuilder.build(any(SearchParameters.class), any(SearchResponse.class), nullable(String.class)))
                .thenReturn(resultSet);
        when(searchParameters.getStores()).thenReturn(new ArrayList<>());
        strategy = new SearchAfterSearchStrategy(
                requestBuilderService, httpClientFactory, resultSetBuilder, pitService, KEEP_ALIVE, MAX_RESULT_WINDOW);
    }

    @Test
    public void firstPageOpensPitAndEmitsCursorWhenMorePagesRemain() throws IOException
    {
        firstPageRequestingSize(2);
        // A full page (hits == size) means there may be more pages, so the PIT stays open and a cursor is emitted.
        stubSearchReturns(responseWith(List.of(
                hit("1", FieldValue.of("a"), FieldValue.of(10L)),
                hit("2", FieldValue.of("b"), FieldValue.of(11L))), null));

        ResultSet rs = strategy.executeSearch(searchParameters, queryWithPermissions);

        assertSame(resultSet, rs);
        verify(pitService).open(INDEX, KEEP_ALIVE);
        verify(pitService, never()).close(anyString());

        SearchAfterCursor.Decoded next = SearchAfterCursor.decode(capturedNextCursor());
        assertEquals("cursor must carry the session PIT forward", OPENED_PIT, next.pitId());
        assertEquals("cursor must carry the last hit's typed sort values", 2, next.sort().size());
        assertEquals("b", next.sort().get(0).stringValue());
        assertEquals(11L, next.sort().get(1).longValue());
    }

    @Test
    public void lastPageClosesTheLivePitAndEmitsNoCursor() throws IOException
    {
        firstPageRequestingSize(5);
        // Fewer hits than requested marks the last page. The response also rotates the PIT id, so the close must
        // target the rotated (live) id rather than the one originally opened, and no further cursor is produced.
        stubSearchReturns(responseWith(List.of(hit("1", FieldValue.of("a"))), "rotated-pit"));

        strategy.executeSearch(searchParameters, queryWithPermissions);

        verify(pitService).close("rotated-pit");
        verify(pitService, never()).close(OPENED_PIT);
        assertNull("the last page must not emit a cursor", capturedNextCursor());
    }

    @Test
    public void subsequentPageReusesCursorPitWithoutOpeningANewOne() throws IOException
    {
        String existingCursor = SearchAfterCursor.encode("session-pit", List.of(FieldValue.of("x")));
        when(searchParameters.getSearchAfter()).thenReturn(existingCursor);
        when(searchParameters.getLimit()).thenReturn(2);
        stubSearchReturns(responseWith(List.of(
                hit("3", FieldValue.of("y")),
                hit("4", FieldValue.of("z"))), null));

        strategy.executeSearch(searchParameters, queryWithPermissions);

        // A cursor with a PIT id means we are mid-session: no new PIT must be opened.
        verify(pitService, never()).open(anyString(), anyString());

        ArgumentCaptor<SearchAfterContext> context = ArgumentCaptor.forClass(SearchAfterContext.class);
        verify(requestBuilderService).buildSearchAfterRequest(any(), any(), anyInt(), context.capture());
        assertEquals("the existing PIT must be reused", "session-pit", context.getValue().getPitId());
        assertEquals("the previous page's sort values must be applied as search_after", 1,
                context.getValue().getSearchAfter().size());
        assertEquals("x", context.getValue().getSearchAfter().get(0).stringValue());

        assertEquals("session-pit", SearchAfterCursor.decode(capturedNextCursor()).pitId());
    }

    @Test
    public void searchFailureClosesOpenedPitAndWrapsException() throws IOException
    {
        firstPageRequestingSize(2);
        when(client.search(any(SearchRequest.class), eq(Object.class))).thenThrow(new IOException("boom"));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> strategy.executeSearch(searchParameters, queryWithPermissions));

        assertEquals(IOException.class, thrown.getCause().getClass());
        // The PIT opened for this page must be released when the request fails.
        verify(pitService).close(OPENED_PIT);
    }

    private void firstPageRequestingSize(int size)
    {
        when(searchParameters.getSearchAfter()).thenReturn("");
        when(searchParameters.getLimit()).thenReturn(size);
    }

    private void stubSearchReturns(SearchResponse<Object> response) throws IOException
    {
        when(client.search(any(SearchRequest.class), eq(Object.class))).thenReturn(response);
    }

    private String capturedNextCursor()
    {
        ArgumentCaptor<String> cursor = ArgumentCaptor.forClass(String.class);
        verify(resultSetBuilder).build(any(SearchParameters.class), any(SearchResponse.class), cursor.capture());
        return cursor.getValue();
    }

    private static Hit<Object> hit(String id, FieldValue... sort)
    {
        return new Hit.Builder<>().index(INDEX).id(id).sortVals(List.of(sort)).build();
    }

    private static SearchResponse<Object> responseWith(List<Hit<Object>> hits, String pitId)
    {
        return new SearchResponse.Builder<Object>()
                .took(1).timedOut(false)
                .shards(new ShardStatistics.Builder().total(1).successful(1).skipped(0).failed(0).build())
                .hits(hb -> hb.total(new TotalHits.Builder().value(hits.size()).relation(TotalHitsRelation.Eq).build())
                        .maxScore(1.0).hits(hits))
                .pitId(pitId)
                .build();
    }
}
