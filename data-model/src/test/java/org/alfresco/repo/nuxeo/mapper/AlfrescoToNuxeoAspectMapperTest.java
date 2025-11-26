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
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.model.NuxeoFacet;
import org.alfresco.repo.nuxeo.model.NuxeoSchema;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for AlfrescoToNuxeoAspectMapper.
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoAspectMapperTest extends TestCase
{
    private AlfrescoToNuxeoAspectMapper mapper;
    private MappingContext context;

    public AlfrescoToNuxeoAspectMapperTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mapper = new AlfrescoToNuxeoAspectMapper();
        context = new MappingContext();
    }

    public void testMapSimpleAspectToFacet()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("cm:versionable");
        aspect.setDescription("Versionable aspect");

        NuxeoFacet facet = mapper.mapAspectToFacet(aspect, context);

        assertNotNull(facet);
        assertEquals("cm:versionable", facet.getName());
        assertEquals("Versionable aspect", facet.getDescription());
    }

    public void testMapAspectWithPropertiesToFacet()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("custom:auditable");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("custom:auditDate");
        prop.setType("d:datetime");
        properties.add(prop);
        aspect.setProperties(properties);

        NuxeoFacet facet = mapper.mapAspectToFacet(aspect, context);

        assertNotNull(facet);
        assertEquals("custom:auditable", facet.getName());
        assertFalse(facet.getSchemas().isEmpty());
        
        // Should include schema name
        assertTrue(facet.getSchemas().get(0).contains("custom"));
        assertTrue(facet.getSchemas().get(0).contains("auditable"));
    }

    public void testMapAspectToSchema()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("custom:paymentInfo");
        aspect.setDescription("Payment information");
        
        List<M2Property> properties = new ArrayList<>();
        
        M2Property prop1 = new M2Property("custom:paymentMethod");
        prop1.setType("d:text");
        properties.add(prop1);
        
        M2Property prop2 = new M2Property("custom:paymentDate");
        prop2.setType("d:date");
        properties.add(prop2);
        
        aspect.setProperties(properties);

        NuxeoSchema schema = mapper.mapAspectToSchema(aspect, context);

        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertNotNull(schema.getPrefix());
        assertEquals(2, schema.getFields().size());
        assertTrue(schema.getDescription().contains("aspect"));
    }

    public void testMapAspectBoth()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("custom:metadata");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("custom:metaValue");
        prop.setType("d:text");
        properties.add(prop);
        aspect.setProperties(properties);

        mapper.mapAspect(aspect, context);

        // Both facet and schema should be in context
        assertNotNull(context.getFacet("custom:metadata"));
        assertFalse(context.getSchemas().isEmpty());
    }

    public void testMapAspectWithoutProperties()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("cm:taggable");
        aspect.setProperties(new ArrayList<M2Property>());

        mapper.mapAspect(aspect, context);

        // Facet should be created
        assertNotNull(context.getFacet("cm:taggable"));
        
        // Schema should not be created if no properties
        assertTrue(context.getSchemas().isEmpty());
    }

    public void testMapNullAspect()
    {
        NuxeoFacet facet = mapper.mapAspectToFacet(null, context);
        assertNull(facet);
        
        NuxeoSchema schema = mapper.mapAspectToSchema(null, context);
        assertNull(schema);
    }

    public void testAspectMappingWarning()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("custom:dynamic");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("custom:value");
        prop.setType("d:text");
        properties.add(prop);
        aspect.setProperties(properties);

        mapper.mapAspect(aspect, context);

        // Should have warning about dynamic aspect application
        assertFalse(context.getWarnings().isEmpty());
        boolean hasDynamicWarning = false;
        for (String warning : context.getWarnings())
        {
            if (warning.contains("dynamic") || warning.contains("facets"))
            {
                hasDynamicWarning = true;
                break;
            }
        }
        assertTrue("Should have warning about dynamic facets", hasDynamicWarning);
    }

    public void testSchemaNameGeneration()
    {
        M2Aspect aspect = new M2Aspect();
        aspect.setName("invoice:paymentInfo");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("invoice:amount");
        prop.setType("d:double");
        properties.add(prop);
        aspect.setProperties(properties);

        NuxeoSchema schema = mapper.mapAspectToSchema(aspect, context);

        assertNotNull(schema);
        assertNotNull(schema.getName());
        assertTrue(schema.getName().contains("invoice"));
        assertTrue(schema.getName().contains("paymentInfo"));
        assertEquals("invoice", schema.getPrefix());
    }

    public void testMultipleAspects()
    {
        M2Aspect aspect1 = new M2Aspect();
        aspect1.setName("cm:versionable");
        
        M2Aspect aspect2 = new M2Aspect();
        aspect2.setName("cm:auditable");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("cm:created");
        prop.setType("d:datetime");
        properties.add(prop);
        aspect2.setProperties(properties);

        mapper.mapAspect(aspect1, context);
        mapper.mapAspect(aspect2, context);

        assertEquals(2, context.getFacets().size());
        assertNotNull(context.getFacet("cm:versionable"));
        assertNotNull(context.getFacet("cm:auditable"));
    }
}
