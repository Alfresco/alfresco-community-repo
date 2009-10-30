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
package org.alfresco.cmis;

import java.util.Collection;

import org.alfresco.service.namespace.QName;

/**
 * Service to query the CMIS meta model
 * 
 * @author davidc
 */
public interface CMISDictionaryService
{
    /**
     * Find type for type id
     * @param typeId
     * @return
     */
    public CMISTypeDefinition findType(CMISTypeId typeId);
    
    /**
     * Find type for type id
     * 
     * @param typeId
     * @return
     */
    public CMISTypeDefinition findType(String typeId);
    
    /**
     * Find type for Alfresco class name. Optionally, constrain match to one of specified CMIS scopes
     * 
     * @param clazz
     * @param matchingScopes
     * @return
     */
    public CMISTypeDefinition findTypeForClass(QName clazz, CMISScope... matchingScopes);
    
    /**
     * Find a type by its query name
     * 
     * @param queryName
     * @return
     */
    public CMISTypeDefinition findTypeByQueryName(String queryName);
    
    /**
     * Find a property by its query name
     * 
     * @param queryName
     * @return
     */
    public CMISPropertyDefinition findPropertyByQueryName(String queryName);
    
    /**
     * Get Base Types
     */
    public Collection<CMISTypeDefinition> getBaseTypes();
    
    /**
     * Get all Types
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllTypes();

    /**
     * Find property.  Optionally constrain match to specified type.
     * 
     * @param property
     * @param matchingType
     * @return
     */
    public CMISPropertyDefinition findProperty(QName property, CMISTypeDefinition matchingType);
    
    /**
     * Find property.  Optionally constrain match to specified type.
     * 
     * @param property
     * @param matchingType
     * @return
     */
    public CMISPropertyDefinition findProperty(String property, CMISTypeDefinition matchingType);

    /**
     * Find data type
     * 
     * @param dataType
     * @return
     */
    public CMISDataTypeEnum findDataType(QName dataType);
    
}
