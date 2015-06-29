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
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link ListPostMethodInvocationProcessor}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class ListPostMethodInvocationProcessorUnitTest
{
    private static final String NON_FILTERED = "NON_FILTERED";
    private static final String FILTERED = "FILTERED";
    private static final String CHANGED_INPUT = "CHANGED_INPUT";
    private static final String CHANGED_OUTPUT = "CHANGED_OUTPUT";

    @InjectMocks ListPostMethodInvocationProcessor listPostMethodInvocationProcessor;
    @Mock PostMethodInvocationProcessor mockPostMethodInvocationProcessor;
    @Mock BasePostMethodInvocationProcessor mockStringProcessor;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockPostMethodInvocationProcessor.getProcessor(isA(List.class))).thenReturn(listPostMethodInvocationProcessor);
        when(mockPostMethodInvocationProcessor.getProcessor(isA(String.class))).thenReturn(mockStringProcessor);

        when(mockStringProcessor.process(NON_FILTERED)).thenReturn(NON_FILTERED);
        when(mockStringProcessor.process(FILTERED)).thenReturn(null);
        when(mockStringProcessor.process(CHANGED_INPUT)).thenReturn(CHANGED_OUTPUT);
    }

    @Test
    public void testProcessCollection_emptyList()
    {
        List<String> collection = new ArrayList<>();

        Collection<String> result = listPostMethodInvocationProcessor.processCollection(collection, mockStringProcessor);

        assertEquals(collection, result);
    }

    @Test
    public void testProcessCollection_completelyFiltered()
    {
        List<String> collection = Arrays.asList(FILTERED, FILTERED);

        Collection<String> result = listPostMethodInvocationProcessor.processCollection(collection, mockStringProcessor);

        assertTrue("Expected all members of the list to be removed.", result.isEmpty());
    }

    @Test
    public void testProcessCollection_supportsReplacement()
    {
        List<String> collection = Arrays.asList(NON_FILTERED, CHANGED_INPUT);

        Collection<String> result = listPostMethodInvocationProcessor.processCollection(collection, mockStringProcessor);

        List<String> expected = Arrays.asList(NON_FILTERED, CHANGED_OUTPUT);
        assertEquals(expected, result);
    }

    @Test
    public void testProcess_listOfLists()
    {
        List<String> innerListA = Arrays.asList(FILTERED, NON_FILTERED, CHANGED_INPUT);
        List<String> innerListB = Arrays.asList(CHANGED_INPUT, FILTERED, NON_FILTERED);
        List<List<String>> collection = Arrays.asList(innerListA, innerListB);

        Collection<List<String>> result = listPostMethodInvocationProcessor.process(collection);

        List<String> expectedInnerListA = Arrays.asList(NON_FILTERED, CHANGED_OUTPUT);
        List<String> expectedInnerListB = Arrays.asList(CHANGED_OUTPUT, NON_FILTERED);
        List<List<String>> expected = Arrays.asList(expectedInnerListA, expectedInnerListB);
        assertEquals(expected, result);
    }
}
