/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;


/**
 * JCR Namespace Resolver
 * 
 * @author David Caruana
 */
public class JCRNamespacePrefixResolver implements NamespaceService
{
    // delegate
    private NamespacePrefixResolver delegate;

    // prefix -> uri
    private Map<String, String> prefixes = new HashMap<String, String>();
    
    // uri -> prefix
    private Map<String, String> uris = new HashMap<String, String>();
    

    /**
     * Construct
     * 
     * @param delegate  namespace delegate
     */
    public JCRNamespacePrefixResolver(NamespacePrefixResolver delegate)
    {
        this.delegate = delegate;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespacePrefixResolver#getPrefixes(java.lang.String)
     */
    public Collection<String> getPrefixes(String namespaceURI) throws NamespaceException
    {
        String prefix = uris.get(namespaceURI);
        if (prefix == null)
        {
            return delegate.getPrefixes(namespaceURI); 
        }
        List<String> prefixes = new ArrayList<String>();
        prefixes.add(prefix);
        return prefixes;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespacePrefixResolver#getPrefixes()
     */
    public Collection<String> getPrefixes()
    {
        List<String> prefixes = new ArrayList<String>();
        Collection<String> uris = getURIs();
        for (String uri : uris)
        {
            Collection<String> uriPrefixes = getPrefixes(uri);
            prefixes.addAll(uriPrefixes);
        }
        return prefixes;        
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespaceService#registerNamespace(java.lang.String, java.lang.String)
     */
    public void registerNamespace(String prefix, String uri)
    {
        //
        // Check re-mapping according to JCR specification
        //
        
        // Cannot map any prefix that starts with xml
        if (prefix.toLowerCase().startsWith(JCRNamespace.XML_PREFIX))
        {
            throw new NamespaceException("Cannot map prefix " + prefix + " as it is reserved");
        }
        
        // Cannot remap a prefix that is already assigned to a uri
        String existingUri = delegate.getNamespaceURI(prefix);
        if (existingUri != null)
        {
            throw new NamespaceException("Cannot map prefix " + prefix + " as it is already assigned to uri " + existingUri);
        }
        
        // Cannot map a prefix to a non-existent uri
        Collection<String> existingURIs = delegate.getURIs();
        if (existingURIs.contains(uri) == false)
        {
            throw new NamespaceException("Cannot map prefix " + prefix + " to uri " + uri + " which does not exist");
        }        
        
        prefixes.put(prefix, uri);
        uris.put(uri, prefix);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespaceService#unregisterNamespace(java.lang.String)
     */
    public void unregisterNamespace(String prefix)
    {
        String uri = prefixes.get(prefix);
        if (uri != null)
        {
            uris.remove(uri);
        }
        prefixes.remove(prefix);
    }

    public String getNamespaceURI(String prefix) throws NamespaceException
    {
        String uri = prefixes.get(prefix);
        if (uri == null)
        {
            return delegate.getNamespaceURI(prefix);
        }
        return uri;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespacePrefixResolver#getURIs()
     */
    public Collection<String> getURIs()
    {
        return delegate.getURIs();
    }

}
