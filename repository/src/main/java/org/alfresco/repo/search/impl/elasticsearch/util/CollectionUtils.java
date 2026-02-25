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
package org.alfresco.repo.search.impl.elasticsearch.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Null-safe collection utilities for Elasticsearch search implementations.
 */
public abstract class CollectionUtils
{
    public static <T> Collection<T> safe(Collection<T> values)
    {
        return ofNullable(values).orElse(emptyList());
    }

    public static <T> List<T> safe(List<T> values)
    {
        return ofNullable(values).orElse(emptyList());
    }

    public static <T> Set<T> safe(Set<T> values)
    {
        return ofNullable(values).orElse(emptySet());
    }
}

