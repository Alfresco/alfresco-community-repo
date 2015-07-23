/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.util.GUID.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Array Post Method Invocation Processor Unit Test
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class ArrayPostMethodInvocationProcessorUnitTest
{
    private static final NodeRef NODE_REF_1 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, generate());
    private static final NodeRef NODE_REF_2 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, generate());
    private static final NodeRef NODE_REF_3 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, generate());
    private static final NodeRef NODE_REF_4 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, generate());

    @InjectMocks private ArrayPostMethodInvocationProcessor arrayPostMethodInvocationProcessor;
    @Mock private PostMethodInvocationProcessor mockedPostMethodInvocationProcessor;
    @Mock private BasePostMethodInvocationProcessor mockedNodeRefProcessor;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockedPostMethodInvocationProcessor.getProcessor(isA(NodeRef.class))).thenReturn(mockedNodeRefProcessor);

        when(mockedNodeRefProcessor.process(NODE_REF_1)).thenReturn(NODE_REF_1);
        when(mockedNodeRefProcessor.process(NODE_REF_2)).thenReturn(null);
        when(mockedNodeRefProcessor.process(NODE_REF_3)).thenReturn(NODE_REF_3);
        when(mockedNodeRefProcessor.process(NODE_REF_4)).thenReturn(null);
    }

    @Test
    public void testArrayPostMethodInvocationProcessor()
    {
        NodeRef[] nodes = new NodeRef[] { NODE_REF_1, NODE_REF_2, NODE_REF_3, NODE_REF_4 };
        NodeRef[] processedNodes = arrayPostMethodInvocationProcessor.process(nodes);

        assertEquals(2, processedNodes.length);
        assertTrue(ArrayUtils.contains(processedNodes, NODE_REF_1));
        assertTrue(ArrayUtils.contains(processedNodes, NODE_REF_3));
    }
}
