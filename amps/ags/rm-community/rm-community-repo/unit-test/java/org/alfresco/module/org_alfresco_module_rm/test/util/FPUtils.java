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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utility class to help with Java 8 FP stuff.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class FPUtils
{
    /**
     * This method is intended to work exactly like {@code java.util.Arrays.asList()} but it takes
     * a vararg of {@code Supplier}s instead of actual objects.
     *
     * @param suppliers a vararg of {@link Supplier}s giving a sequence of values for the list.
     * @param <T> the type of elements in the list.
     * @return the list with each element being the first retrieved from a {@code Supplier}.
     */
    @SafeVarargs
    public static <T> List<T> asListFrom(Supplier<T>... suppliers)
    {
        if (suppliers == null || suppliers.length == 0)
        {
            return Collections.emptyList();
        }
        else
        {
            return Stream.of(suppliers)
                         .map(s -> s.get())
                         .collect(toList());
        }
    }

    /**
     * This method is intended to work exactly like {@link #asSet(Object[])}} but it takes
     * a vararg of {@code Supplier}s instead of actual objects.
     *
     * @param suppliers a vararg of {@link Supplier}s giving a sequence of values for the set.
     * @param <T> the type of elements in the set.
     * @return the set with each element being the first retrieved from a {@code Supplier} (duplicates removed).
     */
    @SafeVarargs
    public static <T> Set<T> asSetFrom(Supplier<T>... suppliers)
    {
        List<T> l = asListFrom(suppliers);
        return new HashSet<>(l);
    }

    /**
     * This utility method converts a vararg of objects into a Set<T>.
     *
     * @param objects the objects to be added to the set
     * @return a Set of objects (any equal objects will of course not be duplicated)
     */
    @SafeVarargs
    public static <T> Set<T> asSet(T... objects)
    {
        return new HashSet<>(asList(objects));
    }
}
