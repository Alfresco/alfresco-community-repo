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

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.NodeSizeDetails;
import org.alfresco.rest.api.tests.AbstractBaseApiTest;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.UserInfo;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SizeDetailsImpl} class.
 *
 */
public class SizeDetailsImplTest extends AbstractBaseApiTest
{
    private final static int DEFAULT_ITEMS = 1000;
    private SizeDetailsImpl sizeDetailsImpl;
    private NodeService nodeService;
    private PermissionService permissionService;
    private Nodes nodes;

    @Before
    public void setUp()
    {
        sizeDetailsImpl = new SizeDetailsImpl();
        nodes = mock(Nodes.class);
        nodeService = mock(NodeService.class);
        permissionService = mock(PermissionService.class);
        ActionService actionService = mock(ActionService.class);
        SimpleCache<Serializable, Map<String, Object>> simpleCache  = mock(SimpleCache.class);

        sizeDetailsImpl.setNodes(nodes);
        sizeDetailsImpl.setNodeService(nodeService);
        sizeDetailsImpl.setPermissionService(permissionService);
        sizeDetailsImpl.setActionService(actionService);
        sizeDetailsImpl.setSimpleCache(simpleCache);
        sizeDetailsImpl.setDefaultItems(DEFAULT_ITEMS);
    }

    @Test
    public void calculateNodeSize() throws Exception
    {
        setRequestContext(user1);
        UserInfo userInfo = new UserInfo(user1);

        String fileName = "content.txt";
        String folder0Name = "f0-testParentFolder-"+RUNID;
        String parentFolder = createFolder(tDocLibNodeId, folder0Name,null).getId();
        permissionService.setPermission(nodes.validateNode(parentFolder), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

        Document d1 = new Document();
        d1.setIsFolder(false);
        d1.setParentId(parentFolder);
        d1.setName(fileName);
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setContent(createContentInfo());
        d1.setCreatedByUser(userInfo);
        d1.setModifiedByUser(userInfo);

        NodeSizeDetails nodeSizeDetails = sizeDetailsImpl.calculateNodeSize(parentFolder);
        assertNull("After executing POST/request-size-details first time, it will provide null with 202 status code",nodeSizeDetails);
    }

    // Method to create content info
    private ContentInfo createContentInfo()
    {
        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(44500L);
        ciExpected.setEncoding("ISO-8859-1");
        return ciExpected;
    }

    @Override
    public String getScope() {
        return "public";
    }
}
