/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.FormCreationDataImpl;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @since 3.4
 * @author Nick Smith
 *
 */
public class FieldProcessorTest extends TestCase
{
    private static final String PREFIX = "test";
    private static final String URI = "http://test/";
    private static final String NAME1 = "Name1";
    private static final String NAME2 = "Name2";
    private static final String DESCRIPTION1 = "Description";
    private static final String DESCRIPTION2 = "Another Description";
    private static final String TITLE = "Title";
    private static final QName qName1 = QName.createQName(URI, NAME1);
    private static final QName qName2 = QName.createQName(URI, NAME2);

    private NamespaceService namespaceService;
    private FormCreationData data;

    public void testMakeAssociationFieldDefinition() throws Exception
    {
        AssociationFieldProcessor processor = new AssociationFieldProcessor();
        processor.setNamespaceService(namespaceService);

        String name1 = ASSOC + ":"+ PREFIX +":"+ NAME1;
        Field field = processor.generateField(name1, data);
        AssociationFieldDefinition assocFieldDef = (AssociationFieldDefinition) field.getFieldDefinition();

        assertNotNull(assocFieldDef);
        assertEquals("assoc_" + PREFIX + "_" + NAME1, assocFieldDef.getDataKeyName());
        assertEquals(PREFIX + ":" + NAME1, assocFieldDef.getName());
        assertEquals(PREFIX + ":" + NAME1, assocFieldDef.getLabel());
        assertEquals(Direction.TARGET, assocFieldDef.getEndpointDirection());
        assertEquals(PREFIX + ":Target", assocFieldDef.getEndpointType());
        assertEquals(DESCRIPTION1, assocFieldDef.getDescription());
        assertFalse(assocFieldDef.isProtectedField());
        assertFalse(assocFieldDef.isEndpointMandatory());
        assertFalse(assocFieldDef.isEndpointMany());

        // Repeat using different params to ensuere the fieldDefinition values
        // are dependant on the AssociationDefinition values.
        String name2 = ASSOC + ":" + PREFIX +":"+ NAME2;
        field = processor.generateField(name2, data);
        assocFieldDef = (AssociationFieldDefinition) field.getFieldDefinition();

        assertEquals(TITLE, assocFieldDef.getLabel());
        assertEquals(DESCRIPTION2, assocFieldDef.getDescription());
        assertTrue(assocFieldDef.isProtectedField());
        assertTrue(assocFieldDef.isEndpointMandatory());
        assertTrue(assocFieldDef.isEndpointMany());
    }

    public void testMakePropertyFieldDefinition() throws Exception
    {
        PropertyFieldProcessor processor = new PropertyFieldProcessor();
        processor.setNamespaceService(namespaceService);
        
        String name1 = PROP+ ":" + PREFIX + ":" + NAME1;
        Field field = processor.generateField(name1, data);
        PropertyFieldDefinition propFieldDef = (PropertyFieldDefinition) field.getFieldDefinition();
        assertNotNull(propFieldDef);
        assertEquals("prop_" + PREFIX + "_" + NAME1, propFieldDef.getDataKeyName());
        assertEquals(PREFIX + ":" + NAME1, propFieldDef.getName());
        assertEquals(PREFIX + ":" + NAME1, propFieldDef.getLabel());
        assertEquals("Default1", propFieldDef.getDefaultValue());
        assertEquals(DESCRIPTION1, propFieldDef.getDescription());
        assertFalse(propFieldDef.isProtectedField());
        assertFalse(propFieldDef.isMandatory());
        assertFalse(propFieldDef.isRepeating());// Maps to isMultiValued() on

        // Repeat using different params to ensuere the fieldDefinition values
        // are dependant on the PropertyDefinition values.
        String name2 = PROP + ":" + PREFIX + ":" + NAME2;
        field = processor.generateField(name2, data);
        propFieldDef = (PropertyFieldDefinition) field.getFieldDefinition();
        assertEquals(TITLE, propFieldDef.getLabel());
        assertEquals(DESCRIPTION2, propFieldDef.getDescription());
        assertEquals("Default2", propFieldDef.getDefaultValue());
        assertTrue(propFieldDef.isProtectedField());
        assertTrue(propFieldDef.isMandatory());
        assertTrue(propFieldDef.isRepeating());
    }

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        namespaceService = makeNamespaceService();
        data = new FormCreationDataImpl(makeItemData(), null,  null);
    }

    private ContentModelItemData<Void> makeItemData()
    {
        Map<QName, PropertyDefinition> propDefs = makePropertyDefs();
        Map<QName, AssociationDefinition> assocDefs = makeAssociationDefs();
        
        Map<QName, Serializable> propValues = new HashMap<QName, Serializable>();
        Map<QName, Serializable> assocValues = new HashMap<QName, Serializable>();
        Map<String, Object> transientValues = new HashMap<String, Object>();
        return new ContentModelItemData<Void>(null, propDefs, assocDefs, propValues, assocValues, transientValues);
    }

    private Map<QName, AssociationDefinition> makeAssociationDefs()
    {
        QName targetClass = QName.createQName(URI, "Target");
        AssociationDefinition assocDef1 = MockClassAttributeDefinition.mockAssociationDefinition(
                qName1, targetClass,
                null,// Defalt title, so sets label to be same as name.
                DESCRIPTION1, false, false, false);
        MockClassAttributeDefinition assocDef2 = MockClassAttributeDefinition.mockAssociationDefinition(
                qName2, targetClass,
                TITLE, DESCRIPTION2,
                true, true, true);
        Map<QName, AssociationDefinition> assocDefs = new HashMap<QName, AssociationDefinition>();
        assocDefs.put(qName1, assocDef1);
        assocDefs.put(qName2, assocDef2);
        return assocDefs;
    }

    private Map<QName, PropertyDefinition> makePropertyDefs()
    {
        QName dataTypeName = QName.createQName(URI, "Type");
        PropertyDefinition propDef1 = MockClassAttributeDefinition.mockPropertyDefinition(
                qName1, dataTypeName,
                null,// Defalt title, so sets label to be same as name.
                DESCRIPTION1, false,
                "Default1", false, false);
        PropertyDefinition propDef2 = MockClassAttributeDefinition.mockPropertyDefinition(
                qName2, dataTypeName,
                TITLE,
                DESCRIPTION2, true,
                "Default2", true, true);
        Map<QName, PropertyDefinition> propDefs = new HashMap<QName, PropertyDefinition>();
        propDefs.put(qName1, propDef1);
        propDefs.put(qName2, propDef2);
        return propDefs;
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl nsService = new NamespaceServiceMemoryImpl();
        nsService.registerNamespace(PREFIX, URI);
        return nsService;
    }

}
