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

import org.alfresco.rest.api.DeletedNodes;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "renditions", entityResource = TrashcanEntityResource.class, title = "Node renditions via archived node")
public class TrashcanRenditionsRelation
        implements RelationshipResourceAction.Read<Rendition>, RelationshipResourceAction.ReadById<Rendition>, RelationshipResourceBinaryAction.Read, InitializingBean
{

    private DeletedNodes deletedNodes;

    public void setDeletedNodes(DeletedNodes deletedNodes)
    {
        this.deletedNodes = deletedNodes;
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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("deletedNodes", this.deletedNodes);
    }
}
