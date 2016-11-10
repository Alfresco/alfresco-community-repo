/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.nodes;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.RMNodesImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Unit Test class for FileplanComponentChildrenRelation.
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class FileplanComponentChildrenRelationUnitTest extends BaseUnitTest
{

    @Mock
    private RMNodesImpl mockedRMNodes;

    @InjectMocks
    private FileplanComponentChildrenRelation filePlanComponentChildrenRelation;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadAll() throws Exception
    {
        Parameters mockedParameters = mock(Parameters.class);
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        filePlanComponentChildrenRelation.readAll(parentNodeRef.getId(), mockedParameters);
        verify(mockedRMNodes, times(1)).listChildren(parentNodeRef.getId(), mockedParameters);
    }

    @Test
    public void testCreate() throws Exception
    {
        Parameters mockedParameters = mock(Parameters.class);
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);

        List<Node> nodeInfos = new ArrayList<Node>();
        Node mokedNodeInfo = mock(Node.class);
        nodeInfos.add(mokedNodeInfo);

        filePlanComponentChildrenRelation.create(parentNodeRef.getId(), nodeInfos, mockedParameters);
        verify(mockedRMNodes, times(1)).createNode(parentNodeRef.getId(), nodeInfos.get(0), mockedParameters);
    }

    @Test
    public void testUpload() throws Exception
    {
        Parameters mockedParameters = mock(Parameters.class);
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        FormData mockedFormData = mock(FormData.class);
        WithResponse mockedWithResponse = mock(WithResponse.class);
        filePlanComponentChildrenRelation.create(parentNodeRef.getId(), mockedFormData, mockedParameters, mockedWithResponse);
        verify(mockedRMNodes, times(1)).upload(parentNodeRef.getId(), mockedFormData, mockedParameters);
    }
}
