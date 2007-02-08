/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.alfresco.service.namespace.NamespaceException;

/**
 * Simple in-memory namespace DAO
 * 
 * TODO: Remove the many to one mapping of prefixes to URIs
 */
public class NamespaceDAOImpl implements NamespaceDAO
{

    private List<String> uris = new ArrayList<String>();
    private HashMap<String, String> prefixes = new HashMap<String, String>();

    
    public Collection<String> getURIs()
    {
        return Collections.unmodifiableCollection(uris);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getPrefixes()
     */
    public Collection<String> getPrefixes()
    {
        return Collections.unmodifiableCollection(prefixes.keySet());
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#addURI(java.lang.String)
     */
    public void addURI(String uri)
    {
        if (uris.contains(uri))
        {
            throw new NamespaceException("URI " + uri + " has already been defined");
        }
        uris.add(uri);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#addPrefix(java.lang.String, java.lang.String)
     */
    public void addPrefix(String prefix, String uri)
    {
        if (!uris.contains(uri))
        {
            throw new NamespaceException("Namespace URI " + uri + " does not exist");
        }
        prefixes.put(prefix, uri);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#removeURI(java.lang.String)
     */
    public void removeURI(String uri)
    {
        uris.remove(uri);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.NamespaceDAO#removePrefix(java.lang.String)
     */
    public void removePrefix(String prefix)
    {
        prefixes.remove(prefix);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {
        return prefixes.get(prefix);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getPrefixes(java.lang.String)
     */
    public Collection<String> getPrefixes(String URI)
    {
        Collection<String> uriPrefixes = new ArrayList<String>();
        for (String key : prefixes.keySet())
        {
            String uri = prefixes.get(key);
            if ((uri != null) && (uri.equals(URI)))
            {
                uriPrefixes.add(key);
            }
        }
        return uriPrefixes;
    }

}
