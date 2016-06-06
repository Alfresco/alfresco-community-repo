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

package org.alfresco.rest.api.trashcan;


import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.archive.ArchivedNodesCannedQueryBuilder;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of an Entity Resource for handling archived content
 *
 * @author Gethin James
 */
@EntityResource(name="deleted-nodes", title = "Deleted Nodes")
public class TrashcanEntityResource implements
        EntityResourceAction.ReadById<Node>, EntityResourceAction.Read<Node>, EntityResourceAction.Delete
{
    private DeletedNodes deletedNodes;

    public void setDeletedNodes(DeletedNodes deletedNodes)
    {
        this.deletedNodes = deletedNodes;
    }

    @Override
    public CollectionWithPagingInfo<Node> readAll(Parameters params)
    {
        return deletedNodes.listDeleted(params);
    }

    @Override
    public Node readById(String nodeId, Parameters parameters) throws EntityNotFoundException
    {
        return deletedNodes.getDeletedNode(nodeId, parameters);
    }

    @Operation("restore")
    @WebApiDescription(title = "Restore deleted Node", description="Restores an archived node",successStatus = HttpServletResponse.SC_OK)
    public Node restoreDeletedNode(String nodeId, Void ignored, Parameters parameters, WithResponse withResponse)
    {
        return deletedNodes.restoreArchivedNode(nodeId);
    }

    @Override
    public void delete(String nodeId, Parameters parameters)
    {
        deletedNodes.purgeArchivedNode(nodeId);
    }
}
