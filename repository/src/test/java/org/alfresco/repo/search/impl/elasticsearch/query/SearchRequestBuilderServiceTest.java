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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.aggregation.ElasticsearchAggregationBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.highlight.ElasticsearchHighlightBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.sort.ElasticsearchSortBuilder;
import org.alfresco.service.cmr.repository.StoreRef;

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
}
