/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.queries;

import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
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
public class QueriesEntityResource implements EntityResourceAction.ReadById<CollectionWithPagingInfo<Node>>, InitializingBean
{
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
    public CollectionWithPagingInfo<Node> readById(String queryId, Parameters parameters)
    {
        return queries.findNodes(queryId, parameters);
    }
}
