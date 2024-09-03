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
package org.alfresco.rest.api.impl;

import org.alfresco.repo.action.executer.NodeSizeDetailActionExecutor;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.SizeDetail;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.api.model.NodeSizeDetail;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.core.exceptions.UnprocessableContentException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import static org.alfresco.rest.api.SizeDetail.PROCESSINGSTATE.COMPLETED;
import static org.alfresco.rest.api.SizeDetail.PROCESSINGSTATE.NOT_INITIATED;
import static org.alfresco.rest.api.SizeDetail.PROCESSINGSTATE.IN_PROGRESS;

public class SizeDetailImpl implements SizeDetail
{
    private static final Logger LOG = LoggerFactory.getLogger(SizeDetailImpl.class);
    private static final String STATUS = "status";
    private static final String ACTIONID = "actionId";
    private static final String INVALID_NODEID = "Invalid parameter: value of nodeId is invalid";
    private static final String INVALID_JOBID = "Invalid parameter: value of jobId is invalid";
    private static final String FOLDER = "folder";
    private Nodes nodes;
    private NodeService nodeService;
    private PermissionService permissionService;
    private ActionService actionService;
    private SimpleCache<Serializable,Map<String, Object>> simpleCache;
    private int defaultItems;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
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

    public void setSimpleCache(SimpleCache<Serializable, Map<String, Object>> simpleCache)
    {
        this.simpleCache = simpleCache;
    }

    public void setDefaultItems(int defaultItems)
    {
        this.defaultItems = defaultItems;
    }

    @Override
    public NodeSizeDetail generateNodeSizeDetailsRequest(String nodeId) {
        NodeRef nodeRef = nodes.validateNode(nodeId);
        validateType(nodeRef);
        String actionId;
        if(simpleCache.get(nodeId) == null)
        {
            actionId = executeAction(nodeRef, defaultItems, simpleCache);
        } else {
            Map<String, Object> result = simpleCache.get(nodeRef.getId());
            actionId = (String)result.get(ACTIONID);
        }
        return new NodeSizeDetail(actionId);
    }

    /**
     * calculateNodeSize : providing HTTP STATUS 202 which signifies REQUEST ACCEPTED.
     *                     HTTP STATUS 200 will provide the size details response from cache.
     */
    @Override
    public NodeSizeDetail getNodeSizeDetails(final String nodeId, final String jobId)
    {
        NodeRef nodeRef = nodes.validateNode(nodeId);
        validateType(nodeRef);

        if(simpleCache.get(nodeId) == null)
        {
            return new NodeSizeDetail(nodeId, NOT_INITIATED.name());
        }

        LOG.debug("Executing NodeSizeDetailActionExecuter from calculateNodeSize method");
        return executorResultToSizeDetail(simpleCache.get(nodeId), nodeId, jobId);
    }

    /**
     * Executing Action Asynchronously.
     */
    private String executeAction(NodeRef nodeRef, int defaultItems, SimpleCache<Serializable, Map<String, Object>> simpleCache)
    {
        Map<String, Object > currentStatus = new HashMap<>();
        currentStatus.put(STATUS,IN_PROGRESS.name());
        Action folderSizeAction = actionService.createAction(NodeSizeDetailActionExecutor.NAME);
        currentStatus.put(ACTIONID,folderSizeAction.getId());
        folderSizeAction.setTrackStatus(true);
        folderSizeAction.setExecuteAsynchronously(true);
        folderSizeAction.setParameterValue(NodeSizeDetailActionExecutor.DEFAULT_SIZE, defaultItems);
        simpleCache.put(nodeRef.getId(),currentStatus);
        actionService.executeAction(folderSizeAction, nodeRef, false, true);
        return folderSizeAction.getId();
    }

    /**
     * Converting action executor response to their respective model class.
     */
    private NodeSizeDetail executorResultToSizeDetail(final Map<String,Object> result, String nodeId, String jobId)
    {
        if(result.containsKey(NodeSizeDetailActionExecutor.EXCEPTION))
        {
            return new NodeSizeDetail(nodeId, COMPLETED.name());
        }

        // Check for the presence of "size" key.
        boolean hasSizeKey = result.containsKey("size");

        if (hasSizeKey)
        {
            NodeSizeDetail nodeSizeDetail = new NodeSizeDetail((String) result.get("nodeId"),
                    (Long) result.get("size"),
                    (String) result.get("calculatedAt"),
                    (Integer) result.get("numberOfFiles"),
                    COMPLETED.name(),
                    (String) result.get(ACTIONID));

            if(!nodeSizeDetail.getJobId().equalsIgnoreCase(jobId)) {
                throw new UnprocessableContentException(INVALID_JOBID);
            }
            return nodeSizeDetail;
        }
        else
        {
            return new NodeSizeDetail(nodeId, IN_PROGRESS.name());
        }
    }

    private void validateType(NodeRef nodeRef) throws InvalidNodeTypeException
    {
        QName qName = nodeService.getType(nodeRef);
        validatePermissions(nodeRef, nodeRef.getId());
        if(!FOLDER.equalsIgnoreCase(qName.getLocalName()))
        {
            throw new InvalidNodeTypeException(INVALID_NODEID);
        }
    }

    /**
     * Validating node permission [i.e. READ permission should be there ]
     */
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