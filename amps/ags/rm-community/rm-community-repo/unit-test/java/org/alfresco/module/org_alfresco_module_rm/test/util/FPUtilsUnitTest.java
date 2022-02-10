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
import static java.util.Collections.emptyList;

import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asListFrom;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSetFrom;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for {@link FPUtils}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class FPUtilsUnitTest
{
    @Test public void asListShouldProduceList()
    {
        List<String> l = asListFrom(() -> "hello",
                                    () -> "world",
                                    () -> {
                                        String s1 = "abc";
                                        String s2 = "xyz";
                                        return s1 + s2;
                                    });
        assertEquals(asList("hello", "world", "abcxyz"), l);
    }

    @Test public void asListShouldWorkForEmptyVarArgs()
    {
        assertEquals(emptyList(), FPUtils.<String>asListFrom());
    }

    @Test public void asSetShouldProduceSet()
    {
        assertEquals(new HashSet<>(asList("hello", "world")),
                     asSet("hello", "hello", "world"));
    }

    @Test public void asSetFromShouldWork()
    {
        Set<String> s = asSetFrom(() -> "hello",
                                  () -> "hello",
                                  () -> "world",
                                  () -> {
                                      String s1 = "wo";
                                      String s2 = "rld";
                                      return s1 + s2;
                                  });
        assertEquals(new HashSet<>(asList("hello", "world")), s);
    }
}
