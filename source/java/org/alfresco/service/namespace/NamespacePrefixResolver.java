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
package org.alfresco.service.namespace;

import java.util.Collection;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * The <code>NamespacePrefixResolver</code> provides a mapping between
 * namespace prefixes and namespace URIs.
 * 
 * @author David Caruana
 */
@PublicService
public interface NamespacePrefixResolver
{
    /**
     * Gets the namespace URI registered for the given prefix
     * 
     * @param prefix  prefix to lookup
     * @return  the namespace
     * @throws NamespaceException  if prefix has not been registered  
     */
    @Auditable(parameters = {"prefix"})
    public String getNamespaceURI(String prefix)
        throws NamespaceException;
    
    /**
     * Gets the registered prefixes for the given namespace URI
     * 
     * @param namespaceURI  namespace URI to lookup
     * @return  the prefixes (or empty collection, if no prefixes registered against URI)
     * @throws NamespaceException  if URI has not been registered 
     */
    @Auditable(parameters = {"namespaceURI"})
    public Collection<String> getPrefixes(String namespaceURI)
        throws NamespaceException;
    
    /**
     * Gets all registered Prefixes
     * 
     * @return collection of all registered namespace prefixes
     */
    @Auditable
    Collection<String> getPrefixes();

    /**
     * Gets all registered Uris
     * 
     * @return collection of all registered namespace uris
     */
    @Auditable
    Collection<String> getURIs();

}
