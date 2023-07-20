/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.DirectAccessUrlHelper;
import org.alfresco.rest.api.model.DirectAccessUrlRequest;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeTargetAssoc;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.DirectAccessUrl;

/**
 * An implementation of an Entity Resource for handling archived content
 *
 * @author Gethin James
 */
@EntityResource(name="deleted-nodes", title = "Deleted Nodes")
public class TrashcanEntityResource implements
        EntityResourceAction.ReadById<Node>, EntityResourceAction.Read<Node>, EntityResourceAction.Delete, BinaryResourceAction.Read
{
    private DeletedNodes deletedNodes;
    private DirectAccessUrlHelper directAccessUrlHelper;

    public void setDeletedNodes(DeletedNodes deletedNodes)
    {
        this.deletedNodes = deletedNodes;
    }

    public void setDirectAccessUrlHelper(DirectAccessUrlHelper directAccessUrlHelper)
    {
        this.directAccessUrlHelper = directAccessUrlHelper;
    }

    @Override
    public CollectionWithPagingInfo<Node> readAll(Parameters params)
    {
        return deletedNodes.listDeleted(params);
    }

    @Override
    public Node readById(String nodeId, Parameters parameters) throws EntityNotFoundException
    {
        return deletedNodes.getDeletedNode(nodeId, parameters, true, null);
    }

    @Operation("restore")
    @WebApiDescription(title = "Restore deleted Node", description = "Restores an archived node", successStatus = HttpServletResponse.SC_OK)
    @WebApiParam(name = "nodeAssocTarget", title = "Target parent id and association type", description = "Target parent id and association type", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT, required = false)
    public Node restoreDeletedNode(String nodeId, NodeTargetAssoc nodeTargetAssoc, Parameters parameters, WithResponse withResponse)
    {
        return deletedNodes.restoreArchivedNode(nodeId, nodeTargetAssoc);
    }

    @Override
    @WebApiDescription(title = "Download content", description = "Download content")
    @BinaryProperties({ "content" })
    public BinaryResource readProperty(String nodeId, Parameters parameters)
    {
        return deletedNodes.getContent(nodeId, null, parameters);
    }

    @Operation("request-direct-access-url")
    @WebApiParam(name = "directAccessUrlRequest", title = "Request direct access url", description = "Options for direct access url request", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request content url",
            description="Generates a direct access URL.",
            successStatus = HttpServletResponse.SC_OK)
    public DirectAccessUrl requestContentDirectUrl(String originalNodeId, DirectAccessUrlRequest directAccessUrlRequest, Parameters parameters, WithResponse withResponse)
    {
        boolean attachment = directAccessUrlHelper.getAttachment(directAccessUrlRequest);
        Long validFor = directAccessUrlHelper.getDefaultExpiryTimeInSec();
        String fileName = directAccessUrlHelper.getFileName(directAccessUrlRequest);
        DirectAccessUrl directAccessUrl;
        try
        {
            directAccessUrl = deletedNodes.requestContentDirectUrl(originalNodeId, null, attachment, validFor, fileName);
        }
        catch (DirectAccessUrlDisabledException ex)
        {
            throw new DisabledServiceException(ex.getMessage());
        }
        return directAccessUrl;
    }

    @Override
    public void delete(String nodeId, Parameters parameters)
    {
        deletedNodes.purgeArchivedNode(nodeId);
    }
}
