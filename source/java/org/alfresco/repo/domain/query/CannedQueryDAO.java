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
package org.alfresco.repo.domain.query;

import java.util.List;

/**
 * DAO services for general-use database canned queries.  Note that this is specifically targeted
 * at low-level queries to the persistance layer.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public interface CannedQueryDAO
{
    /*
     * This interface looks very much like SqlSessionTemplate; this is not accidental.
     * However, the use of generics and the ability to trace all general SQL queries through
     * an Alfresco API make it useful.  Additionally, there are specific checks done in the
     * implementation to insulate the client from some common problems. 
     */
    
    /**
     * Execute a <b>count(*)</b>-style query returning a count value.  The implementation
     * will ensure that <tt>null</tt> is substituted with <tt>0</tt>, if required.
     * <p>
     * All exceptions can be safely caught and handled as required.
     * 
     * @param sqlNamespace          the query namespace (defined by config file) e.g. <b>alfresco.query.usage</b>
     * @param queryName             the name of the query e.g. <b>select_userCount</b>
     * @param parameterObj          the values to drive the selection (may be <tt>null</tt> if not required)
     * @return                      a non-null count
     * 
     * @throws QueryException       if the query returned multiple results
     */
    Long executeCountQuery(String sqlNamespace, String queryName, Object parameterObj);
    
    /**
     * Execute a query that returns exactly one result.  The assumption is that the parameters provided
     * uniquely identify the object.
     * 
     * @param <R>                   the return value's type
     * @param sqlNamespace          the query namespace (defined by config file) e.g. <b>alfresco.query.usage</b>
     * @param queryName             the name of the query e.g. <b>select_userCount</b>
     * @param parameterObj          the values to drive the selection (may be <tt>null</tt> if not required)
     * @return                      the unique result (never <tt>null</tt>)
     * @throws                      concurrency-related exception if a single object was not found
     */
    <R> R executeQueryUnique(String sqlNamespace, String queryName, Object parameterObj);
    
    /**
     * Execute a query that returns one or more results.
     * 
     * @param <R>                   the return value's type
     * @param sqlNamespace          the query namespace (defined by config file) e.g. <b>alfresco.query.usage</b>
     * @param queryName             the name of the query e.g. <b>select_userCount</b>
     * @param parameterObj          the values to drive the selection (may be <tt>null</tt> if not required)
     * @param offset                the number of results to skip
     * @param limit                 the maximum number of results to retrieve
     * @return                      
     */
    <R> List<R> executeQuery(
            String sqlNamespace, String queryName, Object parameterObj,
            int offset, int limit);

    /**
     * Execute a query that returns one or more results, processing the results using a handler.
     * 
     * @param <R>                   the return value's type
     * @param sqlNamespace          the query namespace (defined by config file) e.g. <b>alfresco.query.usage</b>
     * @param queryName             the name of the query e.g. <b>select_userCount</b>
     * @param parameterObj          the values to drive the selection (may be <tt>null</tt> if not required)
     * @param offset                the number of results to skip
     * @param limit                 the maximum number of results to retrieve
     * @return                      
     */
    <R> void executeQuery(
            String sqlNamespace,
            String queryName,
            Object parameterObj,
            int offset,
            int limit,
            ResultHandler<R> handler);
    
    /**
     * A simple, typed results handler.
     * 
     * @author Derek Hulley
     * @since 4.0
     *
     * @param <R>                   the type of the result
     */
    public interface ResultHandler<R>
    {
        /**
         * Allow implementations to process a result.  Note that the interface contract will
         * be met, but internally the querying mechanism <i>might not</i> be able to optimise out
         * all result fetching.
         * 
         * @return                  <tt>true</tt> if more results are required or <tt>false</tt> to
         *                          terminate result fetching.
         */
        boolean handleResult(R result);
    }
}
