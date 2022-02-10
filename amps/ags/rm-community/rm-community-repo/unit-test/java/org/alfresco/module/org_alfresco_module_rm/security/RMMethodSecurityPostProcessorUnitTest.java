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
package org.alfresco.module.org_alfresco_module_rm.security;

import static java.util.Collections.emptyMap;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.junit.Test;

/**
 * Unit tests for {@link RMMethodSecurityPostProcessor}.
 *
 * See RM-7119.
 */
public class RMMethodSecurityPostProcessorUnitTest
{
    /** The class under test. */
    private RMMethodSecurityPostProcessor rmMethodSecurityPostProcessor = new RMMethodSecurityPostProcessor();

    @Test
    public void testConvertToMap_emptyString()
    {
        Map<String, String> actual = rmMethodSecurityPostProcessor.convertToMap("");
        assertEquals("Unexpectedly included empty string in output.", emptyMap(), actual);
    }

    @Test
    public void testConvertToMap_normalPairs()
    {
        Map<String, String> actual = rmMethodSecurityPostProcessor.convertToMap("a=b\nc=d");
        assertEquals("Failed to handle multiline input string.", ImmutableMap.of("a", "b", "c", "d"), actual);
    }

    @Test
    public void testConvertToMap_stripWhitespace()
    {
        Map<String, String> actual = rmMethodSecurityPostProcessor.convertToMap(" \n \t a=b \n \t ");
        assertEquals("Failed to strip whitespace.", ImmutableMap.of("a", "b"), actual);
    }

    @Test
    public void testConvertToMap_ignoreBlankLine()
    {
        Map<String, String> actual = rmMethodSecurityPostProcessor.convertToMap("a=b\n\nc=d");
        assertEquals("Failed to ignore blank line.", ImmutableMap.of("a", "b", "c", "d"), actual);
    }

    @Test
    public void testConvertToMap_multipleEquals()
    {
        Map<String, String> actual = rmMethodSecurityPostProcessor.convertToMap("a=b=c\nd=e=f");
        assertEquals("Issue with handling of = symbol in value.", ImmutableMap.of("a", "b=c", "d", "e=f"), actual);
    }

    /** Check that if a line is missing an equals sign then we get an exception. */
    @Test(expected = AlfrescoRuntimeException.class)
    public void testConvertToMap_missingEquals()
    {
        rmMethodSecurityPostProcessor.convertToMap("a=b\ncd");
    }
}
