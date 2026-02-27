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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util;

import static java.util.Map.entry;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Text.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NestedProperty implements Property
{
    private final String key;

    private final List<Property> values = new ArrayList<>();

    public NestedProperty(String key)
    {
        this.key = key;
    }

    public static NestedProperty nestedProperty(String key)
    {
        return new NestedProperty(key);
    }

    public NestedProperty addChild(Property property)
    {
        values.add(property);

        return this;
    }

    public NestedProperty addChildren(List<Property> properties)
    {
        values.addAll(properties);

        return this;
    }

    public NestedProperty addTextLeaf(String key, String value)
    {
        return addChild(text(key, value));
    }

    @Override
    public Entry<String, ?> toEntry()
    {
        return entry(key, collectValues());
    }

    private Map<String, ?> collectValues()
    {
        return values.stream()
                .map(Property::toEntry)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
