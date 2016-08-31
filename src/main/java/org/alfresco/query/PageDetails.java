/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

/**
 * Stores paging details based on a PagingRequest.
 * 
 * @author steveglover
 *
 */
public class PageDetails
{
    private boolean hasMoreItems = false;
    private int pageSize;
    private int skipCount;
    private int maxItems;
    private int end;

    public PageDetails(int pageSize, boolean hasMoreItems, int skipCount, int maxItems, int end)
    {
        super();
        this.hasMoreItems = hasMoreItems;
        this.pageSize = pageSize;
        this.skipCount = skipCount;
        this.maxItems = maxItems;
        this.end = end;
    }

    public int getSkipCount()
    {
        return skipCount;
    }

    public int getMaxItems()
    {
        return maxItems;
    }

    public int getEnd()
    {
        return end;
    }

    public boolean hasMoreItems()
    {
        return hasMoreItems;
    }

    public int getPageSize()
    {
        return pageSize;
    }
    
    public static PageDetails getPageDetails(PagingRequest pagingRequest, int totalSize)
    {
        int skipCount = pagingRequest.getSkipCount();
        int maxItems = pagingRequest.getMaxItems();
        int end = skipCount + maxItems;
        int pageSize = -1;
        if(end < 0 || end > totalSize)
        {
            // overflow or greater than the total
            end = totalSize;
            pageSize = end - skipCount;
        }
        else
        {
            pageSize = maxItems;
        }
        if(pageSize < 0)
        {
            pageSize = 0;
        }
        boolean hasMoreItems = end < totalSize;
        return new PageDetails(pageSize, hasMoreItems, skipCount, maxItems, end);
    }
}
