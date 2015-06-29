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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link SortedSetPostMethodInvocationProcessor}.
 *
 * @author Tom Page
 * @since 3.0
 */
public class SortedSetPostMethodInvocationProcessorUnitTest
{
    private static final String NON_FILTERED = "NON_FILTERED";
    private static final String FILTERED = "FILTERED";
    private static final String SHORT_INPUT = "SHORT_INPUT";
    private static final String REALLY_LONG_OUTPUT_STRING = "REALLY_LONG_OUTPUT_STRING";

    @InjectMocks SortedSetPostMethodInvocationProcessor sortedSetPostMethodInvocationProcessor;
    @Mock BasePostMethodInvocationProcessor mockStringProcessor;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockStringProcessor.process(NON_FILTERED)).thenReturn(NON_FILTERED);
        when(mockStringProcessor.process(FILTERED)).thenReturn(null);
        when(mockStringProcessor.process(SHORT_INPUT)).thenReturn(REALLY_LONG_OUTPUT_STRING);
    }

    /**
     * Given I have a sorted set of input strings
     * When I pass it to the SortedSet processor
     * Then I expect items above my clearance to be filtered
     * And I expect items below my clearance to be passed through
     * And I expect items that get changed by the filtering process to be changed
     * And I expect the output set to be sorted using the same comparator as the input.
     */
    @Test
    public void testProcessCollection()
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
        collection.add(SHORT_INPUT);
        collection.add(NON_FILTERED);
        collection.add(FILTERED);

        Collection<String> result = sortedSetPostMethodInvocationProcessor.processCollection(collection, mockStringProcessor);

        Iterator<String> iterator = result.iterator();
        assertEquals("Expected the first element to be the shortest", NON_FILTERED, iterator.next());
        assertEquals("Expected the second element to be the longest", REALLY_LONG_OUTPUT_STRING, iterator.next());
        assertFalse("Expected two elements in output", iterator.hasNext());
    }
}
