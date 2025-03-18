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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.alfresco.util.Pair;

/** This comparator defines the default sort order for facets. */
public class SolrFacetComparator implements Comparator<SolrFacetProperties>
{
    /** A sequence of facet IDs which defines their order, as used in REST API &amp; UI. */
    private final List<String> sortedIDs;

    public SolrFacetComparator(List<String> sortedIDs)
    {
        this.sortedIDs = new ArrayList<>();
        if (sortedIDs != null)
        {
            this.sortedIDs.addAll(sortedIDs);
        }
    }

    @Override
    public int compare(SolrFacetProperties facet1, SolrFacetProperties facet2)
    {
        if (sortedIDs.isEmpty())
        {
            return facet1.getFilterID().compareTo(facet2.getFilterID());
        }

        Pair<Integer, Integer> facetIndicesInSortedList = find(facet1, facet2);

        if (bothSorted(facetIndicesInSortedList))
        {
            // Sorting is by position in the sortedIDs list.
            return facetIndicesInSortedList.getFirst() - facetIndicesInSortedList.getSecond();
        }
        else if (neitherSorted(facetIndicesInSortedList))
        {
            return facet1.getFilterID().compareTo(facet2.getFilterID());
        }
        else
        {
            // One is in the sortedIDs list and one is not.
            // All we want in this case is predictability. The order should be the same.
            // We'll (arbitrarily) have facets without an explicit position go at the end.
            return facetIndicesInSortedList.getSecond() == -1 ? -1 : 1;
        }
    }

    /** Get the positional indices of the provided {@link SolrFacetProperties} in the {@link #sortedIDs}. */
    private Pair<Integer, Integer> find(SolrFacetProperties facet1, SolrFacetProperties facet2)
    {
        return new Pair<>(sortedIDs.indexOf(facet1.getFilterID()),
                sortedIDs.indexOf(facet2.getFilterID()));
    }

    /** Are both of the provided positional indexes in the {@link #sortedIDs}? */
    private boolean bothSorted(Pair<Integer, Integer> indices)
    {
        return indices.getFirst() != -1 && indices.getSecond() != -1;
    }

    /** Are neither of the provided positional indexes in the {@link #sortedIDs}? */
    private boolean neitherSorted(Pair<Integer, Integer> indices)
    {
        return indices.getFirst() == -1 && indices.getSecond() == -1;
    }
}
