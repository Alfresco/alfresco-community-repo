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

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.DirectAccessUrlHelper;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.FolderSizeImpl;
import org.alfresco.rest.api.model.*;
import org.alfresco.rest.framework.*;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;

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
        BinaryResourceAction.Read, BinaryResourceAction.Update<Node>, EntityResourceAction.RetrieveFolderSize<Map<String,Object>>, InitializingBean
{

    private static final Logger LOG = LoggerFactory.getLogger(NodesEntityResource.class);

    private static final String INVALID_NODEID = "Invalid parameter: value of nodeId is invalid";
    private static final String NODEID_NOT_FOUND = "Searched nodeId does not exist";
    private static final String STATUS = "status";
    private static final String COMPLETED = "Completed";
    private static final String FOLDER = "folder";
    private Nodes nodes;
    private DirectAccessUrlHelper directAccessUrlHelper;
    private PermissionService permissionService;
    private NodeService nodeService;
    private ActionService actionService;
    private SimpleCache<Serializable,Object> simpleCache;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setDirectAccessUrlHelper(DirectAccessUrlHelper directAccessUrlHelper)
    {
        this.directAccessUrlHelper = directAccessUrlHelper;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setSimpleCache(SimpleCache<Serializable, Object> simpleCache)
    {
        this.simpleCache = simpleCache;
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
    public Node unlock(String nodeId, Void ignore, Parameters parameters, WithResponse withResponse)
    {
        return nodes.unlock(nodeId, parameters);
    }

    @Operation("request-direct-access-url")
    @WebApiParam(name = "directAccessUrlRequest", title = "Request direct access url", description = "Options for direct access url request", kind = ResourceParameter.KIND.HTTP_BODY_OBJECT)
    @WebApiDescription(title = "Request content url",
            description="Generates a direct access URL.",
            successStatus = HttpServletResponse.SC_OK)
    public DirectAccessUrl requestContentDirectUrl(String nodeId, DirectAccessUrlRequest directAccessUrlRequest, Parameters parameters, WithResponse withResponse)
    {
        boolean attachment = directAccessUrlHelper.getAttachment(directAccessUrlRequest);
        Long validFor = directAccessUrlHelper.getDefaultExpiryTimeInSec();
        String fileName = directAccessUrlHelper.getFileName(directAccessUrlRequest);
        NodeRef nodeRef = nodes.validateNode(nodeId);

        DirectAccessUrl directAccessUrl;
        try
        {
            directAccessUrl = nodes.requestContentDirectUrl(nodeRef, attachment, validFor, fileName);
        }
        catch (DirectAccessUrlDisabledException ex)
        {
            throw new DisabledServiceException(ex.getMessage());
        }
        return directAccessUrl;
    }

    /**
     * Folder Size - returns size of a folder.
     *
     * @param nodeId String id of folder - will also accept well-known alias, eg. -root- or -my- or -shared-
     *               Please refer to OpenAPI spec for more details !
     * Returns the executionId which shows pending action, which can be used in a
     * GET/calculateSize endpoint to check if the action's status has been completed, comprising the size of the node in bytes.
     *               <p>
     *               If NodeId does not exist, EntityNotFoundException (status 404).
     *               If nodeId does not represent a folder, InvalidNodeTypeException (status 422).
     */
    @Operation("calculate-folder-size")
    @WebApiDescription(title = "Calculating Folder Size", description = "Calculating size of a folder", successStatus = Status.STATUS_ACCEPTED)
    @WebApiParameters({@WebApiParam(name = "nodeId", title = "The unique id of Execution Job", description = "A single nodeId")})
    public Map<String, Object> calculateFolderSize(String nodeId, Void ignore, Parameters parameters, WithResponse withResponse)
    {
        NodeRef nodeRef = nodes.validateNode(nodeId);
        int maxItems = Math.min(parameters.getPaging().getMaxItems(), 1000);
        QName qName = nodeService.getType(nodeRef);
        Map<String, Object> result = new HashMap<>();
        validatePermissions(nodeRef, nodeId);

        if(!FOLDER.equalsIgnoreCase(qName.getLocalName()))
        {
            throw new InvalidNodeTypeException(INVALID_NODEID);
        }

        try
        {
            FolderSizeImpl folderSizeImpl = new FolderSizeImpl(actionService);
            return folderSizeImpl.executingAsynchronousFolderAction(nodeRef, maxItems, result, simpleCache);
        }
        catch (Exception alfrescoRuntimeError)
        {
            LOG.error("Exception occurred in NodesEntityResource:createById {}", alfrescoRuntimeError.getMessage());
            throw new AlfrescoRuntimeException("Exception occurred in NodesEntityResource:createById",alfrescoRuntimeError);
        }
    }
    @Override
    @WebApiDescription(title = "Returns Folder Node Size", description = "Returning a Folder Node Size")
    @WebApiParameters({@WebApiParam(name = "nodeId", title = "The unique id of Execution Job", description = "A single nodeId")})
    @BinaryProperties({"get-folder-size"})
    public Map<String, Object> getFolderSize(String nodeId) throws EntityNotFoundException
    {
        try
        {
            LOG.debug("Retrieving OUTPUT from NodeSizeActionExecutor - NodesEntityResource:readById");
            NodeRef nodeRef = nodes.validateNode(nodeId);
            validatePermissions(nodeRef, nodeId);
            QName qName = nodeService.getType(nodeRef);

            if(!FOLDER.equalsIgnoreCase(qName.getLocalName()))
            {
                throw new InvalidNodeTypeException(INVALID_NODEID);
            }

            Object cachedResult = simpleCache.get(nodeId);
            if(cachedResult != null)
            {
                return getResult(cachedResult);
            }
            else
            {
                throw new NotFoundException(NODEID_NOT_FOUND);
            }
        }
        catch (Exception ex)
        {
            LOG.error("Exception occurred in NodesEntityResource:readById {}", ex.getMessage());
            throw ex; // Rethrow with original stack trace
        }
    }

    private Map<String, Object> getResult(Object outputResult)
    {
        Map<String, Object> result = new HashMap<>();

        if (outputResult instanceof Map)
        {
            Map<String, Object> mapResult = (Map<String, Object>) outputResult;
            mapResult.put(STATUS, COMPLETED);
            result = mapResult;
        }
        else if(outputResult instanceof String)
        {
            result.put(STATUS, outputResult);
        }
        return result;
    }

    private void validatePermissions(NodeRef nodeRef, String nodeId)
    {
        Node nodeInfo = nodes.getNode(nodeId);
        NodePermissions nodePerms = nodeInfo.getPermissions();

        // Validate permissions.
        if (nodePerms != null && permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
        {
            throw new AccessDeniedException("permissions.err_access_denied");
        }
    }
}

