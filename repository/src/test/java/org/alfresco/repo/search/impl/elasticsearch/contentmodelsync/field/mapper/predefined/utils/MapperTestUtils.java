/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils;

import static java.lang.String.format;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

public final class MapperTestUtils
{
    public static final String TEST_PROPERTY_NAME = "test:property";

    private static String basePath = "/alfresco/search/elasticsearch/mappings/";

    public static PropertyDefinition mockPropertyDefinition(QName typeName)
    {
        return mockPropertyDefinition(typeName, "");
    }

    public static PropertyDefinition mockPropertyDefinition(QName typeName, String propertyName)
    {
        DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
        when(dataTypeDefinition.getName()).thenReturn(typeName);

        QName qualifiedPropertyName = mock(QName.class);
        when(qualifiedPropertyName.getPrefixString()).thenReturn(propertyName);

        PropertyDefinition propertyDefinition = mock(PropertyDefinition.class);
        when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
        when(propertyDefinition.getName()).thenReturn(qualifiedPropertyName);

        return propertyDefinition;
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public static Map<?, ?> readMappingFromFile(String fileName)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        String path = basePath + fileName + ".json";

        try (InputStream inputStream = MapperTestUtils.class.getResourceAsStream(path))
        {
            return objectMapper.readValue(inputStream, Map.class);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static void assertMapsEquals(Map<?, ?> expected, Map<?, ?> actual)
    {
        MapDifference<?, ?> difference = Maps.difference(expected, actual);

        Map<?, ?> missingExpected = difference.entriesOnlyOnLeft();
        Map<?, ?> additional = difference.entriesOnlyOnRight();

        String failureMessage = format("""
                Expected and actual are not equal

                expected:   %s
                actual:     %s

                missing:    %s
                additional: %s
                """, expected, actual, missingExpected, additional);

        var diffs = findDifferences(expected, actual);
        printDifferences(diffs, "");

        assertTrue(failureMessage, difference.areEqual());
    }

    public static Map findDifferences(Map map1, Map map2)
    {
        Map differences = new HashMap<>();

        for (Object key : map1.keySet())
        {
            if (!map2.containsKey(key))
            {
                differences.put(key, map1.get(key));
            }
            else
            {
                Object value1 = map1.get(key);
                Object value2 = map2.get(key);

                if (value1 instanceof Map && value2 instanceof Map)
                {
                    Map nestedDifferences = findDifferences((Map) value1, (Map) value2);
                    if (!nestedDifferences.isEmpty())
                    {
                        differences.put(key, nestedDifferences);
                    }
                }
                else if (!Objects.equals(value1, value2))
                {
                    differences.put(key, value1);
                }
            }
        }

        for (Object key : map2.keySet())
        {
            if (!map1.containsKey(key))
            {
                differences.put(key, map2.get(key));
            }
        }

        return differences;
    }

    @SuppressWarnings("PMD")
    public static void printDifferences(Map<String, Object> differences, String prefix)
    {
        for (Map.Entry<String, Object> entry : differences.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map)
            {
                System.err.println(prefix + key + ":");
                printDifferences((Map<String, Object>) value, prefix + "  ");
            }
            else
            {
                System.err.println(prefix + key + ": " + value);
            }
        }
    }

    private MapperTestUtils()
    {}
}
