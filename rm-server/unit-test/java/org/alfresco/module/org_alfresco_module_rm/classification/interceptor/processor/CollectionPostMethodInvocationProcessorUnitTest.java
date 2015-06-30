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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.alfresco.util.GUID.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private static final String REALLY_LONG_OUTPUT_STRING = generate() + generate();
    private static final String NON_FILTERED = generate();
    private static final String FILTERED = generate();

    @InjectMocks private CollectionPostMethodInvocationProcessor collectionPostMethodInvocationProcessor;
    @Mock private PostMethodInvocationProcessor mockPostMethodInvocationProcessor;
    @Mock private BasePostMethodInvocationProcessor mockStringProcessor;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockPostMethodInvocationProcessor.getProcessor(isA(List.class))).thenReturn(collectionPostMethodInvocationProcessor);
        when(mockPostMethodInvocationProcessor.getProcessor(isA(String.class))).thenReturn(mockStringProcessor);

        when(mockStringProcessor.process(REALLY_LONG_OUTPUT_STRING)).thenReturn(REALLY_LONG_OUTPUT_STRING);
        when(mockStringProcessor.process(NON_FILTERED)).thenReturn(NON_FILTERED);
        when(mockStringProcessor.process(FILTERED)).thenReturn(null);
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
    public void testProcess_emptyList()
    {
        List<String> collection = new ArrayList<>();

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals(collection, result);
    }

    @Test
    public void testProcess_nonFilteredMember()
    {
        Object collection = asList(NON_FILTERED);

        Object result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals("Expected element to still be present in result.", collection, result);
    }

    @Test
    public void testProcess_filteredMemberInModifiableList()
    {
        List<String> collection = newArrayList(FILTERED);

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        assertTrue("Expected an empty list.", result.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testProcess_filteredMemberInUnmodifiableList()
    {
        List<String> collection = asList(FILTERED, NON_FILTERED);

        collectionPostMethodInvocationProcessor.process(collection);
    }

    @Test
    public void testProcess_noProcessorDefined()
    {
        List<Integer> collection = asList(1, 4, 91);

        Collection<Integer> result = collectionPostMethodInvocationProcessor.process(collection);

        assertEquals("If no processor is defined for the members then the whole list should be returned.", collection, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcess_listOfLists()
    {
        List<String> innerListA = newArrayList(FILTERED, NON_FILTERED);
        List<String> innerListB = newArrayList(FILTERED, NON_FILTERED);
        List<List<String>> collection = newArrayList(innerListA, innerListB);

        Collection<List<String>> result = collectionPostMethodInvocationProcessor.process(collection);

        List<String> expectedInnerListA = asList(NON_FILTERED);
        List<String> expectedInnerListB = asList(NON_FILTERED);
        List<List<String>> expected = asList(expectedInnerListA, expectedInnerListB);
        assertEquals(expected, result);
    }

    /**
     * Given I have a sorted set of input strings
     * When I pass it to the collection processor
     * Then I expect items above my clearance to be filtered
     * And I expect items below my clearance to be passed through
     * And I expect the output set to be sorted using the same comparator as the input.
     */
    @Test
    public void testProcess_sortedSet()
    {
        // Create a custom comparator that sorts based on the length of the strings.
        Comparator<String> comparator = new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                return o1.length() - o2.length();
            }
        };

        SortedSet<String> collection = new TreeSet<>(comparator);
        collection.add(REALLY_LONG_OUTPUT_STRING);
        collection.add(NON_FILTERED);
        collection.add(FILTERED);

        Collection<String> result = collectionPostMethodInvocationProcessor.process(collection);

        Iterator<String> iterator = result.iterator();
        assertEquals("Expected the first element to be the shortest", NON_FILTERED, iterator.next());
        assertEquals("Expected the second element to be the longest", REALLY_LONG_OUTPUT_STRING, iterator.next());
        assertFalse("Expected two elements in output", iterator.hasNext());
    }
}
