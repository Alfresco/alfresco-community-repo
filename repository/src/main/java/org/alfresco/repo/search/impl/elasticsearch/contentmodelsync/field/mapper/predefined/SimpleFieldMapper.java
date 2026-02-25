/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping.mappingWithMetadata;

import java.util.Map;

import org.springframework.stereotype.Component;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

@Component
public class SimpleFieldMapper implements PredefinedFieldMapper
{
    private static final Map<QName, ElasticsearchType> supportedTypes = Map.of(
            DataTypeDefinition.INT, ElasticsearchType.INT,
            DataTypeDefinition.LONG, ElasticsearchType.LONG,
            DataTypeDefinition.FLOAT, ElasticsearchType.FLOAT,
            DataTypeDefinition.DOUBLE, ElasticsearchType.DOUBLE,
            DataTypeDefinition.BOOLEAN, ElasticsearchType.BOOLEAN,
            DataTypeDefinition.NODE_REF, ElasticsearchType.KEYWORD,
            DataTypeDefinition.CATEGORY, ElasticsearchType.KEYWORD);

    @Override
    public boolean canMap(FieldMappingContext context)
    {
        return supportedTypes.containsKey(context.propertyDefinition().getDataType().getName());
    }

    @Override
    public ElasticsearchFieldMapping buildMapping(FieldMappingContext context)
    {
        return mappingWithMetadata(context.name(), guessType(context))
                .withAlias();
    }

    private ElasticsearchType guessType(FieldMappingContext context)
    {
        return supportedTypes.get(context.propertyDefinition().getDataType().getName());
    }
}
