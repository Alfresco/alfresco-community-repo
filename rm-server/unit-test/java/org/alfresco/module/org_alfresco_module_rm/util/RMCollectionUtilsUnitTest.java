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

package org.alfresco.module.org_alfresco_module_rm.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link RMCollectionUtils}.
 * 
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class RMCollectionUtilsUnitTest
{
    @Test public void getDuplicateElements()
    {
        List<String> l = asList("A", "B", "C", "B", "A");
        assertEquals("Failed to identify duplicate elements", asList("B", "A"), RMCollectionUtils.getDuplicateElements(l));

        assertEquals(Collections.emptyList(), RMCollectionUtils.getDuplicateElements(asList("A", "B", "C")));
    }
}
