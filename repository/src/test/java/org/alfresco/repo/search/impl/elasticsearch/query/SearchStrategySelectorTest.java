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

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class SearchStrategySelectorTest
{
    private static final int MAX_RESULT_WINDOW = 10000;

    @Mock
    private SearchExecutionStrategy standardStrategy;
    @Mock
    private SearchExecutionStrategy scrollStrategy;
    @Mock
    private SearchParameters searchParameters;
    @Mock
    private Query queryWithPermissions;
    @Mock
    private ResultSet scrollResultSet;
    @Mock
    private ResultSet standardResultSet;

    private SearchStrategySelector selector;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        selector = new SearchStrategySelector(standardStrategy, scrollStrategy, MAX_RESULT_WINDOW);
    }

    @Test
    public void executeSearch_scrollStrategy_whenTotalExceedsWindow_noFacets() throws IOException
    {
        when(searchParameters.getSkipCount()).thenReturn(4000);
        when(searchParameters.getLimit()).thenReturn(7001);
        when(searchParameters.getFacetQueries()).thenReturn(List.of());
        when(scrollStrategy.executeSearch(eq(searchParameters), eq(queryWithPermissions))).thenReturn(scrollResultSet);

        ResultSet rs = selector.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertSame(scrollResultSet, rs);
            verify(scrollStrategy).executeSearch(eq(searchParameters), eq(queryWithPermissions));
            verifyNoInteractions(standardStrategy);
        }
        finally
        {
            rs.close();
        }
    }

    @Test
    public void executeSearch_standardStrategy_whenFacetsPresent_evenIfExceedsWindow() throws IOException
    {
        when(searchParameters.getSkipCount()).thenReturn(5000);
        when(searchParameters.getLimit()).thenReturn(6000);
        when(searchParameters.getFacetQueries()).thenReturn(List.of("f1"));
        when(standardStrategy.executeSearch(eq(searchParameters), eq(queryWithPermissions))).thenReturn(standardResultSet);

        ResultSet rs = selector.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertSame(standardResultSet, rs);
            verify(standardStrategy).executeSearch(eq(searchParameters), eq(queryWithPermissions));
            verifyNoInteractions(scrollStrategy);
        }
        finally
        {
            rs.close();
        }
    }

    @Test
    public void executeSearch_standardStrategy_whenWithinWindow_noFacets() throws IOException
    {
        when(searchParameters.getSkipCount()).thenReturn(100);
        when(searchParameters.getLimit()).thenReturn(900);
        when(searchParameters.getFacetQueries()).thenReturn(List.of());
        when(standardStrategy.executeSearch(eq(searchParameters), eq(queryWithPermissions))).thenReturn(standardResultSet);

        ResultSet rs = selector.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertSame(standardResultSet, rs);
            verify(standardStrategy).executeSearch(eq(searchParameters), eq(queryWithPermissions));
            verifyNoInteractions(scrollStrategy);
        }
        finally
        {
            rs.close();
        }
    }

    @Test
    public void executeSearch_scrollStrategy_whenBoundaryExceededByOne() throws IOException
    {
        when(searchParameters.getSkipCount()).thenReturn(6000);
        when(searchParameters.getLimit()).thenReturn(4001);
        when(searchParameters.getFacetQueries()).thenReturn(List.of());
        when(scrollStrategy.executeSearch(eq(searchParameters), eq(queryWithPermissions))).thenReturn(scrollResultSet);

        ResultSet rs = selector.executeSearch(searchParameters, queryWithPermissions);
        try
        {
            assertSame(scrollResultSet, rs);
            verify(scrollStrategy).executeSearch(eq(searchParameters), eq(queryWithPermissions));
            verifyNoInteractions(standardStrategy);
        }
        finally
        {
            rs.close();
        }
    }
}
