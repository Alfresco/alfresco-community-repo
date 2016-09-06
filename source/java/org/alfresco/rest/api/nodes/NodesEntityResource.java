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

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.LockInfo;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.api.model.UnlockInfo;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Node (file or folder)
 *
 * @author sglover
 * @author Gethin James
 * @author janv
 */
@EntityResource(name="nodes", title = "Nodes")
public class NodesEntityResource implements
        EntityResourceAction.ReadById<Node>, EntityResourceAction.Delete, EntityResourceAction.Update<Node>,
        BinaryResourceAction.Read, BinaryResourceAction.Update<Node>, InitializingBean
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
     * @param nodeId String id of node (folder or document) - will also accept well-known aliases, eg. "-root-", "-my-", "-shared-"
     * 
     * Optional parameters:
     * - path
     */
    @WebApiDescription(title = "Get Node Information", description = "Get information for the node with id 'nodeId'")
    @WebApiParam(name = "nodeId", title = "The node id")
    public Node readById(String nodeId, Parameters parameters)
    {
    	return nodes.getFolderOrDocument(nodeId, parameters);
    }

    /**
     * Download content
     * 
     * @param fileNodeId
     * @param parameters {@link Parameters}
     * @return
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download content", description = "Download content")
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String fileNodeId, Parameters parameters) throws EntityNotFoundException
    {
        return nodes.getContent(fileNodeId, parameters, true);
    }

    /**
     * Upload new version of content
     * 
     * This allow binary content update of an existing file/content node.
     * 
     * Note: alternatively, can upload via POST (multipart/form-data) with existing file name and form "overwrite=true".
     * 
     * @param fileNodeId
     * @param contentInfo Basic information about the content stream
     * @param stream An inputstream
     * @param parameters
     * @return
     */
    @Override
    @WebApiDescription(title = "Upload content", description = "Upload content")
    @BinaryProperties({"content"})
    public Node updateProperty(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        return nodes.updateContent(fileNodeId, contentInfo, stream, parameters);
    }

    /**
     * Update info on the node 'nodeId' - folder or document
     *
     * Can update name (which is a "rename" and hence must be unique within the current parent folder)
     * or update other properties.
     *
     * @param nodeId  String nodeId of node (folder or document)
     * @param nodeInfo node entity with info to update (eg. name, properties ...)
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
        nodes.deleteNode(nodeId, parameters);
    }

    @Operation("copy")
    @WebApiDescription(title = "Copy Node", description="Copy one or more nodes (files or folders) to a new target folder, with option to rename.")
    public Node copyById(String nodeId, NodeTarget target, Parameters parameters, WithResponse withResponse)
    {
       return nodes.moveOrCopyNode(nodeId, target.getTargetParentId(), target.getName(), parameters, true);
    }

    @Operation("move")
    @WebApiDescription(title = "Move Node",
            description="Moves one or more nodes (files or folders) to a new target folder, with option to rename.",
            successStatus = HttpServletResponse.SC_OK)
    public Node moveById(String nodeId, NodeTarget target, Parameters parameters, WithResponse withResponse)
    {
        return nodes.moveOrCopyNode(nodeId, target.getTargetParentId(), target.getName(), parameters, false);
    }
    
    @Operation("lock")
    @WebApiDescription(title = "Lock Node",
            description="Places a lock on a node.",
            successStatus = HttpServletResponse.SC_OK)
    public Node lock(String nodeId, LockInfo lockInfo, Parameters parameters, WithResponse withResponse)
    {
        return nodes.lock(nodeId, lockInfo, parameters);
    }
    
    @Operation("unlock")
    @WebApiDescription(title = "Unlock Node",
            description="Removes a lock on a node.",
            successStatus = HttpServletResponse.SC_OK)
    public Node unlock(String nodeId, UnlockInfo unlockInfo, Parameters parameters, WithResponse withResponse)
    {
        return nodes.unlock(nodeId, unlockInfo, parameters);
    }

}

