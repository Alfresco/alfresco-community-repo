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
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletResponse;

/**
 * Node storage information.
 * Note: Currently marked as experimental and subject to change.
 *
 * @author mpichura
 */
@Experimental
@RelationshipResource(name = "storage-info", entityResource = NodesEntityResource.class, title = "Node's content storage information")
public class NodeStorageInfoRelation implements RelationshipResourceAction.ReadById<ContentStorageInfo>, InitializingBean
{

    private final ContentStorageInformation storageInformation;

    public NodeStorageInfoRelation(ContentStorageInformation storageInformation)
    {
        this.storageInformation = storageInformation;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "storageInformation", storageInformation);
    }

    @WebApiDescription(title = "Get storage properties",
            description = "Retrieves storage properties for given node's content",
            successStatus = HttpServletResponse.SC_OK)
    @Override
    public ContentStorageInfo readById(String nodeId, String contentPropName, Parameters parameters)
            throws RelationshipResourceNotFoundException
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        return storageInformation.getStorageInfo(nodeRef, contentPropName, parameters);
    }

    @Experimental
    @Operation("archive")
    @WebApiParam(name = "archiveContentRequest", title = "Request for archive content",
            description = "Optional parameters for archive content", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request send content to archive",
            description = "Submits a request to send content to archive",
            successStatus = HttpServletResponse.SC_OK)
    public void requestArchiveContent(String nodeId, String contentPropName, ArchiveContentRequest archiveContentRequest, Parameters parameters,
                                      WithResponse withResponse)
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        final boolean result = storageInformation.requestArchiveContent(nodeRef, contentPropName, archiveContentRequest);
        if (result)
        {
            withResponse.setStatus(HttpServletResponse.SC_OK);
        } else
        {
            withResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    @Experimental
    @Operation("archive-restore")
    @WebApiParam(name = "restoreArchivedContentRequest", title = "Request for restore content from archive",
            description = "Optional parameters for restore content from archive", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request restore content from archive",
            description = "Submits a request to restore content from archive",
            successStatus = HttpServletResponse.SC_ACCEPTED)
    public void requestRestoreContentFromArchive(String nodeId, String contentPropName, RestoreArchivedContentRequest restoreArchivedContentRequest,
                                                 Parameters parameters, WithResponse withResponse)
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        final boolean result = storageInformation.requestRestoreContentFromArchive(nodeRef, contentPropName, restoreArchivedContentRequest);
        if (result)
        {
            withResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
        } 
        else
        {
            withResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
}
