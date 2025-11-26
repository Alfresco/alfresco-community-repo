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
package org.alfresco.repo.nuxeo;

import junit.framework.TestCase;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.nuxeo.config.MappingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Integration tests for NuxeoDataModelMapper.
 *
 * @author Alfresco Data Model Migration Team
 */
public class NuxeoDataModelMapperTest extends TestCase
{
    private NuxeoDataModelMapper mapper;

    public NuxeoDataModelMapperTest(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mapper = new NuxeoDataModelMapper();
    }

    public void testMapEmptyModel()
    {
        M2Model model = M2Model.createModel("test:model");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertTrue(context.getDocumentTypes().isEmpty());
        assertTrue(context.getSchemas().isEmpty());
        assertTrue(context.getFacets().isEmpty());
    }

    public void testMapModelWithNamespace()
    {
        M2Model model = M2Model.createModel("test:model");
        M2Namespace namespace = model.createNamespace("http://example.com/test", "test");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertFalse(context.getNamespaceMap().isEmpty());
        assertEquals("test", context.getPrefix("http://example.com/test"));
    }

    public void testMapModelWithType()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Type type = model.createType("test:document");
        type.setParentName("cm:content");
        type.setDescription("Test document");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertEquals(1, context.getDocumentTypes().size());
        assertNotNull(context.getDocumentType("test:document"));
    }

    public void testMapModelWithAspect()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Aspect aspect = model.createAspect("test:metadata");
        aspect.setDescription("Test metadata aspect");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertEquals(1, context.getFacets().size());
        assertNotNull(context.getFacet("test:metadata"));
    }

    public void testMapCompleteModel()
    {
        M2Model model = M2Model.createModel("test:model");
        
        // Add namespace
        M2Namespace namespace = model.createNamespace("http://example.com/test", "test");
        
        // Add type with properties
        M2Type type = model.createType("test:invoice");
        type.setParentName("cm:content");
        M2Property prop1 = type.createProperty("test:invoiceNumber");
        prop1.setType("d:text");
        prop1.setMandatory(true);
        
        M2Property prop2 = type.createProperty("test:amount");
        prop2.setType("d:double");
        
        // Add mandatory aspect
        type.setMandatoryAspects(new ArrayList<String>());
        type.getMandatoryAspects().add("test:paymentInfo");
        
        // Add aspect with properties
        M2Aspect aspect = model.createAspect("test:paymentInfo");
        M2Property aspectProp = aspect.createProperty("test:paymentMethod");
        aspectProp.setType("d:text");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        
        // Verify namespace
        assertEquals(1, context.getNamespaceMap().size());
        
        // Verify type
        assertEquals(1, context.getDocumentTypes().size());
        assertNotNull(context.getDocumentType("test:invoice"));
        
        // Verify aspect/facet
        assertEquals(1, context.getFacets().size());
        assertNotNull(context.getFacet("test:paymentInfo"));
        
        // Verify schemas created
        assertFalse(context.getSchemas().isEmpty());
    }

    public void testGenerateJSON()
    {
        M2Model model = M2Model.createModel("test:model");
        M2Type type = model.createType("test:doc");
        type.setParentName("cm:content");

        MappingContext context = mapper.mapModel(model);
        String json = mapper.toJSON(context);

        assertNotNull(json);
        assertTrue(json.contains("documentTypes"));
        assertTrue(json.contains("schemas"));
        assertTrue(json.contains("facets"));
        assertTrue(json.contains("test:doc"));
    }

    public void testGenerateSummaryReport()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Type type = model.createType("test:document");
        type.setParentName("cm:content");
        
        M2Aspect aspect = model.createAspect("test:metadata");

        MappingContext context = mapper.mapModel(model);
        String report = mapper.generateSummaryReport(context);

        assertNotNull(report);
        assertTrue(report.contains("Summary"));
        assertTrue(report.contains("Document Types"));
        assertTrue(report.contains("test:document"));
    }

    public void testMapNullModel()
    {
        try
        {
            mapper.mapModel((M2Model) null);
            fail("Should throw IllegalArgumentException for null model");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("cannot be null"));
        }
    }

    public void testMapModelWithMultipleTypes()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Type type1 = model.createType("test:invoice");
        type1.setParentName("cm:content");
        
        M2Type type2 = model.createType("test:purchaseOrder");
        type2.setParentName("cm:content");
        
        M2Type type3 = model.createType("test:receipt");
        type3.setParentName("test:invoice");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertEquals(3, context.getDocumentTypes().size());
    }

    public void testMapModelWithMultipleAspects()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Aspect aspect1 = model.createAspect("test:versionable");
        M2Aspect aspect2 = model.createAspect("test:auditable");
        M2Aspect aspect3 = model.createAspect("test:taggable");

        MappingContext context = mapper.mapModel(model);

        assertNotNull(context);
        assertEquals(3, context.getFacets().size());
    }

    public void testJSONArrayGeneration()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Type type = model.createType("test:doc");
        type.setParentName("cm:content");
        
        // Add multiple mandatory aspects
        List<String> mandatoryAspects = new ArrayList<>();
        mandatoryAspects.add("cm:versionable");
        mandatoryAspects.add("cm:auditable");
        type.setMandatoryAspects(mandatoryAspects);

        MappingContext context = mapper.mapModel(model);
        String json = mapper.toJSON(context);

        assertNotNull(json);
        assertTrue(json.contains("cm:versionable"));
        assertTrue(json.contains("cm:auditable"));
    }

    public void testWarningsInSummaryReport()
    {
        M2Model model = M2Model.createModel("test:model");
        
        M2Aspect aspect = model.createAspect("test:dynamic");
        M2Property prop = aspect.createProperty("test:value");
        prop.setType("d:mltext"); // This should generate a warning

        MappingContext context = mapper.mapModel(model);
        String report = mapper.generateSummaryReport(context);

        assertNotNull(report);
        assertTrue(report.contains("Warnings"));
    }
}
