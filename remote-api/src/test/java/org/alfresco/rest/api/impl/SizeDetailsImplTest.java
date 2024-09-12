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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.action.executer.NodeSizeDetailActionExecutor;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeSizeDetails;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SizeDetailsImpl} class.
 */
public class SizeDetailsImplTest
{
    private final static int DEFAULT_ITEMS = 1000;
    private static final String NAMESPACE = "http://www.alfresco.org/test/NodeSizeDetailsTest";
    private static final QName TYPE_FOLDER = QName.createQName(NAMESPACE, "folder");
    private SizeDetailsImpl sizeDetailsImpl;
    private Nodes nodes;
    private ActionService actionService;
    private Action action;

    @Before
    public void setUp()
    {
        sizeDetailsImpl = new SizeDetailsImpl();
        nodes = mock(Nodes.class);
        actionService = mock(ActionService.class);
        action = mock(Action.class);
        SimpleCache<Serializable, Map<String, Object>> simpleCache = mock(SimpleCache.class);

        sizeDetailsImpl.setNodes(nodes);
        sizeDetailsImpl.setActionService(actionService);
        sizeDetailsImpl.setSimpleCache(simpleCache);
        sizeDetailsImpl.setDefaultItems(DEFAULT_ITEMS);
    }

    @Test
    public void calculateNodeSizeDetails()
    {
        String nodeName = "folderNode";
        String nodeId = "node-id";
        String jobId = "job-id";
        NodeRef nodeRef = new NodeRef("protocol", "identifier", nodeId);
        action.setTrackStatus(true);
        action.setExecuteAsynchronously(true);
        action.setParameterValue(NodeSizeDetailActionExecutor.DEFAULT_SIZE, DEFAULT_ITEMS);

        Node node = new Node();
        node.setIsFolder(true);
        node.setNodeRef(nodeRef);
        node.setName(nodeName);
        node.setNodeType(TYPE_FOLDER.getLocalName());
        node.setNodeId(nodeRef.getId());

        when(nodes.validateOrLookupNode(nodeId)).thenReturn(nodeRef);
        when(nodes.getNode(nodeId)).thenReturn(node);
        when(actionService.createAction(NodeSizeDetailActionExecutor.NAME)).thenReturn(action);

        NodeSizeDetails requestSizeDetails = sizeDetailsImpl.generateNodeSizeDetailsRequest(nodeId);
        assertNotNull("After executing POST/size-details, it will provide with 202 status code", requestSizeDetails);

        NodeSizeDetails nodeSizeDetails = sizeDetailsImpl.getNodeSizeDetails(nodeId, jobId);
        assertNotNull("After executing GET/size-details, it will provide with 200 status code", nodeSizeDetails);

    }

}
