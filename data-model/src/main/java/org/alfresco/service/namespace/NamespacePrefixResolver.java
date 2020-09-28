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

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * The <code>NamespacePrefixResolver</code> provides a mapping between
 * namespace prefixes and namespace URIs.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
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
