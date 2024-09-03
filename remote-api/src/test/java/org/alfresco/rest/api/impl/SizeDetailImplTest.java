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
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeSizeDetail;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SizeDetailImpl} class.
 *
 */
public class SizeDetailImplTest
{
    private final static int DEFAULT_ITEMS = 1000;
    private SizeDetailImpl sizeDetailImpl;
    private Nodes nodes;
    private NodeService nodeService;
    private ActionService actionService;
    private Action action;
    private static final String NAMESPACE = "http://www.alfresco.org/test/NodeSizeDetailsTest";
    private static final QName TYPE_FOLDER = QName.createQName(NAMESPACE, "folder");

    @Before
    public void setUp()
    {
        sizeDetailImpl = new SizeDetailImpl();
        nodes = mock(Nodes.class);
        nodeService = mock(NodeService.class);
        PermissionService permissionService = mock(PermissionService.class);
        actionService = mock(ActionService.class);
        action = mock(Action.class);
        SimpleCache<Serializable, Map<String, Object>> simpleCache  = mock(SimpleCache.class);

        sizeDetailImpl.setNodes(nodes);
        sizeDetailImpl.setNodeService(nodeService);
        sizeDetailImpl.setPermissionService(permissionService);
        sizeDetailImpl.setActionService(actionService);
        sizeDetailImpl.setSimpleCache(simpleCache);
        sizeDetailImpl.setDefaultItems(DEFAULT_ITEMS);
    }

    @Test
    public void calculateNodeSize()
    {
        String nodeName = "folderNode";
        String nodeId = "node-id";
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

        when(nodes.validateNode(nodeId)).thenReturn(nodeRef);
        when(nodes.getNode(nodeId)).thenReturn(node);
        when(nodeService.getType(nodeRef)).thenReturn(TYPE_FOLDER);
        when(actionService.createAction(NodeSizeDetailActionExecutor.NAME)).thenReturn(action);
        NodeSizeDetail nodeSizeDetail = sizeDetailImpl.getNodeSizeDetails(nodeId,"");
        assertNull("After executing POST/request-size-detail first time, it will provide null with 202 status code", nodeSizeDetail);
    }

}
