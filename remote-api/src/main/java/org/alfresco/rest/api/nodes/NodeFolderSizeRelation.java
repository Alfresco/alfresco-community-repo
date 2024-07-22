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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.executer.NodeSizeActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.FolderSizeImpl;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.CalculateSize;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction.ReadById;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
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
public class NodeFolderSizeRelation implements CalculateSize<Map<String, Object>>, ReadById<Map<String, Object>>, InitializingBean
{
    private Nodes nodes;
    private SearchService searchService;
    private ServiceRegistry serviceRegistry;
    private PermissionService permissionService;
    private NodeService nodeService;
    private ActionService actionService;
    private ActionTrackingService actionTrackingService;
    private Action folderSizeAction;
    private FolderSizeImpl folderSizeImpl;
    private NodeRef nodeRef;
    private String exceptionMessage = "Invalid parameter: value of nodeId is invalid";
    /** the shared cache that will hold action data */
    private static SimpleCache<Serializable,Object> sharedCache;

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

    public void setActionTrackingService(ActionTrackingService actionTrackingService)
    {
        this.actionTrackingService = actionTrackingService;
    }

    public void setSharedCache(SimpleCache<Serializable, Object> sharedCache)
    {
        this.sharedCache = sharedCache;
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
    @Override
    @WebApiDescription(title = "Calculating Folder Size", description = "Calculating size of a folder", successStatus = Status.STATUS_ACCEPTED)
    public Map<String, Object> createById(String nodeId, Parameters params)
    {
        nodeRef = nodes.validateNode(nodeId);
        int maxItems = Math.min(params.getPaging().getMaxItems(), 1000);
        QName qName = nodeService.getType(nodeRef);
        Map<String, Object> result = new HashMap<>();

        validatePermissions(nodeRef, nodeId);

        if(!"folder".equals(qName.getLocalName()))
        {
            throw new InvalidNodeTypeException(exceptionMessage);
        }

        try
        {
            this.folderSizeImpl = new FolderSizeImpl(actionService, actionTrackingService, nodeService);
            return this.folderSizeImpl.executingAsynchronousFolderAction(maxItems, nodeRef, result, sharedCache);
        }
        catch (Exception ex)
        {
            LOG.error("Exception occurred in NodeFolderSizeRelation:createById {}", ex.getMessage());
            throw new AlfrescoRuntimeException("Exception occurred in NodeFolderSizeRelation:createById");
        }
    }

    @Override
    @WebApiDescription(title = "Returns Folder Node Size", description = "Returning a Folder Node Size")
    @WebApiParameters({@WebApiParam(name = "executionId", title = "The unique id of Execution Job", description = "A single execution id")})
    public Map<String, Object> readById(String executionId, String id, Parameters parameters)
    {
        Map<String, Object> result = new HashMap<>();

        try
        {
            LOG.info("Retrieving OUTPUT from NodeSizeActionExecutor - NodeFolderSizeRelation:readById");
            if(nodeRef!=null)
            {
                //Action extractedResult = this.folderSizeImpl.extractingActionsResult(nodeRef, executionId);
                Action actionData = (Action) sharedCache.get(executionId);
                Action extractedResult = this.folderSizeImpl.getAction(nodeRef, executionId);
                result = getResult(extractedResult);

            }
            else
            {
                result.put("status", "NOT-INITIATED");
            }
            return result;
        }
        catch (Exception ex)
        {
            LOG.error("Exception occurred in NodeFolderSizeRelation:readById {}", ex.getMessage());
            throw ex; // Rethrow with original stack trace
        }
    }

    private Map<String, Object> getResult(Action resultAction)
    {
        Map<String, Object> result = new HashMap<>();

        if (resultAction == null)
        {
            result.put("status", "NOT-INITIATED");
        }
        else
        {
            ActionStatus actionStatus = resultAction.getExecutionStatus();

            if(ActionStatus.Pending.name().equals(actionStatus.name()))
            {
                result.put("status", "IN-PROGRESS");
            }
            else
            {
                Map<String, Object> mapResult = (Map<String, Object>) resultAction.getParameterValue(NodeSizeActionExecuter.RESULT);
                mapResult.put("status","COMPLETED");
                result = mapResult;
            }
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