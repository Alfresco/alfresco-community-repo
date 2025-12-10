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
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.nuxeo.config.MappingContext;
import org.alfresco.repo.nuxeo.model.NuxeoDocumentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for AlfrescoToNuxeoTypeMapper.
 *
 * @author Alfresco Data Model Migration Team
 */
public class AlfrescoToNuxeoTypeMapperTest extends TestCase
{
    private AlfrescoToNuxeoTypeMapper mapper;
    private MappingContext context;

    public AlfrescoToNuxeoTypeMapperTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mapper = new AlfrescoToNuxeoTypeMapper();
        context = new MappingContext();
    }

    public void testMapSimpleType()
    {
        M2Type type = new M2Type();
        type.setName("custom:document");
        type.setParentName("cm:content");
        type.setDescription("Custom document type");

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("custom:document", docType.getName());
        assertEquals("File", docType.getParent());
        assertEquals("Custom document type", docType.getDescription());
    }

    public void testMapTypeWithProperties()
    {
        M2Type type = new M2Type();
        type.setName("custom:invoice");
        type.setParentName("cm:content");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop1 = new M2Property("custom:invoiceNumber");
        prop1.setType("d:text");
        properties.add(prop1);
        
        M2Property prop2 = new M2Property("custom:amount");
        prop2.setType("d:double");
        properties.add(prop2);
        
        type.setProperties(properties);

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("custom:invoice", docType.getName());
        
        // Should have created a schema
        assertFalse(docType.getSchemas().isEmpty());
        assertEquals(1, docType.getSchemas().size());
        
        // Schema should be in context
        assertFalse(context.getSchemas().isEmpty());
    }

    public void testMapTypeWithMandatoryAspects()
    {
        M2Type type = new M2Type();
        type.setName("custom:document");
        type.setParentName("cm:content");
        
        List<String> mandatoryAspects = new ArrayList<>();
        mandatoryAspects.add("cm:versionable");
        mandatoryAspects.add("cm:auditable");
        type.setMandatoryAspects(mandatoryAspects);

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertFalse(docType.getFacets().isEmpty());
        assertEquals(2, docType.getFacets().size());
        assertTrue(docType.getFacets().contains("cm:versionable"));
        assertTrue(docType.getFacets().contains("cm:auditable"));
    }

    public void testMapTypeWithoutParent()
    {
        M2Type type = new M2Type();
        type.setName("custom:rootType");

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("Document", docType.getParent());
    }

    public void testMapTypeCmFolderParent()
    {
        M2Type type = new M2Type();
        type.setName("custom:folder");
        type.setParentName("cm:folder");

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("Folder", docType.getParent());
    }

    public void testMapTypeCmObjectParent()
    {
        M2Type type = new M2Type();
        type.setName("custom:object");
        type.setParentName("cm:cmobject");

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("Document", docType.getParent());
    }

    public void testMapNullType()
    {
        NuxeoDocumentType docType = mapper.mapType(null, context);
        assertNull(docType);
    }

    public void testMapTypeSchemaNameGeneration()
    {
        M2Type type = new M2Type();
        type.setName("invoice:purchaseOrder");
        type.setParentName("cm:content");
        
        List<M2Property> properties = new ArrayList<>();
        M2Property prop = new M2Property("invoice:poNumber");
        prop.setType("d:text");
        properties.add(prop);
        type.setProperties(properties);

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertFalse(docType.getSchemas().isEmpty());
        
        // Schema name should follow pattern: prefix_localName
        String schemaName = docType.getSchemas().get(0);
        assertTrue(schemaName.contains("invoice"));
        assertTrue(schemaName.contains("purchaseOrder"));
    }

    public void testMapTypeWithEmptyProperties()
    {
        M2Type type = new M2Type();
        type.setName("custom:emptyType");
        type.setParentName("cm:content");
        type.setProperties(new ArrayList<M2Property>());

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        // Should not create schema if no properties
        assertTrue(docType.getSchemas().isEmpty());
    }

    public void testCustomParentMapping()
    {
        context.addCustomMapping("type.parent.cm:content", "CustomFile");
        
        M2Type type = new M2Type();
        type.setName("custom:doc");
        type.setParentName("cm:content");

        NuxeoDocumentType docType = mapper.mapType(type, context);

        assertNotNull(docType);
        assertEquals("CustomFile", docType.getParent());
    }
}
