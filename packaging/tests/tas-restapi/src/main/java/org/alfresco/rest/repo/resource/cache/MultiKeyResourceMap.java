/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.repo.resource.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.utility.model.TestModel;
import org.apache.commons.lang3.StringUtils;

/**
 * Allows to store data in map and find it using three keys - id, name and alias.
 * @param <RESOURCE> repository resource, e.g. folder, category, etc.
 */
public class MultiKeyResourceMap<RESOURCE extends TestModel> extends HashMap<String, RESOURCE>
{
    private static final String ID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    private final Map<String, String> keys = new HashMap<>();
    private final Function<RESOURCE, String> idSupplier;
    private final Function<RESOURCE, String> nameSupplier;

    public MultiKeyResourceMap(Function<RESOURCE, String> idSupplier, Function<RESOURCE, String> nameSupplier)
    {
        super();
        this.idSupplier = idSupplier;
        this.nameSupplier = nameSupplier;
    }

    @Override
    public RESOURCE put(String alias, RESOURCE resource)
    {
        String id = idSupplier.apply(resource);
        String name = nameSupplier.apply(resource);
        if (StringUtils.isEmpty(id))
        {
            throw new IllegalArgumentException("ID of entity with name: " + name + " and alias: " + alias + " cannot be empty!");
        }
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Name of entity with ID: " + id + " and alias: " + alias + " cannot be empty!");
        }

        if (StringUtils.isNotEmpty(alias))
        {
            if (keys.containsKey(alias))
            {
                throw new IllegalStateException("Entity with alias: " + alias + " already exists in cache!");
            }
            keys.put(alias, id);
        }

        if (StringUtils.isNotEmpty(name))
        {
            keys.put(name, id);
        }
        return super.put(id, resource);
    }

    @Override
    public RESOURCE get(Object key)
    {
        return super.get(findKey(key));
    }

    @Override
    public RESOURCE remove(Object key)
    {
        RESOURCE resource = this.get(key);
        if (resource == null)
        {
            return  null;
        }

        String id = idSupplier.apply(resource);
        findKeysFor(id).forEach(keys::remove);

        return super.remove(id);
    }

    private Object findKey(Object key)
    {
        Object realKey = key;
        if (key instanceof String k && (!Pattern.compile(ID_PATTERN).matcher(k).matches() || !super.containsKey(k)))
        {
            realKey = keys.getOrDefault(k, k);
        }
        return realKey;
    }

    public Set<String> findKeysFor(Object id) {
        return keys.entrySet()
            .stream()
            .filter(entry -> id.equals(entry.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toSet());
    }
}
