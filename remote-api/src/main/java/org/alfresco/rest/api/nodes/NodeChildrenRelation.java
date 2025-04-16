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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.servlet.FormData;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.util.ParameterCheck;

/**
 * Node Children
 *
 * - list folder children - create folder &/or empty file - create (ie. upload) file with content
 * 
 * @author janv
 * @author Jamal Kaabi-Mofrad
 */
@RelationshipResource(name = "children", entityResource = NodesEntityResource.class, title = "Folder children")
public class NodeChildrenRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.Create<Node>,
        MultiPartRelationshipResourceAction.Create<Node>, InitializingBean
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
     * List folder children - returns a filtered/sorted/paged list of nodes that are immediate children of the parent folder
     *
     * @param parentFolderNodeId
     *            String id of parent folder - will also accept well-known alias, eg. -root- or -my- or -shared-
     * 
     *            Optional query parameters:
     *
     *            - include - fields - where - orderBy - skipCount - maxItems - ... etc
     * 
     *            Please refer to OpenAPI spec for more details !
     * 
     *            If parentFolderNodeId does not exist, EntityNotFoundException (status 404). If parentFolderNodeId does not represent a folder, InvalidArgumentException (status 400).
     */
    @Override
    @WebApiDescription(title = "Return a paged list of nodes for the document/folder identified by parentFolderNodeId")
    public CollectionWithPagingInfo<Node> readAll(String parentFolderNodeId, Parameters parameters)
    {
        return nodes.listChildren(parentFolderNodeId, parameters);
    }

    /**
     * Create one or more nodes (folder or empty file) below parent folder.
     *
     * Note: for parent folder nodeId, can also use well-known alias, eg. -root- or -my- or -shared-
     *
     * If parentFolderNodeId does not exist, EntityNotFoundException (status 404). If parentFolderNodeId does not represent a folder, InvalidArgumentException (status 400).
     */
    @Override
    @WebApiDescription(title = "Create one (or more) nodes as children of folder identified by parentFolderNodeId")
    public List<Node> create(String parentFolderNodeId, List<Node> nodeInfos, Parameters parameters)
    {
        List<Node> result = new ArrayList<>(nodeInfos.size());

        for (Node nodeInfo : nodeInfos)
        {
            result.add(nodes.createNode(parentFolderNodeId, nodeInfo, parameters));
        }

        return result;
    }

    @Override
    @WebApiDescription(title = "Upload file content and meta-data into the repository.")
    @WebApiParam(name = "formData", title = "A single form data", description = "A single form data which holds FormFields.")
    public Node create(String parentFolderNodeId, FormData formData, Parameters parameters, WithResponse withResponse)
    {
        return nodes.upload(parentFolderNodeId, formData, parameters);
    }

}
