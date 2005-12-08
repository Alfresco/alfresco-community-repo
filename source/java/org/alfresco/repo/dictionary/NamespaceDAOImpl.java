/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
