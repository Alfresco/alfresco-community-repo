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

/**
 * Parameters defining the {@link CannedQuery named query} to execute.
 * <p/>
 * The implementations of the underlying queries may be vastly different
 * depending on seemingly-minor variations in the parameters; only set the
 * parameters that are required.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class CannedQueryParameters
{
    public static final int DEFAULT_TOTAL_COUNT_MAX = 0; // default 0 => don't request total count
    
    private final Object parameterBean;
    private final CannedQueryPageDetails pageDetails;
    private final CannedQuerySortDetails sortDetails;
    private final int totalResultCountMax;
    private final String queryExecutionId;

    /**
     * <ul>
     *    <li><b>pageDetails</b>: <tt>null</tt></li>
     *    <li><b>sortDetails</b>: <tt>null</tt></li>
     *    <li><b>totalResultCountMax</b>: <tt>0</tt></li>
     *    <li><b>queryExecutionId</b>: <tt>null</tt></li>
     * </ul>
     *  
     */
    public CannedQueryParameters(Object parameterBean)
    {
        this (parameterBean, null, null, DEFAULT_TOTAL_COUNT_MAX, null);
    }

    /**
     * Defaults:
     * <ul>
     *    <li><b>pageDetails.pageNumber</b>: <tt>1</tt></li>
     *    <li><b>pageDetails.pageCount</b>: <tt>1</tt></li>
     *    <li><b>totalResultCountMax</b>: <tt>0</tt></li>
     * </ul>
     *  
     */
    public CannedQueryParameters(
            Object parameterBean,
            int skipResults,
            int pageSize,
            String queryExecutionId)
    {
        this (
                parameterBean,
                new CannedQueryPageDetails(skipResults, pageSize, CannedQueryPageDetails.DEFAULT_PAGE_NUMBER, CannedQueryPageDetails.DEFAULT_PAGE_COUNT),
                null,
                DEFAULT_TOTAL_COUNT_MAX,
                queryExecutionId);
    }

    /**
     * Defaults:
     * <ul>
     *    <li><b>totalResultCountMax</b>: <tt>0</tt></li>
     *    <li><b>queryExecutionId</b>: <tt>null</tt></li>
     * </ul>
     *  
     */
    public CannedQueryParameters(
            Object parameterBean,
            CannedQueryPageDetails pageDetails,
            CannedQuerySortDetails sortDetails)
    {
        this (parameterBean, pageDetails, sortDetails, DEFAULT_TOTAL_COUNT_MAX, null);
    }

    /**
     * Construct all the parameters for executing a named query, using values from the
     * {@link PagingRequest}.
     * 
     * @param parameterBean         the values that the query will be based on or <tt>null</tt>
     *                              if not relevant to the query
     * @param sortDetails           the type of sorting to be applied or <tt>null</tt> for none
     * @param pagingRequest           the type of paging to be applied or <tt>null</tt> for none
     */
    public CannedQueryParameters(
            Object parameterBean,
            CannedQuerySortDetails sortDetails,
            PagingRequest pagingRequest)
    {
        this (
                parameterBean,
                pagingRequest == null ? null : new CannedQueryPageDetails(pagingRequest),
                sortDetails,
                pagingRequest == null ? 0 : pagingRequest.getRequestTotalCountMax(),
                pagingRequest == null ? null : pagingRequest.getQueryExecutionId());
    }

    /**
     * Construct all the parameters for executing a named query.  Note that the allowable values
     * for the arguments depend on the specific query being executed.
     * 
     * @param parameterBean         the values that the query will be based on or <tt>null</tt>
     *                              if not relevant to the query
     * @param pageDetails           the type of paging to be applied or <tt>null</tt> for none
     * @param sortDetails           the type of sorting to be applied or <tt>null</tt> for none
     * @param totalResultCountMax   greater than zero if the query should not only return the required rows
     *                              but should also return the total number of possible rows up to
     *                              the given maximum.
     * @param queryExecutionId      ID of a previously-executed query to be used during follow-up
     *                              page requests - <tt>null</tt> if not available
     */
    @SuppressWarnings("unchecked")
    public CannedQueryParameters(
            Object parameterBean,
            CannedQueryPageDetails pageDetails,
            CannedQuerySortDetails sortDetails,
            int totalResultCountMax,
            String queryExecutionId)
    {
        if (totalResultCountMax < 0)
        {
            throw new IllegalArgumentException("totalResultCountMax cannot be negative.");
        }
        
        this.parameterBean = parameterBean;
        this.pageDetails = pageDetails == null ? new CannedQueryPageDetails() : pageDetails;
        this.sortDetails = sortDetails == null ? new CannedQuerySortDetails() : sortDetails;
        this.totalResultCountMax = totalResultCountMax;
        this.queryExecutionId = queryExecutionId;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("NamedQueryParameters ")
          .append("[parameterBean=").append(parameterBean)
          .append(", pageDetails=").append(pageDetails)
          .append(", sortDetails=").append(sortDetails)
          .append(", requestTotalResultCountMax=").append(totalResultCountMax)
          .append(", queryExecutionId=").append(queryExecutionId)
          .append("]");
        return sb.toString();
    }

    public String getQueryExecutionId()
    {
        return queryExecutionId;
    }

    /**
     * @return              the sort details (never <tt>null</tt>)
     */
    public CannedQuerySortDetails getSortDetails()
    {
        return sortDetails;
    }

    /**
     * @return              the query paging details (never <tt>null</tt>)
     */
    public CannedQueryPageDetails getPageDetails()
    {
        return pageDetails;
    }

    /**
     * @return                      if > 0 then the query should not only return the required rows but should
     *                              also return the total count (number of possible rows) up to the given max
     *                              if 0 then query does not need to return the total count
     */
    public int getTotalResultCountMax()
    {
        return totalResultCountMax;
    }
    
    /**
     * Helper method to get the total number of query results that need to be obtained in order
     * to satisfy the {@link #getPageDetails() paging requirements}, the
     *  maximum result count ... and an extra to provide
     * 'hasMore' functionality.
     * 
     * @return                      the minimum number of results required before pages can be created
     */
    public int getResultsRequired()
    {
        int resultsForPaging = pageDetails.getResultsRequiredForPaging();
        if (resultsForPaging < Integer.MAX_VALUE)       // Add one for 'hasMore'
        {
            resultsForPaging++;
        }
        int maxRequired = Math.max(totalResultCountMax, resultsForPaging);
        return maxRequired;
    }

    /**
     * @return parameterBean        the values that the query will be based on or <tt>null</tt>
     *                              if not relevant to the query
     */
    public Object getParameterBean()
    {
        return parameterBean;
    }
}
