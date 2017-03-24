/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.api.search.context;

import static java.util.stream.Collectors.*;
import org.alfresco.rest.api.search.model.FacetFields;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.Pivot;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.service.cmr.search.IntervalParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is a snapshot of the SearchQuery before the request is made.
 * It isn't a complete copy of SearchQuery but only has fields that are useful when building
 * the response
 */
public class SearchRequestContext
{

    private final Query query;
    private final boolean includeRequest;
    private final Map<String, String> pivotKeys;

    private SearchRequestContext(Query query, boolean includeRequest)
    {
        this.query = query;
        this.includeRequest = includeRequest;
        this.pivotKeys = new HashMap<>();
        /**
        this.facetQueries = facetQueries!=null?Collections.unmodifiableList(facetQueries): Collections.emptyList();
        this.facetFields = new FacetFields(facetFields!=null?Collections.unmodifiableList(facetFields.getFacets()):Collections.emptyList());
        this.facetIntervals = facetIntervals!=null?
                    new IntervalParameters(Collections.unmodifiableList(facetIntervals.getSets()),
                                           Collections.unmodifiableList(facetIntervals.getIntervals()))
                    :
                    new IntervalParameters(Collections.emptyList(),Collections.emptyList());
        this.pivots = pivots!=null?Collections.unmodifiableList(pivots): Collections.emptyList();**/
    }

    public static final SearchRequestContext from(SearchQuery searchQuery)
    {
        return new SearchRequestContext(searchQuery.getQuery(), searchQuery.includeRequest());
    }

    public Query getQuery()
    {
        return query;
    }
    public boolean includeRequest()
    {
        return includeRequest;
    }

    public Map<String, String> getPivotKeys()
    {
        return pivotKeys;
    }
}
