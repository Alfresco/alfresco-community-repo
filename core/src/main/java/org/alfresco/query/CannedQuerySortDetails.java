/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.Pair;

/**
 * Details for canned queries supporting sorted results
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CannedQuerySortDetails
{
    /**
     * Sort ordering for the sort pairs.
     * @author Derek Hulley
     * @since 4.0
     */
    public static enum SortOrder
    {
        ASCENDING,
        DESCENDING
    }
    
    private final List<Pair<? extends Object, SortOrder>> sortPairs;
    
    /**
     * Construct the sort details with a variable number of sort pairs.
     * <p/>
     * Sorting is done by:<br/>
     * <b>key:</b> the key type to sort on<br/>
     * <b>sortOrder:</b> the ordering of values associated with the key<br/>
     * 
     * @param sortPairs             the sort pairs, which will be applied in order
     */
    public CannedQuerySortDetails(Pair<? extends Object, SortOrder> ... sortPairs)
    {
        this.sortPairs = Collections.unmodifiableList(Arrays.asList(sortPairs));
    }
    
    /**
     * Construct the sort details from a list of sort pairs.
     * <p/>
     * Sorting is done by:<br/>
     * <b>key:</b> the key type to sort on<br/>
     * <b>sortOrder:</b> the ordering of values associated with the key<br/>
     * 
     * @param sortPairs             the sort pairs, which will be applied in order
     */
    public CannedQuerySortDetails(List<Pair<? extends Object, SortOrder>> sortPairs)
    {
        this.sortPairs = Collections.unmodifiableList(sortPairs);
    }
    
    @Override
    public String toString()
    {
        return "CannedQuerySortDetails [sortPairs=" + sortPairs + "]";
    }

    /**
     * Get the sort definitions.  The instance will become unmodifiable after this has been called.
     */
    public List<Pair<? extends Object, SortOrder>> getSortPairs()
    {
        return Collections.unmodifiableList(sortPairs);
    }
}
