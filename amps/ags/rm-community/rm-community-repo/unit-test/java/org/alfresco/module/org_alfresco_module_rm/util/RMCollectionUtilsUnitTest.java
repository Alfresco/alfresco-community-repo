/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static com.google.common.collect.Sets.newHashSet;

import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.asSet;
import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.diffKey;
import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.head;
import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.tail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.Difference;
import org.junit.Test;

/**
 * Unit tests for {@link RMCollectionUtils}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class RMCollectionUtilsUnitTest
{
    @Test public void getDuplicateElements()
    {
        List<String> l = asList("A", "B", "C", "B", "A");
        assertEquals("Failed to identify duplicate elements", asList("B", "A"), RMCollectionUtils.getDuplicateElements(l));

        assertEquals(Collections.emptyList(), RMCollectionUtils.getDuplicateElements(asList("A", "B", "C")));
    }

    @Test public void compareMaps()
    {
        // Set up two maps to compare
        final Map<Integer, Integer> mapA = new HashMap<>();
        final Map<Integer, Integer> mapB = new HashMap<>();

        // Fill one map with numbers and their squares...
        for (int i : asList(1, 2, 3, 4, 5))
        {
            mapA.put(i, i*i);
        }

        // ... the other one has the same entries...
        mapB.putAll(mapA);

        // ... but with an addition, a deletion and a value change.
        mapB.put(6, 36);
        mapB.remove(1);
        mapB.put(3, 100);

        // Now ensure that various changes are correctly identified
        assertEquals(Difference.REMOVED,   diffKey(mapA, mapB, 1));
        assertEquals(Difference.ADDED,     diffKey(mapA, mapB, 6));
        assertEquals(Difference.UNCHANGED, diffKey(mapA, mapB, 2));
        assertEquals(Difference.UNCHANGED, diffKey(mapA, mapB, -1));
        assertEquals(Difference.CHANGED,   diffKey(mapA, mapB, 3));
    }

    @Test public void tailsOfLists()
    {
        assertEquals(asList(2), tail(asList(1, 2)));
        assertEquals(emptyList(), tail(asList(1)));
        expectedException(UnsupportedOperationException.class, () -> tail(emptyList()));
    }

    @Test public void headsOfLists()
    {
        assertEquals("a", head(asList("a", "b")));
        assertEquals("a", head(asList("a")));
        assertNull(head(emptyList()));
   }

    @Test public void elementsAsSet()
    {
        assertEquals(newHashSet("hello", "world"), asSet("hello", "world"));
        assertEquals(newHashSet(3, 7, 31, 127), asSet(3, 7, 31, 127));
    }

    @Test public void elementsAsSerializableList()
    {
        // If these lines compile, then we're good
        Serializable s = RMCollectionUtils.<String, ArrayList<String>>asSerializableList("one", "two", "three");
        List<String> l = RMCollectionUtils.<String, ArrayList<String>>asSerializableList("one", "two", "three");

        assertEquals(s, l);
    }

    @Test public void toImmutableSet_nullInput()
    {
        Set<String> output = RMCollectionUtils.toImmutableSet(null);
        assertNull("toImmutableSet should pass through with null.", output);
    }

    @Test public void toImmutableSet_nullElement()
    {
        Set<String> input = newHashSet("One", null, "Three");

        // Call the method under test.
        Set<String> output = RMCollectionUtils.toImmutableSet(input);

        assertEquals("Unexpected handling of null input element", output, newHashSet("One", "Three"));
        assertEquals("Input should not have been changed.", input, newHashSet("One", null, "Three"));
    }
}
