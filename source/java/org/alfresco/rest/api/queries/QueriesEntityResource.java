/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.queries;

import org.alfresco.rest.api.Queries;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for Queries.
 *
 * @author janv
 */
@EntityResource(name="queries", title = "Queries")
public class QueriesEntityResource implements
        EntityResourceAction.ReadById<CollectionWithPagingInfo<? extends Object>>,
        InitializingBean
{
    private final static String QUERY_LIVE_SEARCH_NODES = "live-search-nodes";
    private final static String QUERY_LIVE_SEARCH_PEOPLE = "live-search-people";

    private Queries queries;

    public void setQueries(Queries queries)
    {
        this.queries = queries;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("queries", this.queries);
    }

    // hmm - a little unorthodox
    @Override
    @WebApiDescription(title="Find results", description = "Find & list search results for given query id")
    public CollectionWithPagingInfo<? extends Object> readById(String queryId, Parameters parameters)
    {
        switch (queryId)
        {
        case QUERY_LIVE_SEARCH_NODES:
            return queries.findNodes(parameters);
        case QUERY_LIVE_SEARCH_PEOPLE:
            return queries.findPeople(parameters);
        default:
            throw new NotFoundException(queryId);
        }
    }
}
