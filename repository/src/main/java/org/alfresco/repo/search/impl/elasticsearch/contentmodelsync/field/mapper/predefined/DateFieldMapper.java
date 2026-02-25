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

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType.DATE;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Text.text;

import java.util.Set;

import org.springframework.stereotype.Component;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

@Component
public class DateFieldMapper implements PredefinedFieldMapper
{
    private static final Property DEFAULT_DATE_FORMAT = text("format", "date_optional_time");
    private static final Set<QName> supportedTypes = Set.of(DataTypeDefinition.DATE, DataTypeDefinition.DATETIME);

    @Override
    public boolean canMap(FieldMappingContext context)
    {
        return supportedTypes.contains(context.propertyDefinition().getDataType().getName());
    }

    @Override
    public ElasticsearchFieldMapping buildMapping(FieldMappingContext context)
    {
        ElasticsearchFieldMapping mapping = ElasticsearchFieldMapping.mappingWithMetadata(context.name(), DATE)
                .withAlias();

        mapping.baseMapping()
                .addChild(DEFAULT_DATE_FORMAT);

        return mapping;
    }

}
