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
package org.alfresco.repo.webservice.repository;

import org.alfresco.repo.webservice.AbstractQuerySession;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.service.ServiceRegistry;

/**
 * A query session for use with {@linkplain RepositoryWebService node-related queries} against the
 * repository.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class RepositoryQuerySession extends AbstractQuerySession<ResultSet, ResultSetRow>
{
    private static final long serialVersionUID = -3621997639261137000L;

    public RepositoryQuerySession(long maxResults, long batchSize, ServerQuery<ResultSet> query)
    {
        super(maxResults, batchSize, query);
    }

    @Override
    protected ResultSetRow[] makeArray(int size)
    {
        return new ResultSetRow[size];
    }

    public ResultSet getNextResults(ServiceRegistry serviceRegistry)
    {
        ResultSet queryResults = getQueryResults(serviceRegistry);
        ResultSetRow[] allRows = queryResults.getRows();
        ResultSetRow[] batchedRows = getNextResults(allRows);
        // Build the resultset for the batched results
        ResultSet batchedResults = new ResultSet();
        batchedResults.setMetaData(queryResults.getMetaData());
        batchedResults.setRows(batchedRows);
        batchedResults.setTotalRowCount(batchedRows.length);
        // Done
        return batchedResults;
    }
}
