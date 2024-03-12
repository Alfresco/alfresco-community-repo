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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Node
 *
 * - folder size
 *
 * @author Mohit Singh
 */
@RelationshipResource(name = "size",  entityResource = NodesEntityResource.class, title = "Folder size")
public class NodeFolderSizeRelation implements
        RelationshipResourceAction.FolderSize<Map<String, Object>>, InitializingBean
{
    private Nodes nodes;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    /**
     * Folder Size - returns a size of folder.
     *
     * @param nodeId String id of folder - will also accept well-known alias, eg. -root- or -my- or -shared-
     * Please refer to OpenAPI spec for more details !
     *
     * If NodeId does not exist, EntityNotFoundException (status 404).
     * If NodeId does not represent a folder, InvalidArgumentException (status 400).
     */
    @Override
    @WebApiDescription(title = "Size of folder",description = "Return a size of folder")
    public Map<String, Object> readById(String nodeId)
    {
        return nodes.getFolderSize(nodeId);
    }

}
