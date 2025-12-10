/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.nuxeo.converter;

import junit.framework.TestCase;

/**
 * Tests for DataTypeConverter.
 *
 * @author Alfresco Data Model Migration Team
 */
public class DataTypeConverterTest extends TestCase
{
    public DataTypeConverterTest(String name)
    {
        super(name);
    }

    public void testConvertTextType()
    {
        assertEquals("string", DataTypeConverter.convertType("d:text"));
        assertEquals("string", DataTypeConverter.convertType("d:mltext"));
    }

    public void testConvertNumericTypes()
    {
        assertEquals("long", DataTypeConverter.convertType("d:int"));
        assertEquals("long", DataTypeConverter.convertType("d:long"));
        assertEquals("double", DataTypeConverter.convertType("d:float"));
        assertEquals("double", DataTypeConverter.convertType("d:double"));
    }

    public void testConvertBooleanType()
    {
        assertEquals("boolean", DataTypeConverter.convertType("d:boolean"));
    }

    public void testConvertDateTypes()
    {
        assertEquals("date", DataTypeConverter.convertType("d:date"));
        assertEquals("date", DataTypeConverter.convertType("d:datetime"));
    }

    public void testConvertContentType()
    {
        assertEquals("blob", DataTypeConverter.convertType("d:content"));
    }

    public void testConvertReferenceTypes()
    {
        assertEquals("string", DataTypeConverter.convertType("d:noderef"));
        assertEquals("string", DataTypeConverter.convertType("d:qname"));
        assertEquals("string", DataTypeConverter.convertType("d:path"));
        assertEquals("string", DataTypeConverter.convertType("d:category"));
    }

    public void testConvertNullType()
    {
        assertEquals("string", DataTypeConverter.convertType(null));
    }

    public void testConvertEmptyType()
    {
        assertEquals("string", DataTypeConverter.convertType(""));
    }

    public void testConvertUnknownType()
    {
        assertEquals("string", DataTypeConverter.convertType("d:unknown"));
        assertEquals("string", DataTypeConverter.convertType("custom:type"));
    }

    public void testHasDataLoss()
    {
        assertTrue(DataTypeConverter.hasDataLoss("d:mltext"));
        assertTrue(DataTypeConverter.hasDataLoss("d:noderef"));
        assertTrue(DataTypeConverter.hasDataLoss("d:qname"));
        
        assertFalse(DataTypeConverter.hasDataLoss("d:text"));
        assertFalse(DataTypeConverter.hasDataLoss("d:int"));
        assertFalse(DataTypeConverter.hasDataLoss("d:boolean"));
    }

    public void testGetDataLossDescription()
    {
        assertNotNull(DataTypeConverter.getDataLossDescription("d:mltext"));
        assertNotNull(DataTypeConverter.getDataLossDescription("d:noderef"));
        assertNotNull(DataTypeConverter.getDataLossDescription("d:qname"));
        
        assertNull(DataTypeConverter.getDataLossDescription("d:text"));
        assertNull(DataTypeConverter.getDataLossDescription("d:int"));
    }

    public void testIsSupported()
    {
        assertTrue(DataTypeConverter.isSupported("d:text"));
        assertTrue(DataTypeConverter.isSupported("d:int"));
        assertTrue(DataTypeConverter.isSupported("d:boolean"));
        assertTrue(DataTypeConverter.isSupported("d:date"));
        assertTrue(DataTypeConverter.isSupported("d:content"));
        
        assertFalse(DataTypeConverter.isSupported(null));
    }

    public void testGetTypeMappings()
    {
        assertNotNull(DataTypeConverter.getTypeMappings());
        assertFalse(DataTypeConverter.getTypeMappings().isEmpty());
        assertTrue(DataTypeConverter.getTypeMappings().size() > 10);
    }

    public void testTypeMappingConsistency()
    {
        // Verify that all mapped types are consistent
        for (String alfrescoType : DataTypeConverter.getTypeMappings().keySet())
        {
            String nuxeoType = DataTypeConverter.convertType(alfrescoType);
            assertNotNull("Nuxeo type should not be null for " + alfrescoType, nuxeoType);
            assertFalse("Nuxeo type should not be empty for " + alfrescoType, nuxeoType.isEmpty());
        }
    }
}
