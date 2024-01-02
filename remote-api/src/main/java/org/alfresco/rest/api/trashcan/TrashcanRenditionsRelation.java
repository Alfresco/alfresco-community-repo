/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.DirectAccessUrlHelper;
import org.alfresco.rest.api.model.DirectAccessUrlRequest;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "renditions", entityResource = TrashcanEntityResource.class, title = "Node renditions via archived node")
public class TrashcanRenditionsRelation
        implements RelationshipResourceAction.Read<Rendition>, RelationshipResourceAction.ReadById<Rendition>, RelationshipResourceBinaryAction.Read, InitializingBean
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

    @WebApiDescription(title = "List renditions", description = "List available (created) renditions")
    @Override
    public CollectionWithPagingInfo<Rendition> readAll(String nodeId, Parameters parameters)
    {
        return deletedNodes.getRenditions(nodeId, parameters);
    }

    @WebApiDescription(title = "Retrieve rendition information", description = "Retrieve (created) rendition information")
    @Override
    public Rendition readById(String nodeId, String renditionId, Parameters parameters)
    {
        return deletedNodes.getRendition(nodeId, renditionId, parameters);
    }

    @WebApiDescription(title = "Download archived node rendition", description = "Download rendition for an archived node")
    @BinaryProperties({ "content" })
    @Override
    public BinaryResource readProperty(String nodeId, String renditionId, Parameters parameters)
    {
        return deletedNodes.getContent(nodeId, renditionId, parameters);
    }

    @Operation ("request-direct-access-url")
    @WebApiParam (name = "directAccessUrlRequest", title = "Request direct access url", description = "Options for direct access url request", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request content url",
            description="Generates a direct access URL.",
            successStatus = HttpServletResponse.SC_OK)
    public DirectAccessUrl requestContentDirectUrl(String originalNodeId, String renditionId, DirectAccessUrlRequest directAccessUrlRequest, Parameters parameters, WithResponse withResponse)
    {
        boolean attachment = directAccessUrlHelper.getAttachment(directAccessUrlRequest);
        Long validFor = directAccessUrlHelper.getDefaultExpiryTimeInSec();
        String fileName = directAccessUrlHelper.getFileName(directAccessUrlRequest);
        DirectAccessUrl directAccessUrl;
        try
        {
            directAccessUrl = deletedNodes.requestContentDirectUrl(originalNodeId, renditionId, attachment, validFor, fileName);
        }
        catch (DirectAccessUrlDisabledException ex)
        {
            throw new DisabledServiceException(ex.getMessage());
        }
        return directAccessUrl;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("deletedNodes", this.deletedNodes);
    }
}
