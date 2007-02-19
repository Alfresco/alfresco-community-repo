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

import java.util.Collection;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.alfresco.service.namespace.NamespaceService;


/**
 * Alfresco implementation of a JCR Namespace registry
 * 
 * @author David Caruana
 */
public class NamespaceRegistryImpl implements NamespaceRegistry
{

    private boolean allowRegistration;
    private NamespaceService namespaceService;
    
    
    /**
     * Construct
     * 
     * @param namespaceService  namespace service
     */
    public NamespaceRegistryImpl(boolean allowRegistraton, NamespaceService namespaceService)
    {
        this.allowRegistration = allowRegistraton;
        this.namespaceService = namespaceService;
    }

    /**
     * Get the namespace prefix resolver
     * 
     * @return  the namespace prefix resolver
     */
    public NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#registerNamespace(java.lang.String, java.lang.String)
     */
    public void registerNamespace(String prefix, String uri) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException
    {
        try
        {
            if (!allowRegistration)
            {
                throw new UnsupportedRepositoryOperationException();
            }
            namespaceService.registerNamespace(prefix, uri);
        }
        catch(org.alfresco.service.namespace.NamespaceException e)
        {
            throw new NamespaceException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#unregisterNamespace(java.lang.String)
     */
    public void unregisterNamespace(String prefix) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException
    {
        try
        {
            if (!allowRegistration)
            {
                throw new UnsupportedRepositoryOperationException();
            }
            namespaceService.unregisterNamespace(prefix);
        }
        catch(org.alfresco.service.namespace.NamespaceException e)
        {
            throw new NamespaceException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#getPrefixes()
     */
    public String[] getPrefixes() throws RepositoryException
    {
        Collection<String> prefixes = namespaceService.getPrefixes();
        return prefixes.toArray(new String[prefixes.size()]);
    }

    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#getURIs()
     */
    public String[] getURIs() throws RepositoryException
    {
        Collection<String> uris = namespaceService.getURIs();
        return uris.toArray(new String[uris.size()]);        
    }

    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#getURI(java.lang.String)
     */
    public String getURI(String prefix) throws NamespaceException, RepositoryException
    {
        String uri = namespaceService.getNamespaceURI(prefix);
        if (uri == null)
        {
            throw new NamespaceException("Prefix " + prefix + " is unknown.");
        }
        return uri;
    }

    /* (non-Javadoc)
     * @see javax.jcr.NamespaceRegistry#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) throws NamespaceException, RepositoryException
    {
        Collection<String> prefixes = namespaceService.getPrefixes(uri);
        if (prefixes.size() == 0)
        {
            throw new NamespaceException("URI " + uri + " is unknown.");
        }
        // Return first prefix registered for uri
        return prefixes.iterator().next();
    }

}
