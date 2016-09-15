/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A delegating namespace prefix resolver which allows local over rides from the
 * delegate. Allows standard/default prefixes to be available but over ridden as
 * required.
 * 
 * @author andyh
 */
public class DynamicNamespacePrefixResolver implements NamespaceService
{
    /**
     * The delegate
     */
    private NamespacePrefixResolver delegate;

    /**
     * The map uris keyed by prefix
     */
    private HashMap<String, String> map = new HashMap<String, String>();

    
    public DynamicNamespacePrefixResolver(NamespacePrefixResolver delegate)
    {
        super();
        this.delegate = delegate;
    }
    
    public DynamicNamespacePrefixResolver()
    {
        this(null);
    }

    
    /**
     * Add prefix to name space mapping override
     * 
     * @param prefix String
     * @param uri String
     */
    public void registerNamespace(String prefix, String uri)
    {
        map.put(prefix, uri);
    }

    /**
     * Remove a prefix to namespace mapping
     * 
     * @param prefix String
     */
    public void unregisterNamespace(String prefix)
    {
        map.remove(prefix);
    }

    // NameSpacePrefix Resolver

    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        String uri = map.get(prefix);
        if ((uri == null) && (delegate != null))
        {
            uri = delegate.getNamespaceURI(prefix);
        }
        return uri;
    }

    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        Collection<String> prefixes = new ArrayList<String>();
        for (String key : map.keySet())
        {
            String uri = map.get(key);
            if ((uri != null) && (uri.equals(namespaceURI)))
            {
                prefixes.add(key);
            }
        }
        // Only add if not over ridden here (if identical already added)
        if (delegate != null)
        {
            for (String prefix : delegate.getPrefixes(namespaceURI))
            {
                if (!map.containsKey(prefix))
                {
                    prefixes.add(prefix);
                }
            }
        }
        return prefixes;
    }

    public Collection<String> getPrefixes()
    {
       Set<String> prefixes = new HashSet<String>();
       if(delegate != null)
       {
          prefixes.addAll(delegate.getPrefixes());
       }
       prefixes.addAll(map.keySet());
       return prefixes;
    }
    
    public Collection<String> getURIs()
    {
       Set<String> uris = new HashSet<String>();
       if(delegate != null)
       {
          uris.addAll(delegate.getURIs());
       }
       uris.addAll(map.keySet());
       return uris;
    }
}
