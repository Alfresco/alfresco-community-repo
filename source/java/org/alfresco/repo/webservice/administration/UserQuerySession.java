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
package org.alfresco.repo.webservice.administration;

import org.alfresco.repo.webservice.AbstractQuerySession;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.service.ServiceRegistry;

/**
 * A session for managing user-related queries.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class UserQuerySession extends AbstractQuerySession<UserQueryResults, UserDetails>
{
    private static final long serialVersionUID = 1823253197962982642L;

    public UserQuerySession(long maxResults, long batchSize, ServerQuery<UserQueryResults> query)
    {
        super(maxResults, batchSize, query);
    }

    @Override
    protected UserDetails[] makeArray(int size)
    {
        return new UserDetails[size];
    }

    public UserQueryResults getNextResults(ServiceRegistry serviceRegistry)
    {
        UserQueryResults queryResults = getQueryResults(serviceRegistry);
        UserDetails[] allRows = queryResults.getUserDetails();
        UserDetails[] batchedRows = getNextResults(allRows);
        // Build the user query results
        UserQueryResults batchedResults = new UserQueryResults();
//        batchedResults.setQuerySession(getId());  TODO: http://issues.alfresco.com/browse/AR-1689

        batchedResults.setUserDetails(batchedRows);
        // Done
        return batchedResults;
    }
}
