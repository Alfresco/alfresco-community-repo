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
import static org.mockito.Mockito.when;
import static org.springframework.extensions.webscripts.GUID.generate;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * NodeRef Post Method Invocation Processor Unit Test
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class NodeRefPostMethodInvocationProcessorUnitTest extends BaseUnitTest
{
    @InjectMocks private NodeRefPostMethodInvocationProcessor nodeRefPostMethodInvocationProcessor;
    @Mock private ContentClassificationService mockedContentClassificationService;
    @Mock private PreMethodInvocationProcessor mockedPreMethodInvocationProcessor;

    @Test
    public void testProcessingNonExistingNode()
    {
        NodeRef nodeRef = new NodeRef(generate() + "://" + generate() + "/");

        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef), TYPE_CONTENT)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef)).thenReturn(true);

        assertEquals(nodeRef, nodeRefPostMethodInvocationProcessor.process(nodeRef));
    }

    @Test
    public void testProcessingNonContent()
    {
        NodeRef nodeRef = generateNodeRef();

        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef), TYPE_CONTENT)).thenReturn(false);
        when(mockedContentClassificationService.hasClearance(nodeRef)).thenReturn(true);

        assertEquals(nodeRef, nodeRefPostMethodInvocationProcessor.process(nodeRef));
    }

    @Test
    public void testExistingNodeWithUserClearance()
    {
        NodeRef nodeRef = generateNodeRef();

        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef), TYPE_CONTENT)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef)).thenReturn(true);

        assertEquals(nodeRef, nodeRefPostMethodInvocationProcessor.process(nodeRef));
    }

    @Test
    public void testExistingNodeWithNoUserClearance()
    {
        NodeRef nodeRef = generateNodeRef();

        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(nodeRef), TYPE_CONTENT)).thenReturn(true);
        when(mockedContentClassificationService.hasClearance(nodeRef)).thenReturn(false);

        assertEquals(null, nodeRefPostMethodInvocationProcessor.process(nodeRef));
    }

    @Test
    public void testProcessingNull()
    {
        assertEquals("Expected null to be passed through without error.", null,
                    nodeRefPostMethodInvocationProcessor.process((NodeRef) null));
    }
}
