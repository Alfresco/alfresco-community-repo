/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.search.impl.solr.facet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.junit.Test;

/**Some Unit tests for {@link SolrFacetComparator}. */
public class SolrFacetComparatorTest
{
    @Test public void simpleSortOfSortedFacets() throws Exception
    {
        List<String> expectedIds = Arrays.asList(new String[] { "a", "b", "c"});
        
        SolrFacetProperties.Builder builder = new SolrFacetProperties.Builder();
        
        List<SolrFacetProperties> facets = Arrays.asList(new SolrFacetProperties[]
                                                         {
                                                            builder.filterID("c").index(1).build(),
                                                            builder.filterID("b").index(2).build(),
                                                            builder.filterID("a").index(3).build(),
                                                         });
        Collections.sort(facets, new SolrFacetComparator(expectedIds));
        
        assertEquals(expectedIds, toFacetIds(facets));
    }
    
    private List<String> toFacetIds(List<SolrFacetProperties> facets)
    {
        return CollectionUtils.transform(facets, new Function<SolrFacetProperties, String>()
                {
                    @Override public String apply(SolrFacetProperties value)
                    {
                        return value.getFilterID();
                    }
                });
    }
}
