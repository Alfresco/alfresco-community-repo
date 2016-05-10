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

package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Queries API
 *
 * @author janv
 */
public interface Queries
{
    /**
     * Find Nodes
     *
     * @param queryId currently expects "live-search-nodes"
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return the search query results
     */
    CollectionWithPagingInfo<Node> findNodes(String queryId, Parameters parameters);

    String PARAM_TERM = "term";
    String PARAM_ROOT_NODE_ID = "rootNodeId";
    String PARAM_NODE_TYPE = "nodeType";
}
