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
import static org.junit.Assert.assertNull;
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
 * Unit tests for the {@link CollectionPostMethodInvocationProcessor}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class CollectionPostMethodInvocationProcessorUnitTest
{
    private static final String NON_FILTERED = "NON_FILTERED";
    private static final String FILTERED = "FILTERED";
    private static final String CHANGED_INPUT = "CHANGED_INPUT";
    private static final String CHANGED_OUTPUT = "CHANGED_OUTPUT";

    @InjectMocks CollectionPostMethodInvocationProcessor collectionPostMethodInvocationProcessor;
    @Mock PostMethodInvocationProcessor mockPostMethodInvocationProcessor;
    @Mock BasePostMethodInvocationProcessor mockStringProcessor;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockPostMethodInvocationProcessor.getProcessor(isA(String.class))).thenReturn(mockStringProcessor);

        when(mockStringProcessor.process(NON_FILTERED)).thenReturn(NON_FILTERED);
        when(mockStringProcessor.process(FILTERED)).thenReturn(null);
        when(mockStringProcessor.process(CHANGED_INPUT)).thenReturn(CHANGED_OUTPUT);
    }

    @Test
    public void testProcess_copesWithNull()
    {
        Object result = collectionPostMethodInvocationProcessor.process(null);

        assertNull("Expected null collection to be passed through.", result);
    }

    @Test
    public void testProcess_nullMember()
    {
        List<String> collection = new ArrayList<>();
        collection.add(null);

        Object result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals("Expected collection containing null to be passed through.", collection, result);
    }

    @Test
    public void testProcess_nonFilteredMember()
    {
        Object collection = Arrays.asList(NON_FILTERED);

        Object result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals("Expected element to still be present in result.", collection, result);
    }

    @Test
    public void testProcess_filteredMemberInModifiableList()
    {
        List<String> collection = new ArrayList<>(Arrays.asList(FILTERED));

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        assertTrue("Expected an empty list.", result.isEmpty());
    }

    @Test
    public void testProcess_filteredMemberInUnmodifiableList()
    {
        List<String> collection = Arrays.asList(FILTERED, NON_FILTERED);

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        assertNull("Since the collection could not be modified the whole thing should be filtered.", result);
    }

    @Test
    public void testProcess_modifiedMember()
    {
        List<String> collection = Arrays.asList(NON_FILTERED, CHANGED_INPUT);

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        assertNull("Since the Collection interface does not support replacement, the whole collection should be filtered.",
                    result);
    }

    @Test
    public void testProcess_noProcessorDefined()
    {
        List<Integer> collection = Arrays.asList(1, 4, 91);

        Collection<Integer> result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals("If no processor is defined for the members then the whole list should be returned.", collection,
                    result);
    }
}
