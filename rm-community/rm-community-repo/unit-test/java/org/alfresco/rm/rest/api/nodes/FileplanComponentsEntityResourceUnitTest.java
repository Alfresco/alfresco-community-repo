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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.RMNodesImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit Test class for FileplanComponentsEntityResource.
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class FileplanComponentsEntityResourceUnitTest extends BaseUnitTest
{
    private static final String PERMANENT_PARAMETER = "permanent";

    @Mock
    private RMNodesImpl mockedRMNodes;

    @InjectMocks
    private FileplanComponentsEntityResource filePlanComponentsEntityResource;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadById() throws Exception
    {
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        Parameters mockedParameters = mock(Parameters.class);
        filePlanComponentsEntityResource.readById(parentNodeRef.getId(), mockedParameters);
        verify(mockedRMNodes, times(1)).getFolderOrDocument(parentNodeRef.getId(), mockedParameters);
    }

    @Test
    public void testUpdate() throws Exception
    {
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        Parameters mockedParameters = mock(Parameters.class);
        Node mockedNodeInfo = mock(Node.class);
        filePlanComponentsEntityResource.update(parentNodeRef.getId(), mockedNodeInfo, mockedParameters);
        verify(mockedRMNodes, times(1)).updateNode(parentNodeRef.getId(), mockedNodeInfo, mockedParameters);
    }

    @Test
    public void testDelete() throws Exception
    {
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        Parameters mockedParameters = mock(Parameters.class);
        when(mockedParameters.getParameter(PERMANENT_PARAMETER)).thenReturn(null);
        filePlanComponentsEntityResource.delete(parentNodeRef.getId(), mockedParameters);
        verify(mockedRMNodes, times(1)).deleteNode(parentNodeRef.getId(), mockedParameters);
    }

    @Test
    public void testDeleteWithPermanentParameter() throws Exception
    {
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        Parameters mockedParameters = mock(Parameters.class);
        when(mockedParameters.getParameter(PERMANENT_PARAMETER)).thenReturn(Boolean.toString(true));
        try
        {
            filePlanComponentsEntityResource.delete(parentNodeRef.getId(), mockedParameters);
            fail("Expected ecxeption as DELETE does not support parameter: permanent.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("DELETE does not support parameter: permanent", ex.getMsgId());
        }
        verify(mockedRMNodes, never()).deleteNode(parentNodeRef.getId(), mockedParameters);
    }
}
