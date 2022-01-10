/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.ContentStorageInformation;
import org.alfresco.rest.api.model.ArchiveContentRequest;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.api.model.RestoreArchivedContentRequest;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletResponse;

/**
 * Node Versions storage information.
 *
 *  - GET  /nodes/{nodeId}/versions/{versionId}/storage-info/{contentPropQNameId}
 * 
 * Note: Currently marked as experimental and subject to change.
 *
 * @author janv
 */
@Experimental
@RelationshipResource(name = "storage-info", entityResource = NodeVersionsRelation.class, title = "Node Version's content storage information")
public class NodeVersionsStorageInfoRelation implements RelationshipResourceAction.ReadById<ContentStorageInfo>, InitializingBean
{
    private final ContentStorageInformation storageInformation;
    private NodeVersionsRelation nodeVersions;

    public NodeVersionsStorageInfoRelation(ContentStorageInformation storageInformation, NodeVersionsRelation nodeVersions)
    {
        this.storageInformation = storageInformation;
        this.nodeVersions = nodeVersions;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "storageInformation", storageInformation);
        PropertyCheck.mandatory(this, "nodeVersions", nodeVersions);
    }

    @WebApiDescription(title = "Get storage properties",
            description = "Retrieves storage properties for given node version content",
            successStatus = HttpServletResponse.SC_OK)
    @Override
    public ContentStorageInfo readById(String nodeId, String versionId, Parameters parameters)
            throws RelationshipResourceNotFoundException
    {
        String contentPropQNameId = parameters.getRelationship2Id();

        NodeRef versionNodeRef = findVersionNodeRef(nodeId, versionId);

        return storageInformation.getStorageInfo(versionNodeRef, contentPropQNameId, parameters);
    }

    @Experimental
    @Operation("archive")
    @WebApiParam(name = "archiveContentRequest", title = "Request for archive version content",
            description = "Optional parameters for archive version content", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request send version content to archive",
            description = "Submits a request to send version content to archive",
            successStatus = HttpServletResponse.SC_OK)
    public void requestArchiveContent(String nodeId, String versionId, ArchiveContentRequest archiveContentRequest, Parameters parameters,
                                      WithResponse withResponse) 
    {
        String contentPropQNameId = parameters.getRelationship2Id();

        NodeRef versionNodeRef = findVersionNodeRef(nodeId, versionId);

        final boolean result = storageInformation.requestArchiveContent(versionNodeRef, contentPropQNameId, archiveContentRequest);
        if (result)
        {
            withResponse.setStatus(HttpServletResponse.SC_OK);
        } 
        else
        {
            withResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    @Experimental
    @Operation("archive-restore")
    @WebApiParam(name = "restoreArchivedContentRequest", title = "Request for restore version content from archive",
            description = "Optional parameters for restore version content from archive", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request restore version content from archive",
            description = "Submits a request to restore version content from archive",
            successStatus = HttpServletResponse.SC_ACCEPTED)
    public void requestRestoreContentFromArchive(String nodeId, String versionId, RestoreArchivedContentRequest restoreArchivedContentRequest,
                                                 Parameters parameters, WithResponse withResponse)
    {
        String contentPropQNameId = parameters.getRelationship2Id();

        NodeRef versionNodeRef = findVersionNodeRef(nodeId, versionId);

        final boolean result = storageInformation.requestRestoreContentFromArchive(versionNodeRef, contentPropQNameId, restoreArchivedContentRequest);
        if (result)
        {
            withResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else
        {
            withResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private NodeRef findVersionNodeRef(String nodeId, String versionId)
    {
        Version version = nodeVersions.findVersion(nodeId, versionId);
        if (version == null)
        {
            throw new EntityNotFoundException(nodeId+"-"+versionId);
        }
        return version.getFrozenStateNodeRef();
    }
}
