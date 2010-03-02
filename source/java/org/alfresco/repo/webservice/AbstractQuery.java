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

import java.util.Set;

import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Abstract implementation of a QuerySession providing support for automatic id generation
 * and provides support for paging through query results.
 * <p>
 * Before executing, all the services need to be set.
 * 
 * @author gavinc
 */
public abstract class AbstractQuery<RESULTSET> implements ServerQuery<RESULTSET>
{
    public AbstractQuery()
    {
    }
    
    /**
     * Override this method only if the query can limit the results without a post-query cut-off.
     * 
     * {@inheritDoc}
     */
    public RESULTSET execute(ServiceRegistry serviceRegistry, long maxResults)
    {
        return execute(serviceRegistry);
    }

    /**
     * Create a result set row node object for the provided node reference
     * 
     * @param nodeRef
     *      the node reference
     * @param nodeService
     *      the node service
     * @return
     *      the result set row node
     */
    protected ResultSetRowNode createResultSetRowNode(NodeRef nodeRef, NodeService nodeService)
    {
        // Get the type
        String type = nodeService.getType(nodeRef).toString();

        // Get the aspects applied to the node
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        String[] aspectNames = new String[aspects.size()];
        int index = 0;
        for (QName aspect : aspects)
        {
            aspectNames[index] = aspect.toString();
            index++;
        }

        // Create and return the result set row node
        return new ResultSetRowNode(nodeRef.getId(), type, aspectNames);
    }
}