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