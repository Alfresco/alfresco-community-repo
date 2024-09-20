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

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.sizeDetails.NodeSizeDetailsService;
import org.alfresco.repo.node.sizeDetails.NodeSizeDetailsServiceImpl.NodeSizeDetails;
import org.alfresco.repo.node.sizeDetails.NodeSizeDetailsServiceImpl.NodeSizeDetails.STATUS;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.SizeDetails;
import org.alfresco.rest.framework.core.exceptions.InvalidNodeTypeException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

public class SizeDetailsImpl implements SizeDetails, InitializingBean
{
    private Nodes nodes;
    private NodeRef nodeRef;
    private SimpleCache<Serializable, NodeSizeDetails> simpleCache;
    private NodeSizeDetailsService nodeSizeDetailsService;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setNodeSizeDetailsService(NodeSizeDetailsService nodeSizeDetailsService)
    {
        this.nodeSizeDetailsService = nodeSizeDetailsService;
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
            actionId = executeSizeDetails();
        }
        else
        {
            NodeSizeDetails nodeSizeDetails = simpleCache.get(nodeId);
            actionId = nodeSizeDetails.getJobId();
        }
        return new NodeSizeDetails(null, null, actionId, null);
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
            NodeSizeDetails nodeSizeDetails = new NodeSizeDetails(nodeId, null, null, STATUS.NOT_INITIATED);
            return nodeSizeDetails;
        }
        else
        {
            NodeSizeDetails nodeSizeDetails = simpleCache.get(nodeId);
            String cachedJobId = nodeSizeDetails.getJobId();
            if (cachedJobId != null && !jobId.equalsIgnoreCase(cachedJobId))
            {
                throw new NotFoundException("jobId does not exist");
            }
        }

        return simpleCache.get(nodeId);
    }

    /**
     * Executing Asynchronously.
     */
    private String executeSizeDetails()
    {
        String jobId = GUID.generate();
        nodeSizeDetailsService.invokeSizeDetailsExecutor(nodeRef, jobId);
        NodeSizeDetails nodeSizeDetails = new NodeSizeDetails(nodeRef.getId(), null, jobId, STATUS.PENDING);
        simpleCache.put(nodeRef.getId(), nodeSizeDetails);
        return jobId;
    }

    private void validateType(NodeRef nodeRef) throws InvalidNodeTypeException
    {
        if (!nodes.isSubClass(nodeRef, ContentModel.TYPE_FOLDER, false))
        {
            throw new InvalidNodeTypeException("Invalid parameter: value of nodeId is invalid");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodes", this.nodes);
        ParameterCheck.mandatory("nodeSizeDetailsServiceImpl", this.nodeSizeDetailsService);
        this.simpleCache = nodeSizeDetailsService.getSimpleCache();
    }

}