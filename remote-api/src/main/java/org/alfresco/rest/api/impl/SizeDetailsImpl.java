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

import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsService;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl.NodeSizeDetails;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl.NodeSizeDetails.STATUS;
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
    private final Nodes nodes;
    private final NodeSizeDetailsService nodeSizeDetailsService;

    public SizeDetailsImpl(Nodes nodes, NodeSizeDetailsService nodeSizeDetailsService)
    {
        this.nodes = nodes;
        this.nodeSizeDetailsService = nodeSizeDetailsService;
    }

    /**
     * generateNodeSizeDetailsRequest : providing HTTP STATUS 202 with jobId.
     */
    @Override
    public NodeSizeDetails generateNodeSizeDetailsRequest(String nodeId)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        validateType(nodeRef);

        Optional<NodeSizeDetails> nodeSizeDetails = nodeSizeDetailsService.getSizeDetails(nodeId);
        String actionId = nodeSizeDetails.map(NodeSizeDetails::getJobId)
                    .orElseGet(() -> executeSizeDetails(nodeRef));

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

        Optional<NodeSizeDetails> optionalDetails = nodeSizeDetailsService.getSizeDetails(nodeId);
        return optionalDetails.map(details -> {
                        String cachedJobId = details.getJobId();
                        if (cachedJobId != null && !jobId.equalsIgnoreCase(cachedJobId))
                        {
                            throw new NotFoundException("jobId does not exist");
                        }
                        return details;
                    })
                    .orElseGet(() -> new NodeSizeDetails(nodeId, STATUS.NOT_INITIATED));
    }

    /**
     * Executing Asynchronously.
     */
    private String executeSizeDetails(NodeRef nodeRef)
    {
        String jobId = GUID.generate();
        NodeSizeDetails nodeSizeDetails = new NodeSizeDetails(nodeRef.getId(), jobId, STATUS.PENDING);
        nodeSizeDetailsService.putSizeDetails(nodeRef.getId(), nodeSizeDetails);
        nodeSizeDetailsService.invokeSizeDetailsExecutor(nodeRef, jobId);
        return jobId;
    }

    private void validateType(NodeRef nodeRef) throws InvalidNodeTypeException
    {
        if (!nodes.isSubClass(nodeRef, ContentModel.TYPE_FOLDER, false))
        {
            throw new InvalidNodeTypeException("Node id " + nodeRef.getId() + " does not represent a folder.");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodes", this.nodes);
        ParameterCheck.mandatory("nodeSizeDetailsServiceImpl", this.nodeSizeDetailsService);
    }

}