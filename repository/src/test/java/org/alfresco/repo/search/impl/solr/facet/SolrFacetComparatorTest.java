/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.solr.facet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;

/** Some Unit tests for {@link SolrFacetComparator}. */
public class SolrFacetComparatorTest
{
    @Test
    public void simpleSortOfSortedFacets() throws Exception
    {
        List<String> expectedIds = Arrays.asList(new String[]{"a", "b", "c", "d"});

        SolrFacetProperties.Builder builder = new SolrFacetProperties.Builder();

        List<SolrFacetProperties> facets = Arrays.asList(new SolrFacetProperties[]{
                builder.filterID("a").build(),
                builder.filterID("d").build(),
                builder.filterID("b").build(),
                builder.filterID("c").build(),
        });
        Collections.sort(facets, new SolrFacetComparator(expectedIds));

        assertEquals(expectedIds, toFacetIds(facets));
    }

    private List<String> toFacetIds(List<SolrFacetProperties> facets)
    {
        return CollectionUtils.transform(facets, new Function<SolrFacetProperties, String>() {
            @Override
            public String apply(SolrFacetProperties value)
            {
                return value.getFilterID();
            }
        });
    }
}
