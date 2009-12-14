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
package org.alfresco.repo.domain;

import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Pair;

/**
 * Data abstraction layer for QName and Namespace entities.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public interface QNameDAO
{
    /**
     * @param id            the unique ID of the entity
     * @return              the namespace pair (id, uri)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    Pair<Long, String> getNamespace(Long id);
    
    /**
     * @param namespaceUri  the namespace URI to query for
     * @return              the namespace pair (id, uri) or <tt>null</tt> if it doesn't exist
     */
    Pair<Long, String> getNamespace(String namespaceUri);
    
    /**
     * Get an existing instance matching the URI or create one if necessary.
     * Note that this method should be treated as a write method and should not
     * be used in the context of read-only or query methods.
     * 
     * @param namespaceUri  the namespace URI to create
     * @return              the existing namespace pair (id, uri) or a new one
     */
    Pair<Long, String> getOrCreateNamespace(String namespaceUri);
    
    /**
     * @param namespaceUri  the namespace URI to create
     * @return              the new namespace pair (id, uri)
     */
    Pair<Long, String> newNamespace(String namespaceUri);
    
    /**
     * Modifies an existing namespace URI.  If the new URI already exists, then no
     * new entity is created. 
     * 
     * @param oldNamespaceUri           the old namespace URI
     * @param newNamespaceUri           the new namespace URI
     * @throws AlfrescoRuntimeException if the new namespace is in use
     */
    void updateNamespace(String oldNamespaceUri, String newNamespaceUri);
    
    /**
     * @param id            the unique ID of the entity
     * @return              the QName pair (id, qname) (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    Pair<Long, QName> getQName(Long id);

    /**
     * @param qname         the QName to query for
     * @return              the QName pair (id, qname) or <tt>null</tt> if it doesn't exist
     */
    Pair<Long, QName> getQName(QName qname);
    
    /**
     * Get an existing instance matching the QName or create one if necessary.
     * Note that this method should be treated as a write method and should not
     * be used in the context of read-only or query methods.
     * 
     * @param qname         the QName to query for
     * @return              the QName pair (id, qname) or a new one
     */
    Pair<Long, QName> getOrCreateQName(QName qname);
    
    /**
     * @param qname         the QName to create
     * @return              the new QName pair (id, qname)
     */
    Pair<Long, QName> newQName(QName qname);
    
    Set<QName> convertIdsToQNames(Set<Long> ids);
    
    Map<QName, ? extends Object> convertIdMapToQNameMap(Map<Long, ? extends Object> idMap);
    
    Set<Long> convertQNamesToIds(Set<QName> qnames, boolean create);
}
