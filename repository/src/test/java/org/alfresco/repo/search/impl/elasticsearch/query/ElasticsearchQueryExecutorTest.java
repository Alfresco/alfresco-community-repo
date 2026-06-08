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

import static com.ibm.icu.impl.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.repo.search.impl.elasticsearch.permission.ElasticsearchPermissionQueryFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.filter.ElasticsearchFilterBuilder;
import org.alfresco.repo.search.impl.elasticsearch.query.language.LanguageQueryBuilder;
import org.alfresco.repo.search.impl.elasticsearch.resultset.ElasticsearchResultSet;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/** Unit tests for {@link ElasticsearchQueryExecutor}. */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.CloseResource")
public class ElasticsearchQueryExecutorTest
{
    ElasticsearchQueryExecutor elasticsearchQueryExecutor;
    @Mock
    ElasticsearchPermissionQueryFactory permissionQueryFactory;
    @Mock
    LanguageQueryBuilder languageQueryBuilder;
    @Mock
    LanguageQueryBuilder defaultLanguageQueryBuilder;
    @Mock
    ElasticsearchFilterBuilder elasticsearchFilterBuilder;
    @Mock
    SearchParameters searchParameters;
    @Mock
    ElasticsearchResultSet elasticsearchResultSet;
    @Mock
    SearchStrategySelector searchStrategySelector;
    @Mock
    Query baseQuery;
    @Mock
    Query filteredQuery;
    @Mock
    Query permissionedQuery;
    @Captor
    ArgumentCaptor<Query> queryCaptor;

    @Before
    public void setUp() throws IOException, ParseException
    {
        elasticsearchQueryExecutor = new ElasticsearchQueryExecutor(permissionQueryFactory, languageQueryBuilder, defaultLanguageQueryBuilder, elasticsearchFilterBuilder, false, searchStrategySelector);

        given(languageQueryBuilder.getQuery(searchParameters)).willReturn(baseQuery);
        given(elasticsearchFilterBuilder.getFTSQueryWithFilters(eq(baseQuery), eq(searchParameters), eq(defaultLanguageQueryBuilder))).willReturn(filteredQuery);
        given(permissionQueryFactory.getQueryWithPermissionFilter(eq(filteredQuery), anyBoolean())).willReturn(permissionedQuery);
        given(searchStrategySelector.executeSearch(eq(searchParameters), any(Query.class))).willReturn(elasticsearchResultSet);
    }

    @Test
    public void executeQuery_returnsResultSetAndPassesQuery() throws IOException
    {
        ResultSet actual = elasticsearchQueryExecutor.executeQuery(searchParameters);

        assertEquals("Unexpected result set received.", elasticsearchResultSet, actual);
        verify(searchStrategySelector).executeSearch(eq(searchParameters), queryCaptor.capture());
        assertNotNull("Query should have been passed to strategy selector", queryCaptor.getValue());
    }

    @Test
    public void executeQuery_unsupportedOperationException_isRethrown() throws ParseException
    {
        given(languageQueryBuilder.getQuery(searchParameters)).willThrow(new UnsupportedOperationException("not supported"));
        try
        {
            elasticsearchQueryExecutor.executeQuery(searchParameters);
            fail("Expected UnsupportedOperationException");
        }
        catch (UnsupportedOperationException e)
        {
            assertEquals("not supported", e.getMessage());
        }
    }

    @Test
    public void executeQuery_highlightError_retriesWithoutHighlight() throws ParseException, IOException
    {
        GeneralHighlightParameters highlightParams = mock(GeneralHighlightParameters.class);
        given(searchParameters.getHighlight()).willReturn(highlightParams, (GeneralHighlightParameters) null);

        OpenSearchException ose = mock(OpenSearchException.class, RETURNS_DEEP_STUBS);
        ErrorCause cause = mock(ErrorCause.class);
        given(cause.reason()).willReturn("query failed due to index.highlight.max_analyzed_offset limit");
        given(ose.error().rootCause()).willReturn(List.of(cause));

        given(languageQueryBuilder.getQuery(searchParameters)).willThrow(ose).willReturn(baseQuery);

        ResultSet rs = elasticsearchQueryExecutor.executeQuery(searchParameters);

        assertEquals(elasticsearchResultSet, rs);
        verify(searchParameters).setHighlight(null);
        verify(languageQueryBuilder, times(2)).getQuery(searchParameters);
        verify(searchStrategySelector).executeSearch(eq(searchParameters), eq(permissionedQuery));
    }

    @Test
    public void executeQuery_openSearchNonHighlight_rethrows() throws ParseException
    {
        GeneralHighlightParameters highlightParams = mock(GeneralHighlightParameters.class);
        given(searchParameters.getHighlight()).willReturn(highlightParams);

        OpenSearchException ose = mock(OpenSearchException.class, Mockito.RETURNS_DEEP_STUBS);
        ErrorCause cause = mock(ErrorCause.class);
        given(cause.reason()).willReturn("some other shard failure");
        given(ose.error().rootCause()).willReturn(List.of(cause));
        given(languageQueryBuilder.getQuery(searchParameters)).willThrow(ose);

        try
        {
            elasticsearchQueryExecutor.executeQuery(searchParameters);
            fail("Expected OpenSearchException to be rethrown");
        }
        catch (OpenSearchException e)
        {
            assertEquals(ose, e);
        }
    }

}
