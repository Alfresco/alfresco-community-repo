/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import static java.lang.String.format;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.custom.CustomFieldMapper;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.PredefinedFieldMapper;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

public class FieldMappingBuilderTest
{
    private static final String TEST_INDEX_NAME = "test_name";

    @Test
    public void shouldUseCustomFieldMapperFirst()
    {
        CustomFieldMapper customFieldMapper = mockedCustomFieldMapper(true);
        PredefinedFieldMapper defaultFieldMapper = mockedPredefinedFieldMapper(true);

        FieldMappingBuilder fieldMappingBuilder = new FieldMappingBuilder(List.of(defaultFieldMapper), List.of(customFieldMapper));

        fieldMappingBuilder.buildFieldsMappings(TEST_INDEX_NAME, List.of(mockedPropertyDefinition()));

        verify(customFieldMapper, times(1)).buildMapping(any());
        verify(defaultFieldMapper, times(0)).buildMapping(any());
    }

    @Test
    public void ifNoCustomMapperCanMapPropertyUseDefaultMappers()
    {
        CustomFieldMapper customFieldMapper = mockedCustomFieldMapper(false);
        PredefinedFieldMapper defaultFieldMapper = mockedPredefinedFieldMapper(true);

        FieldMappingBuilder fieldMappingBuilder = new FieldMappingBuilder(List.of(defaultFieldMapper), List.of(customFieldMapper));

        fieldMappingBuilder.buildFieldsMappings(TEST_INDEX_NAME, List.of(mockedPropertyDefinition()));

        verify(customFieldMapper, times(0)).buildMapping(any());
        verify(defaultFieldMapper, times(1)).buildMapping(any());
    }

    @Test
    public void shouldCountIndexedProperties()
    {
        FieldMappingBuilder fieldMappingBuilder = new FieldMappingBuilder(List.of(), List.of());

        List<PropertyDefinition> propertyDefinitions = List.of(
                mockedPropertyDefinition(true),
                mockedPropertyDefinition(true),
                mockedPropertyDefinition(false));

        int indexedPropertiesNumber = 2;

        var result = fieldMappingBuilder.buildFieldsMappings(TEST_INDEX_NAME, propertyDefinitions);

        assertEquals(format("Number of indexed properties should be %s but is %s", indexedPropertiesNumber, result.getSecond()), indexedPropertiesNumber, (int) result.getSecond());
    }

    private PredefinedFieldMapper mockedPredefinedFieldMapper(boolean canMap)
    {
        PredefinedFieldMapper fieldMapper = mock();

        when(fieldMapper.canMap(any())).thenReturn(canMap);
        when(fieldMapper.buildMapping(any())).thenReturn(new ElasticsearchFieldMapping(new FieldName("acme:test")));

        return fieldMapper;
    }

    private CustomFieldMapper mockedCustomFieldMapper(boolean canMap)
    {
        CustomFieldMapper fieldMapper = mock();

        when(fieldMapper.canMap(any())).thenReturn(canMap);
        when(fieldMapper.buildMapping(any())).thenReturn(new ElasticsearchFieldMapping(new FieldName("acme:test")));

        return fieldMapper;
    }

    private PropertyDefinition mockedPropertyDefinition()
    {
        return mockedPropertyDefinition(true);
    }

    private PropertyDefinition mockedPropertyDefinition(boolean isIndexed)
    {
        PropertyDefinition propertyDefinition = mock();

        when(propertyDefinition.isIndexed()).thenReturn(isIndexed);
        when(propertyDefinition.getName()).thenReturn(QName.createQName("acme:test"));

        return propertyDefinition;
    }
}
