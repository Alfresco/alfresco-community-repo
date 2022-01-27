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

package org.alfresco.module.org_alfresco_module_rm.forms;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * RecordsManagementTypeFormFilter Unit Test
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class RecordsManagementTypeFormFilterUnitTest extends BaseUnitTest
{
    private static final QName MY_CUSTOM_TYPE = generateQName(RM_URI);
    
    @Mock private Form mockForm;
    @Mock private TypeDefinition mockTypeDefinition;
    @Mock(name="recordsManagementAdminService") private RecordsManagementAdminService mockRecordsManagementAdminService;
    
    @Spy @InjectMocks RecordsManagementTypeFormFilter typeFormFilter;

    /**
     * Test addCustomRMProperties - no custom properties found
     */
    @Test
    public void testAddCustomRMPropertiesNoneFound()
    {        
        typeFormFilter.addCustomRMProperties(MY_CUSTOM_TYPE, mockForm);
        verifyNoMoreInteractions(mockForm);
    }
    
    /**
     * Test that non-customisable types are being treated correctly
     */
    @Test
    public void testAfterGenerateNotCustomisable()
    {
        when(mockTypeDefinition.getName()).thenReturn(MY_CUSTOM_TYPE);
        when(mockRecordsManagementAdminService.isCustomisable(MY_CUSTOM_TYPE)).thenReturn(false);
        
        typeFormFilter.afterGenerate(mockTypeDefinition, null, null, mockForm, null);
        
        verify(typeFormFilter, never()).addCustomRMProperties(any(QName.class), any(Form.class));
    }
    
    /**
     * Test that customisable types are being treated correctly
     */
    @Test
    public void testAfterGenerateCustomisable()
    {
        when(mockTypeDefinition.getName()).thenReturn(MY_CUSTOM_TYPE);
        when(mockRecordsManagementAdminService.isCustomisable(MY_CUSTOM_TYPE)).thenReturn(true);
        
        typeFormFilter.afterGenerate(mockTypeDefinition, null, null, mockForm, null);
        
        verify(typeFormFilter, times(1)).addCustomRMProperties(any(QName.class), any(Form.class));
    }
    
    /**
     * Test the default values for certain properties are being set correctly
     */
    @Test
    public void testDefaultFormValues()
    {
        List<FieldDefinition> defs = new ArrayList<>(3);
        FieldDefinition idDef = mockFieldDefinition("rma:identifier");
        defs.add(idDef);
        FieldDefinition vrDef = mockFieldDefinition("rma:vitalRecordIndicator");
        defs.add(vrDef);
        FieldDefinition rpDef = mockFieldDefinition("rma:reviewPeriod");
        defs.add(rpDef);
        
        when(mockForm.getFieldDefinitions()).thenReturn(defs);
        
        typeFormFilter.afterGenerate(mockTypeDefinition, null, null, mockForm, null);
        
        verify(mockedIdentifierService).generateIdentifier(nullable(QName.class), nullable(NodeRef.class));
        verify(idDef).setDefaultValue(nullable(String.class));
        verify(vrDef).setDefaultValue(Boolean.FALSE.toString());
        verify(rpDef).setDefaultValue("none|0");
    }
    
    /**
     * Helper to mock field definition
     */
    private FieldDefinition mockFieldDefinition(String name)
    {
        FieldDefinition def = mock(FieldDefinition.class);
        when(def.getName()).thenReturn(name);
        return def;
    }
        
    /**
     * Test addCustomRMProperties - two custom properties found
     */
    @Test
    public void testAddCustomRMProperties()
    {        
        // map of custom properties
        Map<QName, PropertyDefinition> properties = mockPropertyDefintionMap(2);
        
        // setup rm admin service to return properties for my custom type
        when(mockRecordsManagementAdminService.getCustomPropertyDefinitions(MY_CUSTOM_TYPE)).thenReturn(properties);
        
        // call method
        typeFormFilter.addCustomRMProperties(MY_CUSTOM_TYPE, mockForm);
        
        // ensure that two custom properties have been added to the form
        verify(mockForm, times(1)).addFields(anyList());
    }
    
    /**
     * Helper method to createa a mock property definition map
     */
    private Map<QName, PropertyDefinition> mockPropertyDefintionMap(int size)
    {
        Map<QName, PropertyDefinition> properties = new HashMap<>(size);
        for (int i = 0; i < size; i++)
        {
            QName name = generateQName(RM_URI);
            PropertyDefinition propDef = mock(PropertyDefinition.class);
            when(propDef.getName()).thenReturn(name);
            DataTypeDefinition mockDataTypeDefinition = mock(DataTypeDefinition.class);
            when(mockDataTypeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
            when(propDef.getDataType()).thenReturn(mockDataTypeDefinition);
            properties.put(name, propDef);
        }        
        return properties;        
    }
}
