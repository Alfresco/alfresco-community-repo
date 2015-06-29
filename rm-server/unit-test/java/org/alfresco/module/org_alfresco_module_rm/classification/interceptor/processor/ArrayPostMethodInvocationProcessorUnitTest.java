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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Array Post Method Invocation Processor Unit Test
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ArrayPostMethodInvocationProcessorUnitTest extends BaseUnitTest
{
    @InjectMocks ArrayPostMethodInvocationProcessor arrayPostMethodInvocationProcessor;
    @Mock private ContentClassificationService mockedContentClassificationService;
    @Mock private PostMethodInvocationProcessor mockedPostMethodInvocationProcessor;

    @Test
    public void testArrayPostMethodInvocationProcessor()
    {
        NodeRefPostMethodInvocationProcessor processor = new NodeRefPostMethodInvocationProcessor();
        processor.setNodeService(mockedNodeService);
        processor.setDictionaryService(mockedDictionaryService);
        processor.setContentClassificationService(mockedContentClassificationService);

        NodeRef nodeRef1 = generateNodeRef();
        NodeRef nodeRef2 = generateNodeRef();
        NodeRef nodeRef3 = generateNodeRef();
        NodeRef nodeRef4 = generateNodeRef();

        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef1), TYPE_CONTENT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef2), TYPE_CONTENT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef3), TYPE_CONTENT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef4), TYPE_CONTENT)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef1)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef2)).thenReturn(false);
        when(mockedContentClassificationService.hasClearance(nodeRef3)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef4)).thenReturn(false);
        when(mockedPostMethodInvocationProcessor.getProcessor(Mockito.any())).thenReturn(processor);

        NodeRef[] nodes = new NodeRef[] { nodeRef1, nodeRef2, nodeRef3, nodeRef4 };
        NodeRef[] processedNodes = arrayPostMethodInvocationProcessor.process(nodes);

        assertEquals(2, processedNodes.length);
        assertTrue(ArrayUtils.contains(processedNodes, nodeRef1));
        assertTrue(ArrayUtils.contains(processedNodes, nodeRef3));
    }
}
