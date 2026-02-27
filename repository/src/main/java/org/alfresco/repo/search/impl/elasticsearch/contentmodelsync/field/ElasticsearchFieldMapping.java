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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchPropertyKey.DECODED_QUALIFIED_NAME;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchPropertyKey.METADATA;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchPropertyKey.PATH;
import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.NestedProperty.nestedProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchType;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.NestedProperty;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;

public class ElasticsearchFieldMapping
{
    public final FieldName name;

    public final Map<String, NestedProperty> mappings = new HashMap<>();

    public ElasticsearchFieldMapping(FieldName name)
    {
        this.name = name;
    }

    public static ElasticsearchFieldMapping mappingWithMetadata(FieldName name, ElasticsearchType type)
    {
        ElasticsearchFieldMapping mapping = new ElasticsearchFieldMapping(name);

        mapping.baseMapping()
                .addChild(type)
                .addChild(generateMetadata(name));

        return mapping;
    }

    private static Property generateMetadata(FieldName name)
    {
        return nestedProperty(METADATA)
                .addTextLeaf(DECODED_QUALIFIED_NAME, name.truncated());
    }

    public NestedProperty baseMapping()
    {
        return mapping(name.encoded());
    }

    public NestedProperty notTokenizedMapping()
    {
        return mapping(name.untokenized());
    }

    public NestedProperty exactTermSearchMapping()
    {
        return mapping(name.exactTermSearch());
    }

    private NestedProperty mapping(String name)
    {
        if (mappings.containsKey(name))
        {
            return mappings.get(name);
        }

        NestedProperty mapping = nestedProperty(name);
        mappings.put(name, mapping);

        return mapping;
    }

    public ElasticsearchFieldMapping withAlias()
    {
        notTokenizedMapping().addChild(ElasticsearchType.ALIAS)
                .addTextLeaf(PATH, name.encoded());

        return this;
    }

    public Map<String, Object> asMap()
    {
        return mappings.values()
                .stream()
                .map(Property::toEntry)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

}
