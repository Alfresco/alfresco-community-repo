/* Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.service.namespace;

import java.util.Collection;

import org.alfresco.util.OneToManyHashBiMap;

/**
 * A basic implementation of the NamespaceService interface intended for use in
 * unit tests. This implementation does not persist any changes beyond the
 * lifetime of the object.
 * 
 * @author Nick Smith
 */
public class NamespaceServiceMemoryImpl implements NamespaceService
{
    // URI to Prefix map.
    private final OneToManyHashBiMap<String, String> map = new OneToManyHashBiMap<String, String>();

    public void registerNamespace(String prefix, String uri)
    {
        map.putSingleValue(uri, prefix);
    }

    public void unregisterNamespace(String prefix)
    {
        map.removeValue(prefix);
    }

    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        return map.getKey(prefix);
    }

    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        return map.get(namespaceURI);
    }

    public Collection<String> getPrefixes()
    {
        return map.flatValues();
    }

    public Collection<String> getURIs()
    {
        return map.keySet();
    }

}