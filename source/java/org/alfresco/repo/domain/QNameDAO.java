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
     * @return              the namespace entity (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    NamespaceEntity getNamespaceEntity(Long id);
    
    /**
     * @param namespaceUri  the namespace URI to query for
     * @return              the namespace entity of null if it doesn't exist
     */
    NamespaceEntity getNamespaceEntity(String namespaceUri);
    
    /**
     * Get an existing instance matching the URI or create one if necessary.
     * Note that this method should be treated as a write method and should not
     * be used in the context of read-only or query methods.
     * 
     * @param namespaceUri  the namespace URI to create
     * @return              the existing namespace entity if found or a new one
     */
    NamespaceEntity getOrCreateNamespaceEntity(String namespaceUri);
    
    /**
     * @param namespaceUri  the namespace URI to create
     * @return              Returns the new instance
     */
    NamespaceEntity newNamespaceEntity(String namespaceUri);
    
    /**
     * @param id            the unique ID of the entity
     * @return              the QName entity (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    QNameEntity getQNameEntity(Long id);

    /**
     * @param id            the unique ID of the entity
     * @return              the QName (never null)
     * @throws              AlfrescoRuntimeException if the ID provided is invalid
     */
    QName getQName(Long id);

    /**
     * @param qname         the QName to query for
     * @return              the QName entity of null if it doesn't exist
     */
    QNameEntity getQNameEntity(QName qname);
    
    /**
     * Get an existing instance matching the QName or create one if necessary.
     * Note that this method should be treated as a write method and should not
     * be used in the context of read-only or query methods.
     * 
     * @param qname         the QName to query for
     * @return              an existing QName entity if found or a new one
     */
    QNameEntity getOrCreateQNameEntity(QName qname);
    
    /**
     * @param qname         the QName to create
     * @return              the new instance
     */
    QNameEntity newQNameEntity(QName qname);
    
    Set<QName> convertIdsToQNames(Set<Long> ids);
    
    Map<QName, ? extends Object> convertIdMapToQNameMap(Map<Long, ? extends Object> idMap);
}
