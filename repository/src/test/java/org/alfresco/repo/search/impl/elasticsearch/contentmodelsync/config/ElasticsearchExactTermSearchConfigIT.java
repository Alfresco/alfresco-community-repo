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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

@TestPropertySource("classpath:/alfresco/search/elasticsearch/config/exactTermSearch.properties")
public class ElasticsearchExactTermSearchConfigIT extends ElasticsearchSpringTest
{
    ElasticsearchExactTermSearchConfig config;

    QName testName = QName.createQName("namespace", "name");

    @Override
    @Before
    public void setUp() throws Exception
    {
        config = elasticsearchContext.getBean(ElasticsearchExactTermSearchConfig.class);
    }

    @Test
    public void shouldReturnTrueIfExactTermSearchIsEnabledForProperty()
    {
        QName name = QName.createQName("http://www.alfresco.org/model/content/1.0", "content");
        PropertyDefinition propertyDefinition = mockedPropertyDefinition(name, DataTypeDefinition.CONTENT);

        assertTrue(String.format("Exact term search should be enabled for property with name %s", name.getLocalName()), config.isExactTermSearchEnabled(propertyDefinition));
    }

    @Test
    public void shouldReturnTrueIfExactTermSearchIsEnabledForDataType()
    {
        PropertyDefinition textProperty = mockedPropertyDefinition(testName, DataTypeDefinition.TEXT);
        PropertyDefinition multilanguageTextProperty = mockedPropertyDefinition(testName, DataTypeDefinition.MLTEXT);

        String failureMessage = "Exact term search should be enabled for property with datatype %s";
        assertTrue(String.format(failureMessage, textProperty.getDataType().getName()), config.isExactTermSearchEnabled(textProperty));
        assertTrue(String.format(failureMessage, multilanguageTextProperty.getDataType().getName()), config.isExactTermSearchEnabled(multilanguageTextProperty));
    }

    @Test
    public void shouldReturnFalseIfExactTermSearchIsNotEnabledForPropertyOrDataType()
    {
        QName testName = QName.createQName("namespace", "exactTermSearchDisabledName");
        PropertyDefinition propertyDefinition = mockedPropertyDefinition(testName, DataTypeDefinition.INT);

        String failureMessage = String.format("Exact term search should not be enabled for property with name %s and datatype %s", testName, DataTypeDefinition.INT);
        assertFalse(failureMessage, config.isExactTermSearchEnabled(propertyDefinition));
    }

    private PropertyDefinition mockedPropertyDefinition(QName name, QName dataTypeName)
    {
        DataTypeDefinition dataTypeDefinition = mock();
        when(dataTypeDefinition.getName()).thenReturn(dataTypeName);

        PropertyDefinition propertyDefinition = mock();
        when(propertyDefinition.getName()).thenReturn(name);
        when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);

        return propertyDefinition;
    }
}
