/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Simple wrapper for single page request (with optional request for total count up to a given max)
 * 
 * @author janv
 * @since 4.0
 */
@AlfrescoPublicApi
public class PagingRequest
{
    private int skipCount = CannedQueryPageDetails.DEFAULT_SKIP_RESULTS;
    private int maxItems;
    
    private int requestTotalCountMax = 0; // request total count up to a given max (0 => do not request total count)
    private String queryExecutionId;

    /**
     * Construct a page request
     * 
     * @param maxItems              the maximum number of items per page
     */
    public PagingRequest(int maxItems)
    {
        this.maxItems = maxItems;
    }
    
    /**
     * Construct a page request
     * 
     * @param maxItems              the maximum number of items per page
     * @param skipCount             the number of items to skip before the first page
     */
    public PagingRequest(int skipCount, int maxItems)
    {
        this.skipCount = skipCount;
        this.maxItems = maxItems;
    }
    
    /**
     * Construct a page request
     * 
     * @param maxItems              the maximum number of items per page
     * @param queryExecutionId      a query execution ID associated with ealier paged requests
     */
    public PagingRequest(int maxItems, String queryExecutionId)
    {
        setMaxItems(maxItems);
        this.queryExecutionId = queryExecutionId;
    }
    
    /**
     * Construct a page request
     * 
     * @param skipCount             the number of items to skip before the first page
     * @param maxItems              the maximum number of items per page
     * @param queryExecutionId      a query execution ID associated with ealier paged requests
     */
    public PagingRequest(int skipCount, int maxItems, String queryExecutionId)
    {
        setSkipCount(skipCount);
        setMaxItems(maxItems);
        this.queryExecutionId = queryExecutionId;
    }
    
    /**
     * Results to skip before retrieving the page.  Usually a multiple of page size (ie. page size * num pages to skip).
     * Default is 0.
     * 
     * @return          the number of results to skip before the page
     */
    public int getSkipCount()
    {
        return skipCount;
    }
    
    /**
     * Change the skip count. Must be called before the paging query is run. 
     */
    protected void setSkipCount(int skipCount)
    {
        this.skipCount = (skipCount < 0 ? CannedQueryPageDetails.DEFAULT_SKIP_RESULTS : skipCount);
    }
    
    /**
     * Size of the page - if skip count is 0 then return up to max items.
     * 
     * @return          the maximum size of the page
     */
    public int getMaxItems()
    {
        return maxItems;
    }
    
    /**
     * Change the size of the page. Must be called before the paging query is run.
     */
    protected void setMaxItems(int maxItems)
    {
        this.maxItems = (maxItems < 0 ? CannedQueryPageDetails.DEFAULT_PAGE_SIZE : maxItems);
    }
    
    /**
     * Get requested total count (up to a given maximum).
     */
    public int getRequestTotalCountMax()
    {
        return requestTotalCountMax;
    }
    
    /**
     * Set request total count (up to a given maximum).  Default is 0 => do not request total count (which allows possible query optimisation).
     * 
     * @param requestTotalCountMax
     */
    public void setRequestTotalCountMax(int requestTotalCountMax)
    {
        this.requestTotalCountMax = requestTotalCountMax;
    }
    
    /**
     * Get a unique ID associated with these query results.  This must be available before and
     * after execution i.e. it must depend on the type of query and the query parameters
     * rather than the execution results.  Client has the option to pass this back as a hint when
     * paging.
     * 
     * @return                      a unique ID associated with the query execution results
     */
    public String getQueryExecutionId()
    {
        return queryExecutionId;
    }
    
    /**
     * Change the unique query ID for the results. Must be called before the paging query is run.
     */
    protected void setQueryExecutionId(String queryExecutionId)
    {
        this.queryExecutionId = queryExecutionId; 
    }
}
