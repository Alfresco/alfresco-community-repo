/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.util.Collections;
import java.util.List;

import org.alfresco.util.Pair;

/**
 * Wraps a list of items as a {@link PagingResults}, used typically when
 *  migrating from a full listing system to a paged one.
 * 
 * @author Nick Burch
 * @since Odin
 */
public class ListBackedPagingResults<R> implements PagingResults<R>
{
    private List<R> results;
    private int size;
    private boolean hasMore;
    
    public ListBackedPagingResults(List<R> list)
    {
        this.results = Collections.unmodifiableList(list);
        
        // No more items remain, the page is everything
        size = list.size();
        hasMore = false;
    }
    public ListBackedPagingResults(List<R> list, PagingRequest paging)
    {
        // Excerpt
        int start = paging.getSkipCount();
        int end = Math.min(list.size(), start + paging.getMaxItems());
        if (paging.getMaxItems() == 0)
        {
            end = list.size();
        }
        
        this.results = Collections.unmodifiableList(
                list.subList(start, end));
        this.size = list.size();
        this.hasMore = ! (list.size() == end);
    }
    
    /**
     * Returns the whole set of results as one page
     */
    public List<R> getPage()
    {
       return results;
    }
    
    public boolean hasMoreItems()
    {
       return hasMore;
    }
    
    /**
     * We know exactly how many results there are
     */
    public Pair<Integer, Integer> getTotalResultCount()
    {
       return new Pair<Integer,Integer>(size, size);
    }
    
    /**
     * There is no unique query ID, as no query was done
     */
    public String getQueryExecutionId()
    {
       return null;
    }
}
