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

/**
 * Details for canned queries supporting paged results.
 * <p/>
 * Results are {@link #skipResults skipped}, chopped into pages of
 * {@link #pageSize appropriate size} before the {@link #pageCount start page}
 * and {@link #pageNumber number} are returned.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CannedQueryPageDetails
{
    public static final int DEFAULT_SKIP_RESULTS = 0;
    public static final int DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_COUNT = 1;
    
    private final int skipResults;
    private final int pageSize;
    private final int pageNumber;
    private final int pageCount;
    
    /**
     * Construct with defaults
     * <ul>
     *   <li><b>skipResults:</b> {@link #DEFAULT_SKIP_RESULTS}</li>
     *   <li><b>pageSize:</b> {@link #DEFAULT_PAGE_SIZE}</li>
     *   <li><b>pageNumber:</b> {@link #DEFAULT_PAGE_NUMBER}</li>
     *   <li><b>pageCount:</b> {@link #DEFAULT_PAGE_COUNT}</li>
     * </ul>
     */
    public CannedQueryPageDetails()
    {
        this(DEFAULT_SKIP_RESULTS, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_COUNT);
    }
    
    /**
     * Construct with defaults
     * <ul>
     *   <li><b>pageNumber:</b> {@link #DEFAULT_PAGE_NUMBER}</li>
     *   <li><b>pageCount:</b> {@link #DEFAULT_PAGE_COUNT}</li>
     * </ul>
     * @param skipResults               results to skip before <i>page one</i>
     *                                  (default <b>{@link #DEFAULT_SKIP_RESULTS}</b>)
     * @param pageSize                  the size of each page
     *                                  (default <b>{@link #DEFAULT_PAGE_SIZE}</b>)
     */
    public CannedQueryPageDetails(int skipResults, int pageSize)
    {
        this (skipResults, pageSize, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_COUNT);
    }
    
    /**
     * @param skipResults               results to skip before <i>page one</i>
     *                                  (default <b>{@link #DEFAULT_SKIP_RESULTS}</b>)
     * @param pageSize                  the size of each page
     *                                  (default <b>{@link #DEFAULT_PAGE_SIZE}</b>)
     * @param pageNumber                the first page number to return
     *                                  (default <b>{@link #DEFAULT_PAGE_NUMBER}</b>)
     * @param pageCount                 the number of pages to return
     *                                  (default <b>{@link #DEFAULT_PAGE_COUNT}</b>)
     */
    public CannedQueryPageDetails(int skipResults, int pageSize, int pageNumber, int pageCount)
    {
        this.skipResults = skipResults;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.pageCount = pageCount;
        
        // Do some checks
        if (skipResults < 0)
        {
            throw new IllegalArgumentException("Cannot skip fewer than 0 results.");
        }
        if (pageSize < 1)
        {
            throw new IllegalArgumentException("pageSize must be greater than zero.");
        }
        if (pageNumber < 1)
        {
            throw new IllegalArgumentException("pageNumber must be greater than zero.");
        }
        if (pageCount < 1)
        {
            throw new IllegalArgumentException("pageCount must be greater than zero.");
        }
    }
    
    /**
     * Helper constructor to transform a paging request into the Canned Query form.
     * 
     * @param pagingRequest             the paging details
     */
    public CannedQueryPageDetails(PagingRequest pagingRequest)
    {
        this(pagingRequest.getSkipCount(), pagingRequest.getMaxItems());
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("NamedQueryPageDetails ")
          .append("[skipResults=").append(skipResults)
          .append(", pageSize=").append(pageSize)
          .append(", pageCount=").append(pageCount)
          .append(", pageNumber=").append(pageNumber)
          .append("]");
        return sb.toString();
    }
    
    /**
     * Get the number of query results to skip before applying further page parameters
     * @return                          results to skip before <i>page one</i>
     */
    public int getSkipResults()
    {
        return skipResults;
    }
    
    /**
     * Get the size of each page
     * @return                          the size of each page
     */
    public int getPageSize()
    {
        return pageSize;
    }
    
    /**
     * Get the first page number to return
     * @return                          the first page number to return
     */
    public int getPageNumber()
    {
        return pageNumber;
    }
    
    /**
     * Get the total number of pages to return
     * @return                          the number of pages to return
     */
    public int getPageCount()
    {
        return pageCount;
    }
    
    /**
     * Calculate the number of results that would be required to satisy this paging request.
     * Note that the skip size can significantly increase this number even if the page sizes
     * are small.
     * 
     * @return                          the number of results required for proper paging
     */
    public int getResultsRequiredForPaging()
    {
        int tmp = skipResults + pageCount * pageSize;
        if(tmp < 0)
        {
            // overflow
            return Integer.MAX_VALUE;
        }
        else
        {
            return tmp;
        }
    }
}
