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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl.NodeSizeDetails;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SizeDetailsImpl} class.
 */
public class SizeDetailsImplTest
{
    private static final String NAMESPACE = "http://www.alfresco.org/test/NodeSizeDetailsTest";
    private static final QName TYPE_FOLDER = QName.createQName(NAMESPACE, "folder");
    private SizeDetailsImpl sizeDetailsImpl;
    private Nodes nodes;
    private NodeSizeDetailsServiceImpl nodeSizeDetailsServiceImpl;
    private NodeSizeDetails nodeSizeDetails;

    @Before
    public void setUp() throws Exception
    {
        sizeDetailsImpl = new SizeDetailsImpl();
        nodes = mock(Nodes.class);
        SearchService searchService = mock(SearchService.class);
        nodeSizeDetailsServiceImpl = mock(NodeSizeDetailsServiceImpl.class);
        ThreadPoolExecutor threadPoolExecutor = mock(ThreadPoolExecutor.class);
        SimpleCache<Serializable, NodeSizeDetails> simpleCache = mock(SimpleCache.class);
        nodeSizeDetails = mock(NodeSizeDetails.class);

        nodeSizeDetailsServiceImpl.setSearchService(searchService);
        nodeSizeDetailsServiceImpl.setDefaultItems(1000);
        nodeSizeDetailsServiceImpl.setSimpleCache(simpleCache);
        verify(nodeSizeDetailsServiceImpl).setSimpleCache(simpleCache);
        nodeSizeDetailsServiceImpl.setThreadPoolExecutor(threadPoolExecutor);
        sizeDetailsImpl.setNodes(nodes);
        sizeDetailsImpl.setNodeSizeDetailsService(nodeSizeDetailsServiceImpl);
    }

    @Test
    public void calculateNodeSizeDetails()
    {
        String nodeName = "folderNode";
        String nodeId = "node-id";
        String jobId = "job-id";
        NodeRef nodeRef = new NodeRef("protocol", "identifier", nodeId);

        Node node = new Node();
        node.setIsFolder(true);
        node.setNodeRef(nodeRef);
        node.setName(nodeName);
        node.setNodeType(TYPE_FOLDER.getLocalName());
        node.setNodeId(nodeRef.getId());

        when(nodes.validateOrLookupNode(nodeId)).thenReturn(nodeRef);
        when(nodes.isSubClass(nodeRef, ContentModel.TYPE_FOLDER, false)).thenReturn(true);
        when(nodeSizeDetailsServiceImpl.getSizeDetails(nodeId)).thenReturn(nodeSizeDetails);

        NodeSizeDetails requestSizeDetails = sizeDetailsImpl.generateNodeSizeDetailsRequest(nodeId);
        assertNotNull("After executing POST/size-details, it will provide with 202 status code", requestSizeDetails);

        NodeSizeDetails nodeSizeDetails = sizeDetailsImpl.getNodeSizeDetails(nodeId, jobId);
        assertNotNull("After executing GET/size-details, it will provide with 200 status code", nodeSizeDetails);

    }

}
