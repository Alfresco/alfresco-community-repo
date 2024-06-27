/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import org.alfresco.model.FolderSizeModel;
import org.alfresco.repo.action.executer.NodeSizeActionExecuter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *   NodeFolderSizeRelation
 *   Calculating and Retrieving the folder size
 */

@RelationshipResource(name = "calculateSize",  entityResource = NodesEntityResource.class, title = "Calculate size")
public class NodeFolderSizeRelation implements RelationshipResourceAction.CalculateSize<Map<String, Object>>,RelationshipResourceAction.ReadById<Map<String, Object>>, InitializingBean
{
    private Nodes nodes;
    private SearchService searchService;
    private ServiceRegistry serviceRegistry;
    private PermissionService permissionService;
    private NodeService nodeService;
    private ActionService actionService;
    private static final String NOT_A_VALID_NODEID = "Node Id does not refer to a valid type [should be of folder type]";

    /**
     * The logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(NodeFolderSizeRelation.class);

    /**
     * The class that wraps the ReST APIs from core.
     */

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.permissionService = serviceRegistry.getPermissionService();
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("nodes", this.nodes);
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
     *               If nodeId does not represent a folder, InvalidNodeTypeException (status 400).
     */
    @Override
    @WebApiDescription(title = "Calculating Folder Size", description = "Calculating size of a folder", successStatus = Status.STATUS_ACCEPTED)
    public Map<String, Object> createById(String nodeId, Parameters params)
    {
        NodeRef nodeRef = nodes.validateNode(nodeId);
        Map<String, Object> resetFolderOutput = new HashMap<>();
        int maxItems = params.getPaging().getMaxItems();
        QName qName = nodeService.getType(nodeRef);
        Map<String, Object> result = new HashMap<>();
        resetFolderOutput.put("status","IN-PROGRESS");
        nodeService.setProperty(nodeRef, FolderSizeModel.PROP_OUTPUT, (Serializable) resetFolderOutput);

        validatePermissions(nodeRef, nodeId);

        if(!"folder".equals(qName.getLocalName()))
        {
            throw new InvalidNodeTypeException(NOT_A_VALID_NODEID);
        }

        try
        {
            Action folderSizeAction = actionService.createAction(NodeSizeActionExecuter.NAME);
            folderSizeAction.setTrackStatus(true);
            folderSizeAction.setExecuteAsynchronously(true);
            folderSizeAction.setParameterValue(NodeSizeActionExecuter.PAGE_SIZE, maxItems);
            actionService.executeAction(folderSizeAction, nodeRef, false, true);
            LOG.info(" Executing ActionExecutor in NodeFolderSizeRelation:createById ");
            result.put("nodeId", nodeId);
            return result;
        }
        catch (Exception ex)
        {
            LOG.error("Exception occurred in NodeFolderSizeRelation:createById {}", ex.getMessage());
            throw ex; // This rethrows with the original stack trace preserved.
        }
    }

    @Override
    @WebApiDescription(title = "Returns Folder Node Size", description = "Returning a Folder Node Size")
    @WebApiParameters({@WebApiParam(name = "nodeId", title = "The unique id of the Node being addressed", description = "A single node id")})
    public Map<String, Object> readById(String nodeId, String id, Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateNode(nodeId);
        validatePermissions(nodeRef, nodeId);
        validateNodeType(nodeRef);

        try
        {
            LOG.info("Retrieving OUTPUT from ActionExecutor in NodeFolderSizeRelation:readById");
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            Map<String, Object> result = new HashMap<>();

            if (properties == null || !properties.containsKey(FolderSizeModel.PROP_OUTPUT))
            {
                result.put("status", "NOT-INITIATED");
            }
            else
            {
                Map<String, Object> mapResult = (Map<String, Object>) properties.get(FolderSizeModel.PROP_OUTPUT);
                Object status = mapResult.get("status");

                if (status != null && status.toString().contains("IN-PROGRESS"))
                {
                    result.put("status", "IN-PROGRESS");
                }
                else
                {
                    mapResult.put("status", "COMPLETED");
                    result = mapResult;
                }
            }
            return result;
        }
        catch (Exception ex)
        {
            LOG.error("Exception occurred in NodeFolderSizeRelation:readById {}", ex.getMessage());
            throw ex; // Rethrow with original stack trace
        }
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

    private void validateNodeType(NodeRef nodeRef)
    {
        // Check node type.
        QName qName = nodeService.getType(nodeRef);

        if (!"folder".equals(qName.getLocalName()))
        {
            throw new InvalidNodeTypeException(NOT_A_VALID_NODEID);
        }

        // Check for specific properties
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        if (properties.containsKey(FolderSizeModel.PROP_ERROR) && properties.get(FolderSizeModel.PROP_ERROR) != null)
        {
            throw new InvalidNodeTypeException(String.valueOf(properties.get(FolderSizeModel.PROP_ERROR)));
        }
    }

}