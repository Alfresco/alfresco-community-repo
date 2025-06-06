/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

package org.alfresco.rest.api.nodes;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;

import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.rest.api.DirectAccessUrlHelper;
import org.alfresco.rest.api.Renditions;
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.PropertyCheck;

/**
 *
 * Node version renditions
 *
 * - GET /nodes/{nodeId}/versions/{versionId}/renditions - POST /nodes/{nodeId}/versions/{versionId}/renditions - GET /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId} - DELETE /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId} - GET /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/content - POST /nodes/{nodeId}/versions/{versionId}/renditions/{renditionId}/request-direct-access-url
 *
 * @author janv
 */
@RelationshipResource(name = "renditions", entityResource = NodeVersionsRelation.class, title = "Node version renditions")
public class NodeVersionRenditionsRelation implements RelationshipResourceAction.Read<Rendition>,
        RelationshipResourceAction.ReadById<Rendition>,
        RelationshipResourceAction.Create<Rendition>,
        RelationshipResourceAction.Delete,
        RelationshipResourceBinaryAction.Read,
        InitializingBean
{
    private Renditions renditions;
    private DirectAccessUrlHelper directAccessUrlHelper;

    public void setRenditions(Renditions renditions)
    {
        this.renditions = renditions;
    }

    public void setDirectAccessUrlHelper(DirectAccessUrlHelper directAccessUrlHelper)
    {
        this.directAccessUrlHelper = directAccessUrlHelper;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "renditions", renditions);
    }

    @Override
    public CollectionWithPagingInfo<Rendition> readAll(String nodeId, Parameters parameters)
    {
        String versionId = parameters.getRelationshipId();

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return renditions.getRenditions(nodeRef, versionId, parameters);
    }

    @Override
    public Rendition readById(String nodeId, String versionId, Parameters parameters)
    {
        String renditionId = parameters.getRelationship2Id();

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return renditions.getRendition(nodeRef, versionId, renditionId, parameters);
    }

    @WebApiDescription(title = "Create rendition", successStatus = Status.STATUS_ACCEPTED)
    @Override
    public List<Rendition> create(String nodeId, List<Rendition> entity, Parameters parameters)
    {
        String versionId = parameters.getRelationshipId();

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        renditions.createRenditions(nodeRef, versionId, entity, parameters);
        return null;
    }

    @WebApiDescription(title = "Download rendition", description = "Download rendition")
    @BinaryProperties({"content"})
    @Override
    public BinaryResource readProperty(String nodeId, String versionId, Parameters parameters)
    {
        String renditionId = parameters.getRelationship2Id();

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return renditions.getContent(nodeRef, versionId, renditionId, parameters);
    }

    @Operation("request-direct-access-url")
    @WebApiParam(name = "directAccessUrlRequest",
            title = "Request direct access url",
            description = "Options for direct access url request",
            kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request content url",
            description = "Generates a direct access URL.",
            successStatus = HttpServletResponse.SC_OK)
    public DirectAccessUrl requestContentDirectUrl(String nodeId,
            String versionId,
            DirectAccessUrlRequest directAccessUrlRequest,
            Parameters parameters, WithResponse withResponse)
    {
        boolean attachment = directAccessUrlHelper.getAttachment(directAccessUrlRequest);
        Long validFor = directAccessUrlHelper.getDefaultExpiryTimeInSec();
        String fileName = directAccessUrlHelper.getFileName(directAccessUrlRequest);
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        String renditionId = parameters.getRelationship2Id();

        DirectAccessUrl directAccessUrl;
        try
        {
            directAccessUrl = renditions.requestContentDirectUrl(nodeRef, versionId, renditionId, attachment, validFor, fileName);
        }
        catch (DirectAccessUrlDisabledException ex)
        {
            throw new DisabledServiceException(ex.getMessage());
        }
        return directAccessUrl;
    }

    @WebApiDescription(title = "Delete rendition")
    @Override
    public void delete(String nodeId, String versionId, Parameters parameters)
    {
        String renditionId = parameters.getRelationship2Id();

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        renditions.deleteRendition(nodeRef, versionId, renditionId, parameters);
    }
}
