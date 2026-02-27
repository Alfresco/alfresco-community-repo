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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property;

import static java.util.Map.entry;

import java.util.Map.Entry;

import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.util.Property;

public enum ElasticsearchType implements Property
{
    INT("integer"), LONG("long"), FLOAT("float"), DOUBLE("double"), BOOLEAN("boolean"), DATE("date"), TEXT("text"), ALIAS("alias"), KEYWORD("keyword");

    private final String value;

    ElasticsearchType(String value)
    {
        this.value = value;
    }

    @Override
    public Entry<String, ?> toEntry()
    {
        return entry("type", value);
    }
}
