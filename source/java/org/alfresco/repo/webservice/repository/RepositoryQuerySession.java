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
package org.alfresco.repo.webservice.repository;

import org.alfresco.repo.webservice.AbstractQuerySession;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static Log logger = LogFactory.getLog(RepositoryQuerySession.class);

    public RepositoryQuerySession(long maxResults, long batchSize, ServerQuery<ResultSet> query)
    {
        super(maxResults, batchSize, query);
    }

    @Override
    protected ResultSetRow[] makeArray(int size)
    {
        return new ResultSetRow[size];
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.QuerySession#getNextResults(org.alfresco.service.ServiceRegistry)
     */
    public ResultSet getNextResults(ServiceRegistry serviceRegistry)
    {
        ResultSet queryResults = getQueryResults(serviceRegistry);
        ResultSetRow[] allRows = queryResults.getRows();
        ResultSetRow[] batchedRows = getNextResults(allRows);
        // Build the resultset for the batched results
        ResultSet batchedResults = new ResultSet();
        batchedResults.setMetaData(queryResults.getMetaData());
        batchedResults.setRows(batchedRows);
        batchedResults.setTotalRowCount(allRows.length); 
        logger.debug("total row count :"+allRows.length);
        // Done
        return batchedResults;
    }
}
