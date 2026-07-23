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

import java.io.IOException;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

public class SearchStrategySelector implements SearchStrategy
{

    private final SearchStrategy standardStrategy;
    private final SearchStrategy scrollStrategy;
    private final SearchStrategy searchAfterStrategy;
    private final int maxResultWindow;

    public SearchStrategySelector(SearchExecutionStrategy standardStrategy, SearchExecutionStrategy scrollStrategy,
            SearchExecutionStrategy searchAfterStrategy, int maxResultWindow)
    {
        this.standardStrategy = standardStrategy;
        this.scrollStrategy = scrollStrategy;
        this.searchAfterStrategy = searchAfterStrategy;
        this.maxResultWindow = maxResultWindow;
    }

    @Override
    public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions) throws IOException
    {
        if (searchAfterNeeded(searchParameters))
        {
            return searchAfterStrategy.executeSearch(searchParameters, queryWithPermissions);
        }
        else if (scrollNeeded(searchParameters))
        {
            return scrollStrategy.executeSearch(searchParameters, queryWithPermissions);
        }
        else
        {
            return standardStrategy.executeSearch(searchParameters, queryWithPermissions);
        }
    }

    /**
     * Use {@code search_after} whenever a cursor is present. An empty cursor means the first page, which routes here too so every page shares the same tiebreaker sort.
     *
     * @param searchParameters
     *            Search parameters.
     * @return true if {@code search_after} paging is requested.
     */
    private boolean searchAfterNeeded(SearchParameters searchParameters)
    {
        return searchParameters.getSearchAfter() != null;
    }

    /**
     * Returns true if scroll is needed: no facets and requested results exceed max window.
     *
     * @param searchParameters
     *            Search parameters.
     * @return true if scroll is required.
     */
    private boolean scrollNeeded(SearchParameters searchParameters)
    {
        boolean hasFacets = !searchParameters.getFacetQueries().isEmpty();
        int totalRequested = searchParameters.getSkipCount() + searchParameters.getLimit();
        boolean exceedsMaxResultWindow = totalRequested > maxResultWindow;
        return !hasFacets && exceedsMaxResultWindow;
    }
}
