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

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.SizeDetails;
import org.alfresco.rest.api.model.NodeSizeDetails;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;
import java.util.Arrays;
import java.util.List;

@RelationshipResource(name = "size-details", entityResource = NodesEntityResource.class, title = "Node Size Details")
public class NodeSizeDetailsRelation implements
        RelationshipResourceAction.ReadById<NodeSizeDetails>,
        RelationshipResourceAction.Create<NodeSizeDetails>,
        InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(NodeSizeDetailsRelation.class);
    private Nodes nodes;
    private SizeDetails sizeDetails;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setSizeDetails(SizeDetails sizeDetails)
    {
        this.sizeDetails = sizeDetails;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    @WebApiDescription(title = "Create node-size details request", successStatus = Status.STATUS_ACCEPTED)
    @WebApiParam(name="nodeSizeEntity", title="Node Size Details Request", description="Request for processing Node Size.",
            kind= ResourceParameter.KIND.HTTP_BODY_OBJECT, allowMultiple=false)
    @Override
    public List<NodeSizeDetails> create(String nodeId, List<NodeSizeDetails> nodeSizeEntity, Parameters parameters)
    {
        LOG.debug(" Executing generateNodeSizeDetailsRequest method ");
        return Arrays.asList(sizeDetails.generateNodeSizeDetailsRequest(nodeId));
    }

    @WebApiDescription(title = "Get Node Size Details", description = "Get the Node Size Details")
    @WebApiParameters({
            @WebApiParam(name="nodeId", title="The unique id of the Node being addressed", description="A single node id"),
            @WebApiParam(name="jobId", title="Job Id to get the NodeSizeDetails", description="JobId")})
    @Override
    public NodeSizeDetails readById(String nodeId, String jobId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        LOG.debug(" Executing getNodeSizeDetails method ");
        return sizeDetails.getNodeSizeDetails(nodeId,jobId);
    }
}