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

import static org.alfresco.rest.api.SizeDetails.ProcessingState.COMPLETED;
import static org.alfresco.rest.api.SizeDetails.ProcessingState.IN_PROGRESS;
import static org.alfresco.rest.api.SizeDetails.ProcessingState.NOT_INITIATED;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.NodeSizeDetailActionExecutor;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.SizeDetails;
import org.alfresco.rest.api.model.NodeSizeDetails;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.core.exceptions.UnprocessableContentException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;

public class SizeDetailsImpl implements SizeDetails
{
    private static final String ACTION_ID = "actionId";
    private Nodes nodes;
    private NodeRef nodeRef;
    private ActionService actionService;
    private SimpleCache<Serializable, Map<String, Object>> simpleCache;
    private int defaultItems;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
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

    /**
     * generateNodeSizeDetailsRequest : providing HTTP STATUS 202 with jobId.
     */
    @Override
    public NodeSizeDetails generateNodeSizeDetailsRequest(String nodeId)
    {
        nodeRef = nodes.validateOrLookupNode(nodeId);
        validateType(nodeRef);
        String actionId;
        if (!simpleCache.contains(nodeId))
        {
            actionId = executeAction();
        }
        else
        {
            Map<String, Object> result = simpleCache.get(nodeRef.getId());
            actionId = (String) result.get(ACTION_ID);
        }
        return new NodeSizeDetails(actionId);
    }

    /**
     * getNodeSizeDetails : providing HTTP STATUS 200 with NodeSizeDetails data from cache.
     */
    @Override
    public NodeSizeDetails getNodeSizeDetails(final String nodeId, final String jobId)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        validateType(nodeRef);

        if (!simpleCache.contains(nodeId))
        {
            return new NodeSizeDetails(nodeId, NOT_INITIATED.name());
        }

        return executorResultToSizeDetail(simpleCache.get(nodeId), nodeId, jobId);
    }

    /**
     * Executing Action Asynchronously.
     */
    private String executeAction()
    {
        Map<String, Object > currentStatus = new HashMap<>();
        currentStatus.put("status",IN_PROGRESS.name());
        Action folderSizeAction = actionService.createAction(NodeSizeDetailActionExecutor.NAME);
        currentStatus.put(ACTION_ID,folderSizeAction.getId());
        folderSizeAction.setTrackStatus(true);
        folderSizeAction.setParameterValue(NodeSizeDetailActionExecutor.DEFAULT_SIZE, defaultItems);
        simpleCache.put(nodeRef.getId(),currentStatus);
        actionService.executeAction(folderSizeAction, nodeRef, false, true);
        return folderSizeAction.getId();
    }

    /**
     * Converting action executor response to their respective model class.
     */
    private NodeSizeDetails executorResultToSizeDetail(final Map<String, Object> result, String nodeId, String jobId)
    {
        if (result.containsKey(NodeSizeDetailActionExecutor.EXCEPTION))
        {
            return new NodeSizeDetails(nodeId, COMPLETED.name());
        }

        if (result.containsKey("size"))
        {
            NodeSizeDetails nodeSizeDetails =
                        new NodeSizeDetails((String) result.get("nodeId"), (Long) result.get("size"),
                                            (Date) result.get("calculatedAt"), (Integer) result.get("numberOfFiles"),
                                            COMPLETED.name(), (String) result.get(ACTION_ID));

            if (!nodeSizeDetails.getJobId()
                        .equalsIgnoreCase(jobId))
            {
                throw new UnprocessableContentException("Invalid parameter: value of jobId is invalid");
            }
            return nodeSizeDetails;
        }
        else
        {
            return new NodeSizeDetails(nodeId, IN_PROGRESS.name());
        }
    }

    private void validateType(NodeRef nodeRef) throws InvalidNodeTypeException
    {
        if (!nodes.isSubClass(nodeRef, ContentModel.TYPE_FOLDER, false))
        {
            throw new InvalidNodeTypeException("Invalid parameter: value of nodeId is invalid");
        }
    }
}