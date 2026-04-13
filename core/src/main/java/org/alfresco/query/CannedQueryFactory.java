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
 * Interface for factory implementations for producing instances of {@link CannedQuery}
 * based on all the query parameters.
 * 
 * @param <R>                   the query result type
 * 
 * @author Derek Hulley, janv
 * @since 4.0
 */
public interface CannedQueryFactory<R>
{
    /**
     * Retrieve an instance of a {@link CannedQuery} based on the full range of
     * available parameters.
     * 
     * @param parameters            the full query parameters
     * @return                      an implementation that will execute the query
     */
    CannedQuery<R> getCannedQuery(CannedQueryParameters parameters);
    
    /**
     * Retrieve an instance of a {@link CannedQuery} based on limited parameters.
     * 
     * @param parameterBean         the values that the query will be based on or <tt>null</tt>
     *                              if not relevant to the query
     * @param skipResults           results to skip before page
     * @param pageSize              the size of page - ie. max items (if skipResults = 0)
     * @param queryExecutionId      ID of a previously-executed query to be used during follow-up
     *                              page requests - <tt>null</tt> if not available
     * @return                      an implementation that will execute the query
     */
    CannedQuery<R> getCannedQuery(Object parameterBean, int skipResults, int pageSize, String queryExecutionId);
}
