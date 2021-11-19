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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.ContentStorageInformation;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
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

    @WebApiDescription(title = "Get storage properties",
            description = "Retrieves storage properties for given node's content",
            successStatus = HttpServletResponse.SC_OK)
    @Override
    public ContentStorageInfo readById(String entityResourceId, String id, Parameters parameters)
            throws RelationshipResourceNotFoundException
    {
        return storageInformation.getStorageInfo(entityResourceId, id, parameters);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "storageInformation", storageInformation);
    }
}
