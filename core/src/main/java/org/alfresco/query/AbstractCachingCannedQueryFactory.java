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

import java.util.List;

/**
 * Caching support extension for {@link CannedQueryFactory} implementations.
 * <p/>
 * Depending on the parameters provided, this class may choose to pick up existing results
 * and re-use them for later page requests; the client will not have knowledge of the
 * shortcuts.
 * 
 * TODO: This is work-in-progress
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public abstract class AbstractCachingCannedQueryFactory<R> extends AbstractCannedQueryFactory<R>
{
    /**
     * Base implementation that provides a caching facade around the query.
     * 
     * @return              a decoraded facade query that will cache query results for later paging requests
     */
    @Override
    public final CannedQuery<R> getCannedQuery(CannedQueryParameters parameters)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Derived classes must implement this method to provide the raw query that supports the given
     * parameters.  All requests must be serviced without any further caching in order to prevent
     * duplicate caching.
     * 
     * @param parameters    the query parameters as given by the client
     * @return              the query that will generate the results
     */
    protected abstract CannedQuery<R> getCannedQueryImpl(CannedQueryParameters parameters);
    
    private class CannedQueryCacheFacade<R> extends AbstractCannedQuery<R>
    {
        private final AbstractCannedQuery<R> delegate;
        
        private CannedQueryCacheFacade(CannedQueryParameters params, AbstractCannedQuery<R> delegate)
        {
            super(params);
            this.delegate = delegate;
        }
        
        @Override
        protected List<R> queryAndFilter(CannedQueryParameters parameters)
        {
            // Copy the parameters and remove all references to paging.
            // The underlying query will return full or filtered results (possibly also sorted)
            // but will not apply page limitations
            
            throw new UnsupportedOperationException();
        }
    }
}
