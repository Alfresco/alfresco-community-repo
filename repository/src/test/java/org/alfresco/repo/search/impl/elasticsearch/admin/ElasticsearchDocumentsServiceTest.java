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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ElasticsearchDocumentsService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchDocumentsServiceTest
{
    /**
     * The name of the ES index used in these tests.
     */
    public static final String TEST_INDEX_NAME = "test index name";
    public static final String UUID = "60630dbf-fb36-402d-8a4f-a999b2d44f23";

    /**
     * The class under test.
     */
    @InjectMocks
    private ElasticsearchDocumentsService elasticsearchDocumentsService;
    @Mock
    private ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;
    @Mock
    private OpenSearchClient openSearchClient;
    @Mock
    private Hit searchHit;
    @Mock
    private HitsMetadata searchHits;
    @Mock
    private SearchResponse searchResponse;

    @Captor
    private ArgumentCaptor<SearchRequest> searchRequestCaptor;

    @Before
    public void setUp()
    {
        when(elasticsearchHttpClientFactory.getElasticsearchClient()).thenReturn(openSearchClient);
        when(elasticsearchHttpClientFactory.getIndexName()).thenReturn(TEST_INDEX_NAME);
    }

    /**
     * Simulate 1 document indexed in Elasticsearch and check we can retrieve it.
     */
    @Test
    public void shouldGetDocumentsSuccess() throws Exception
    {
        Map<String, Object> sourceAsMap = new HashMap<>() {
            {
                put("CONTENT_INDEXING_LAST_UPDATE", 1000L);
                put("METADATA_INDEXING_LAST_UPDATE", 2000L);
            }
        };

        when(searchHit.id()).thenReturn(UUID);
        when(searchHit.source()).thenReturn(sourceAsMap);
        when(searchResponse.hits()).thenReturn(searchHits);
        when(searchHits.hits()).thenReturn(Collections.singletonList(searchHit));
        when(openSearchClient.search(searchRequestCaptor.capture(), eq(Map.class))).thenReturn(
                searchResponse);

        List<ElasticsearchDocument> documents = elasticsearchDocumentsService.getDocuments(
                Collections.singletonList(UUID), 10000);
        ElasticsearchDocument expectedDocument = documents.get(0);

        assertArrayEquals("Unexpected index name provided in request.", new String[]{TEST_INDEX_NAME},
                searchRequestCaptor.getValue().index().toArray());
        assertTrue("Unexpected source filter setting",
                searchRequestCaptor.getValue().source().fetch());
        assertEquals("Unexpected document's id retrieved from service.", UUID, expectedDocument.getId());
        assertEquals("Unexpected document's content indexing last update timestamp retrieved from service.", 1000L,
                expectedDocument.getContentIndexingLastUpdate());
        assertEquals("Unexpected document's metadata indexing last update timestamp retrieved from service.", 2000L,
                expectedDocument.getMetadataIndexingLastUpdate());
    }

    /**
     * Simulate 1 document indexed in Elasticsearch and check we can retrieve it with specified source fields.
     */
    @Test
    public void shouldGetDocumentsWithSpecifiedSourceFieldsSuccess() throws Exception
    {
        Map<String, Object> sourceAsMap = new HashMap<>() {
            {
                put("CONTENT_INDEXING_LAST_UPDATE", 1000L);
                put("METADATA_INDEXING_LAST_UPDATE", 2000L);
            }
        };

        when(searchHit.id()).thenReturn(UUID);
        when(searchHit.source()).thenReturn(sourceAsMap);
        when(searchResponse.hits()).thenReturn(searchHits);
        when(searchHits.hits()).thenReturn(Collections.singletonList(searchHit));
        when(openSearchClient.search(searchRequestCaptor.capture(), eq(Map.class))).thenReturn(
                searchResponse);

        List<ElasticsearchDocument> documents = elasticsearchDocumentsService.getDocuments(
                Collections.singletonList(UUID),
                Arrays.asList("CONTENT_INDEXING_LAST_UPDATE", "METADATA_INDEXING_LAST_UPDATE"), 10000);
        ElasticsearchDocument expectedDocument = documents.get(0);

        assertEquals("Unexpected source filter values",
                new String[]{"CONTENT_INDEXING_LAST_UPDATE", "METADATA_INDEXING_LAST_UPDATE"},
                searchRequestCaptor.getValue().source().filter().includes().toArray());
        assertEquals("Unexpected document's id retrieved from service.", UUID, expectedDocument.getId());

    }

    /**
     * Check that we get empty list when trying to get the documents in an ES cluster that's not running.
     */
    @Test
    public void shouldGetDocumentsFailure() throws Exception
    {
        when(openSearchClient.search(searchRequestCaptor.capture(), eq(Map.class)))
                .thenThrow(new IOException("Unable to access ES"));

        List<ElasticsearchDocument> documents = elasticsearchDocumentsService.getDocuments(
                Collections.singletonList(UUID), 10000);

        assertArrayEquals("Unexpected index name provided in request.", new String[]{TEST_INDEX_NAME},
                searchRequestCaptor.getValue().index().toArray());
        assertThat("Expected empty list to be returned when ES is not running.", documents, is(empty()));
    }

}
