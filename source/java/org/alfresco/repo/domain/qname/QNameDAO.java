/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.qname;

import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.Pair;

/**
 * Data abstraction layer for QName and Namespace entities.
 * 
 * @author Derek Hulley
 * @since 3.3
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
     * Modify an existing QName.  The ID of the new QName will be the same as the old one
     * i.e. the old QName will cease to exist and will become the new QName.  This allows
     * QName modification without affecting tables that reference the old QName.
     * 
     * @param qnameOld      the old QName, which must exist
     * @param qnameNew      the new QName, which must not exist
     * @return              the QName pair (id, qname) with the ID unchanged from old to new
     */
    Pair<Long, QName> updateQName(QName qnameOld, QName qnameNew);
    
    /**
     * Bulk-convert QName IDs into QNames
     * 
     * @param ids           the IDs
     * @return              the QNames for the IDs given, in the same order
     */
    Set<QName> convertIdsToQNames(Set<Long> ids);
    
    /**
     * Convenience method to convert map keys from QName IDs to QNames
     * 
     * @param idMap         a map of objects keyed by QName ID
     * @return              a map of the same objects keyed by the equivalent QNames
     */
    Map<QName, ? extends Object> convertIdMapToQNameMap(Map<Long, ? extends Object> idMap);
    
    /**
     * Bulk-convert QNames into QName IDs.  This is primarily used for generating
     * SQL <tt>IN</tt> clause lists for other DAO queries.
     * 
     * @param qnames        the QNames to convert
     * @param create        <tt>true</tt> to create any missing QName entities
     * @return              returns the QName IDs (order not guaranteed)
     */
    Set<Long> convertQNamesToIds(Set<QName> qnames, boolean create);
}
