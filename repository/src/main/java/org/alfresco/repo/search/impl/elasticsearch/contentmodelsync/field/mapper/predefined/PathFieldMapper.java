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

import static java.util.Optional.ofNullable;

import static org.alfresco.repo.dictionary.IndexTokenisationMode.BOTH;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping.mappingWithMetadata;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchPropertyKey.COPY_TO;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType.KEYWORD;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType.TEXT;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Text.text;

import org.springframework.stereotype.Component;

import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.ElasticsearchFieldMapping;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

@Component
public class PathFieldMapper implements PredefinedFieldMapper
{
    public static final Property ANALYZER = text("analyzer", "path_emulator");

    @Override
    public boolean canMap(FieldMappingContext context)
    {
        return context.propertyDefinition()
                .getDataType()
                .getName()
                .equals(DataTypeDefinition.PATH);
    }

    @Override
    public ElasticsearchFieldMapping buildMapping(FieldMappingContext context)
    {
        return switch (context.propertyDefinition().getIndexTokenisationMode())
        {
        case FALSE -> buildKeywordMapping(context);
        case TRUE, BOTH -> buildPathMapping(context);
        };
    }

    private ElasticsearchFieldMapping buildKeywordMapping(FieldMappingContext context)
    {
        return mappingWithMetadata(context.name(), KEYWORD).withAlias();
    }

    private ElasticsearchFieldMapping buildPathMapping(FieldMappingContext context)
    {
        ElasticsearchFieldMapping mapping = mappingWithMetadata(context.name(), TEXT);

        mapping.baseMapping()
                .addChild(ANALYZER);

        if (shouldCreateNotTokenizedIndex(context))
        {
            mapping.baseMapping().addTextLeaf(COPY_TO, context.name()
                    .untokenized());

            mapping.notTokenizedMapping()
                    .addChild(KEYWORD);
        }

        return mapping;
    }

    private boolean shouldCreateNotTokenizedIndex(FieldMappingContext context)
    {
        return context.propertyDefinition().getIndexTokenisationMode() == BOTH || ofNullable(context.propertyDefinition()
                .getFacetable()).map(option -> option == Facetable.TRUE)
                        .orElse(false);
    }
}
