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
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Node
 *
 * @author sglover
 * @author Gethin James
 * @author janv
 */
@EntityResource(name="nodes", title = "Nodes")
public class NodesEntityResource implements EntityResourceAction.ReadById<Node>,  EntityResourceAction.Delete, EntityResourceAction.Update<Node>, InitializingBean
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
     * Returns information regarding the node 'nodeId' - folder or document
     * 
     * TODO other metadata/properties & permissions etc ...
     * 
     * @param nodeId String id of node (folder or document) - will also accept well-known aliases, eg. "-root-" or "-my-"
     * 
     * Optional parameters:
     * - path
     * - incPrimaryPath
     */
    @WebApiDescription(title = "Get Node Information", description = "Get information for the node with id 'nodeId'")
    @WebApiParam(name = "nodeId", title = "The node id")
    public Node readById(String nodeId, Parameters parameters)
    {
    	return nodes.getFolderOrDocument(nodeId, parameters);
    }

    /**
     * Update info on the node 'nodeId' - folder or document
     *
     * Initially, name, title &/or description. Note: changing name is a "rename" (and must be unique within the current parent folder).
     *
     * TODO other metadata/properties & permissions etc ...
     *
     * @param nodeId  String nodeId of node (folder or document)
     * @param nodeInfo node entity with info to update (eg. name, title, description ...)
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title="Updates a node (file or folder) with id 'nodeId'")
    public Node update(String nodeId, Node nodeInfo, Parameters parameters)
    {
        return nodes.updateNode(nodeId, nodeInfo, parameters);
    }
    
    /**
     * Delete the given node. Note: will cascade delete for a folder.
     * 
     * @param nodeId String id of node (folder or document)
     */
    @Override
    @WebApiDescription(title = "Delete Node", description="Delete the file or folder with id 'nodeId'. Folder will cascade delete")
    public void delete(String nodeId, Parameters parameters)
    {
        nodes.deleteNode(nodeId);
    }
}
