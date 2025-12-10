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
package org.alfresco.repo.nuxeo.mapper;

import junit.framework.TestCase;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.model.NuxeoField;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for AlfrescoToNuxeoPropertyMapper.
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoPropertyMapperTest extends TestCase
{
    private AlfrescoToNuxeoPropertyMapper mapper;
    private MappingContext context;

    public AlfrescoToNuxeoPropertyMapperTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mapper = new AlfrescoToNuxeoPropertyMapper();
        context = new MappingContext();
    }

    public void testMapSimpleTextProperty()
    {
        M2Property property = new M2Property("cm:name");
        property.setType("d:text");
        property.setMandatory(true);
        property.setDescription("Name property");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("name", field.getName());
        assertEquals("string", field.getType());
        assertTrue(field.isRequired());
        assertFalse(field.isMultiValued());
        assertEquals("Name property", field.getDescription());
    }

    public void testMapMultiValuedProperty()
    {
        M2Property property = new M2Property("cm:tags");
        property.setType("d:text");
        property.setMultiValued(true);

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("tags", field.getName());
        assertEquals("string", field.getType());
        assertTrue(field.isMultiValued());
        assertEquals("string[]", field.getFullType());
    }

    public void testMapPropertyWithDefaultValue()
    {
        M2Property property = new M2Property("cm:enabled");
        property.setType("d:boolean");
        property.setDefaultValue("true");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("enabled", field.getName());
        assertEquals("boolean", field.getType());
        assertEquals("true", field.getDefaultValue());
    }

    public void testMapNumericProperty()
    {
        M2Property property = new M2Property("cm:count");
        property.setType("d:int");
        property.setMandatory(false);

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("count", field.getName());
        assertEquals("long", field.getType());
        assertFalse(field.isRequired());
    }

    public void testMapDateProperty()
    {
        M2Property property = new M2Property("cm:created");
        property.setType("d:datetime");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("created", field.getName());
        assertEquals("date", field.getType());
    }

    public void testMapContentProperty()
    {
        M2Property property = new M2Property("cm:content");
        property.setType("d:content");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("content", field.getName());
        assertEquals("blob", field.getType());
    }

    public void testMapPropertyWithDataLoss()
    {
        M2Property property = new M2Property("cm:title");
        property.setType("d:mltext");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("title", field.getName());
        assertEquals("string", field.getType());
        
        // Should add warning about data loss
        assertFalse(context.getWarnings().isEmpty());
        boolean hasMLTextWarning = false;
        for (String warning : context.getWarnings())
        {
            if (warning.contains("mltext") || warning.contains("Multi-lingual"))
            {
                hasMLTextWarning = true;
                break;
            }
        }
        assertTrue("Should have warning about mltext data loss", hasMLTextWarning);
    }

    public void testMapMultipleProperties()
    {
        List<M2Property> properties = new ArrayList<>();
        
        M2Property prop1 = new M2Property("cm:name");
        prop1.setType("d:text");
        properties.add(prop1);
        
        M2Property prop2 = new M2Property("cm:size");
        prop2.setType("d:long");
        properties.add(prop2);
        
        M2Property prop3 = new M2Property("cm:enabled");
        prop3.setType("d:boolean");
        properties.add(prop3);

        List<NuxeoField> fields = mapper.mapProperties(properties, context);

        assertNotNull(fields);
        assertEquals(3, fields.size());
        
        assertEquals("name", fields.get(0).getName());
        assertEquals("size", fields.get(1).getName());
        assertEquals("enabled", fields.get(2).getName());
    }

    public void testMapNullProperty()
    {
        NuxeoField field = mapper.mapProperty(null, context);
        assertNull(field);
    }

    public void testMapEmptyPropertyList()
    {
        List<NuxeoField> fields = mapper.mapProperties(null, context);
        assertNotNull(fields);
        assertTrue(fields.isEmpty());
    }

    public void testExtractLocalNameFromQualifiedName()
    {
        M2Property property = new M2Property("cm:name");
        property.setType("d:text");

        NuxeoField field = mapper.mapProperty(property, context);

        assertEquals("name", field.getName());
    }

    public void testExtractLocalNameFromURIFormat()
    {
        M2Property property = new M2Property("{http://www.alfresco.org/model/content/1.0}name");
        property.setType("d:text");

        NuxeoField field = mapper.mapProperty(property, context);

        assertEquals("name", field.getName());
    }

    public void testMapPropertyWithoutPrefix()
    {
        M2Property property = new M2Property("simpleProperty");
        property.setType("d:text");

        NuxeoField field = mapper.mapProperty(property, context);

        assertNotNull(field);
        assertEquals("simpleProperty", field.getName());
    }
}
