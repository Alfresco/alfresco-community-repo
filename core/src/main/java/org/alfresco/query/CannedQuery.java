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

/**
 * Interface for named query implementations.  These are queries that encapsulate varying
 * degrees of functionality, but ultimately provide support for paging results.
 * <p/>
 * Note that each instance of the query is stateful and cannot be reused.
 * 
 * @param <R>               the query result type
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface CannedQuery<R>
{
    /**
     * Get the original parameters used to generate the query.
     * 
     * @return              the parameters used to obtain the named query.
     */
    CannedQueryParameters getParameters();
    
    /**
     * Execute the named query, which was provided to support the
     * {@link #getParameters() parameters} originally provided.
     * <p/>
     * <b>Note: This method can only be used once</b>; to requery, get a new
     * instance from the {@link CannedQueryFactory factory}.
     * 
     * @return              the query results
     * 
     * @throws IllegalStateException on second and subsequent calls to this method
     */
    CannedQueryResults<R> execute();
}
