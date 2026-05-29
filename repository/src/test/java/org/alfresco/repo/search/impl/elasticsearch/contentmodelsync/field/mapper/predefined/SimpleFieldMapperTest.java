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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined;

import static org.junit.Assert.assertTrue;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.TEST_PROPERTY_NAME;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.assertMapsEquals;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.mockPropertyDefinition;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils.readMappingFromFile;

import java.util.List;
import java.util.Map;

import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.ElasticsearchFieldMapper.FieldMappingContext;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.mapper.predefined.utils.MapperTestUtils;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

@RunWith(TestParameterInjector.class)
public class SimpleFieldMapperTest
{
    private final SimpleFieldMapper mapper = new SimpleFieldMapper();

    @Test
    public void shouldBeAbleToMapSimpleDatatypes(@TestParameter SupportedDatatype datatype)
    {
        assertTrue(mapper.canMap(new FieldMappingContext(datatype.propertyDefinition)));
    }

    @Test
    public void shouldNotBeAbleToMapOtherDatatypes()
    {
        List<QName> unsupportedDatatypes = List.of(DataTypeDefinition.DATE, DataTypeDefinition.DATETIME, DataTypeDefinition.ENCRYPTED, DataTypeDefinition.TEXT, DataTypeDefinition.MLTEXT, DataTypeDefinition.CONTENT);

        unsupportedDatatypes.stream()
                .map(MapperTestUtils::mockPropertyDefinition)
                .map(FieldMappingContext::new)
                .map(mapper::canMap)
                .forEach(Assert::assertFalse);
    }

    @Test
    public void shouldProperlyMapDatatypes(@TestParameter SupportedDatatype datatype)
    {
        Map<?, ?> expectedMapping = readMappingFromFile(datatype.expectedMappingFilepath);

        FieldMappingContext context = new FieldMappingContext(datatype.propertyDefinition);

        Map<?, ?> actualMapping = mapper.buildMapping(context).asMap();

        assertMapsEquals(expectedMapping, actualMapping);
    }

    public enum SupportedDatatype
    {
        INT(DataTypeDefinition.INT), LONG(DataTypeDefinition.LONG), FLOAT(DataTypeDefinition.FLOAT), DOUBLE(DataTypeDefinition.DOUBLE), BOOLEAN(DataTypeDefinition.BOOLEAN), NODE_REF(DataTypeDefinition.NODE_REF), CATEGORY(DataTypeDefinition.CATEGORY);

        final PropertyDefinition propertyDefinition;
        final String expectedMappingFilepath;

        SupportedDatatype(QName datatypeName)
        {
            this.propertyDefinition = mockPropertyDefinition(datatypeName, TEST_PROPERTY_NAME);
            this.expectedMappingFilepath = "simple/" + this.name()
                    .toLowerCase();
        }
    }
}
