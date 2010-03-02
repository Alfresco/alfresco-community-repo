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

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;

/**
 * An interface for objects that track the query and its results.  The only commonality between
 * the different types of results used in the WebServices return values is that they are
 * Serializable.
 * 
 * @author Derek Hulley
 */
public interface QuerySession<RESULTSET> extends Serializable
{
    /**
     * Retrieves the id this query session can be identified as
     * 
     * @return Id of this query session
     */
    public String getId();
    
    /**
     * Check if the session is expecting more results.  This will be <tt>false</tt> if the
     * cursor previously passed the end of a given set of results.
     */
    public boolean haveMoreResults();

    /**
     * Get the next batch of results from the full set of available results.  If there are no results
     * available, then this session must go and fetch them.  It is up to the implementation to provide
     * a means for this to occur.
     *
     * @param serviceRegistry
     *      the services to perform a query
     * @param allResults
     *      All available results.  It may be necessary to requery to get the results.
     * @return
     *      Returns the next batch of results based on the maximum fetch size.  If there are no
     *      more results, the resultset will be empty.
     */
    public RESULTSET getNextResults(ServiceRegistry serviceRegistry);
}
