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
package org.alfresco.repo.webservice;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract implementation of the query session that keeps track of the paging data.
 * It provides support for paging of results of <code>Serializable[]</code> instances.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public abstract class AbstractQuerySession<RESULTSET, RESULTSETROW> implements QuerySession<RESULTSET>
{
    private static Log logger = LogFactory.getLog(AbstractQuerySession.class);
 
    private String id;
    private long maxResults;
    private long batchSize;
    private ServerQuery<RESULTSET> query;
    /** a transient cache of the query results */
    private transient RESULTSET cachedResults;
    
    /**
     * A pointer to the first row to be returned.  When the last result is returned, the
     * position will be out of range of the current results by 1.
     */
    private long position;
    /**
     * Keep track of whether the position has previously passed the end of a set of results.
     */
    private boolean expectMoreResults;

    /**
     * Common constructor that initialises the session's id and batch size
     * 
     * @param maxResults
     *      the maximum number of results to retrieve for the query.  This is not the page
     *      size, which is normally significantly smaller.
     * @param batchSize
     *      the batch size this session will use
     * @param query
     *      the query that generates the results
     */
    public AbstractQuerySession(long maxResults, long batchSize, ServerQuery<RESULTSET> query)
    {
        this.id = GUID.generate();
        this.batchSize = batchSize;
        this.maxResults = maxResults;
        this.query = query;
        this.position = 0;
        this.expectMoreResults = true;
    }

    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * {@inheritDoc}
     */
    public ServerQuery<RESULTSET> getQuery()
    {
        return query;
    }

    /**
     * Helper method to get the results.  This may be a cached value or may be
     * freshly retrieved from the query object.
     * 
     * @param serviceRegistry   the 
     * @return                  the query results, new or cached
     */
    protected RESULTSET getQueryResults(ServiceRegistry serviceRegistry)
    {
        if (cachedResults != null)
        {
            return cachedResults;
        }
        // Get the results and cache them
        cachedResults = query.execute(serviceRegistry, maxResults);
        // Done
        return cachedResults;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean haveMoreResults()
    {
        return expectMoreResults;
    }

    protected abstract RESULTSETROW[] makeArray(int size);
    
    /**
     * Helper method to page through the results.  The task of retrieving, unwrapping and
     * rewrapping the array of results (rows) is left up to the derived implementations.
     */
    protected final RESULTSETROW[] getNextResults(RESULTSETROW[] allResults)
    {
        /*
         * This class can't manipulate the query to get the results because each
         * query implementation's results (the array of rows) is contained within
         * a different type of object.  This method helps
         */

        long allResultsSize = allResults.length;

        RESULTSETROW[] batchedResults = null;
        if (position >= allResultsSize)
        {
            // We are already past the last result
            batchedResults = makeArray(0);
            // Position is after last
            position = allResultsSize;
        }
        else if (position == 0 && batchSize >= allResultsSize)
        {
            // We can give back the original results
            batchedResults = allResults;
            // Position is after last
            position = allResultsSize;
        }
        else if ((position + batchSize) >= allResultsSize)
        {
            // There isn't an excess of rows remaining, so copy to the last one
            long rowCopyCount = allResultsSize - position; //ETWOONE-396 second part
            batchedResults = makeArray((int)rowCopyCount);
            System.arraycopy(allResults, (int)position, batchedResults, 0, (int)rowCopyCount);
            // Position is after last
            position = allResultsSize;
        }
        else
        {
            // There are an excess of rows remaining
            batchedResults = makeArray((int)batchSize);
            System.arraycopy(allResults, (int)position, batchedResults, 0, (int)batchSize);
            // Position increases by the batch size
            position += batchSize;
        }
        // Keep track of whether we expect more results
        if (position >= allResultsSize)
        {
            expectMoreResults = false;
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("\n" +
                    "Fetched next set of results: \n" +
                    "   Total results count: " + allResultsSize + "\n" +
                    "   Batch size:          " + batchedResults.length + "\n" +
                    "   New Position:        " + position);
        }
        return batchedResults;
    }
}
