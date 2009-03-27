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
package org.alfresco.cmis.dictionary;

import java.util.Collection;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.service.namespace.QName;

/**
 * Service to query the CMIS meta model
 * 
 * @author davidc
 */
public interface CMISDictionaryService
{
    /**
     * Get Type Id
     * 
     * @param typeId
     * @return
     */
    public CMISTypeId getTypeId(String typeId);
    
    /**
     * Get Type Id from Table Name
     * 
     * @param table
     * @return
     */
    public CMISTypeId getTypeIdFromTable(String table);
    
    /**
     * Get Type Id from Alfresco Class Name
     * 
     * @param clazz
     * @param matchingScope  if provided, only return type id matching scope
     * @return
     */
    public CMISTypeId getTypeId(QName clazz, CMISScope matchingScope);

    /**
     * Get Type Definition
     * 
     * @param typeId
     * @return
     */
    public CMISTypeDefinition getType(CMISTypeId typeId);
    
    /**
     * Get All Type Definitions
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllTypes();
    
    /**
     * Get Property Id for Alfresco property name
     * 
     * @param property
     * @return
     */
    public CMISPropertyId getPropertyId(QName property);
    
    /**
     * Get Property Id
     * 
     * @param propertyId
     * @return
     */
    public CMISPropertyId getPropertyId(String propertyId);
    
    /**
     * Get Property
     * 
     * @param propertyId
     * @return
     */
    public CMISPropertyDefinition getProperty(CMISPropertyId propertyId);
    
    /**
     * Get Data Type
     * 
     * @param dataType
     * @return
     */
    public CMISDataTypeEnum getDataType(QName dataType);

    
    // public CMISTypeDef findType(CMISTypeId typeId)
    // public CMISTypeDef findType(String typeId)
    // public CMISTypeDef findTypeForClass(QName clazz, CMISScope matchingScope, ...)
    // public CMISTypeDef findTypeForTable(String tableName)
    // public CMISTypeDef getAllTypes();
    // public CMISPropertyDefinition getProperty(QName property)
    // public CMISPropertyDefinition getProperty(CMISTypeDef typeDef, String property)
    // public CMISDataTypeEnum getDataType(QName dataType)
    
}
