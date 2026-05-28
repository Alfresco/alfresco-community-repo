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
public class DateFieldMapperTest
{
    private final DateFieldMapper mapper = new DateFieldMapper();

    @Test
    public void shouldBeAbleToMapDates(@TestParameter SupportedDatatype datatype)
    {

        assertTrue(mapper.canMap(new FieldMappingContext(datatype.propertyDefinition)));
    }

    @Test
    public void shouldNotBeAbleToMapOtherDatatypes()
    {
        List<QName> unsupportedDatatypes = List.of(DataTypeDefinition.ENCRYPTED, DataTypeDefinition.TEXT, DataTypeDefinition.MLTEXT, DataTypeDefinition.CONTENT, DataTypeDefinition.INT, DataTypeDefinition.LONG, DataTypeDefinition.BOOLEAN);

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
        DATE(DataTypeDefinition.DATE), DATETIME(DataTypeDefinition.DATETIME);

        final PropertyDefinition propertyDefinition;
        final String expectedMappingFilepath;

        SupportedDatatype(QName datatypeName)
        {
            this.propertyDefinition = mockPropertyDefinition(datatypeName, TEST_PROPERTY_NAME);
            this.expectedMappingFilepath = "date/" + this.name().toLowerCase();
        }
    }
}
