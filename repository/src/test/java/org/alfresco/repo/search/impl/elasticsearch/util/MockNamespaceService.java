/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.Collection;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.OneToManyHashBiMap;

/**
 * @author Jamal Kaabi-Mofrad
 */
@SuppressWarnings("PMD")
public class MockNamespaceService implements NamespaceService
{
    private final OneToManyHashBiMap<String, String> map = new OneToManyHashBiMap<>();

    public void registerNamespace(String prefix, String uri)
    {
        this.map.putSingleValue(uri, prefix);
    }

    public void unregisterNamespace(String prefix)
    {
        this.map.removeValue(prefix);
    }

    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        return this.map.getKey(prefix);
    }

    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        return this.map.get(namespaceURI);
    }

    public Collection<String> getPrefixes()
    {
        return this.map.flatValues();
    }

    public Collection<String> getURIs()
    {
        return this.map.keySet();
    }
}
