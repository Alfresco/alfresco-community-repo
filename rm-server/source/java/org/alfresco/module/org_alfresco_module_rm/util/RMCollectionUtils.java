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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Various common helper methods for Collections.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
// This class should all be moved to core Alfresco whenever possible and reused from there.
public final class RMCollectionUtils
{
    private RMCollectionUtils() { /* Intentionally empty. */}

    /**
     * Gets the list of duplicate elements contained within the specified list, if any.
     * @param l   the list in which to find duplicates.
     * @param <T> the element type of the list.
     * @return    a list of duplicate elements. If there are no duplicates, returns an empty list.
     */
    public static <T> List<T> getDuplicateElements(List<T> l)
    {
        final Set<T> uniqueElems = new HashSet<>();
        final List<T> duplicateElems = new ArrayList<>();

        for (T elem: l)
        {
            if (uniqueElems.contains(elem))
            {
                if (!duplicateElems.contains(elem)) duplicateElems.add(elem);
            }
            else
            {
                uniqueElems.add(elem);
            }
        }
        return duplicateElems;
    }
}
