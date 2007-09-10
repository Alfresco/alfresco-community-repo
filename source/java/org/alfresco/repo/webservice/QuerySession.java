/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
