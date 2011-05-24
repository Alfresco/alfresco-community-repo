/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General  License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General  License for more details.
 *
 * You should have received a copy of the GNU Lesser General  License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.opencmis.dictionary;

import java.util.List;

import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

/**
 * Service to query the CMIS meta model
 * 
 * @author davidc
 */
public interface CMISDictionaryService
{
    /**
     * Find type for type id
     * 
     * @param typeId
     * @return
     */
    TypeDefinitionWrapper findType(String typeId);

    /**
     * Find type for Alfresco class name. Optionally, constrain match to one of
     * specified CMIS scopes
     * 
     * @param clazz
     * @param matchingScopes
     * @return
     */
    TypeDefinitionWrapper findTypeForClass(QName clazz, BaseTypeId... matchingScopes);

    TypeDefinitionWrapper findNodeType(QName clazz);

    TypeDefinitionWrapper findAssocType(QName clazz);

    PropertyDefintionWrapper findProperty(String propId);

    PropertyDefintionWrapper findPropertyByQueryName(String queryName);

    /**
     * Find a type by its query name
     * 
     * @param queryName
     * @return
     */
    TypeDefinitionWrapper findTypeByQueryName(String queryName);

    /**
     * Get Base Types
     */
    List<TypeDefinitionWrapper> getBaseTypes();

    /**
     * Get all Types
     * 
     * @return
     */
    List<TypeDefinitionWrapper> getAllTypes();

    /**
     * Find data type
     * 
     * @param dataType
     * @return
     */
    PropertyType findDataType(QName dataType);

    QName findAlfrescoDataType(PropertyType propertyType);
}
