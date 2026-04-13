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
     * @param typeId String
     * @return TypeDefinitionWrapper
     */
    TypeDefinitionWrapper findType(String typeId);

    List<TypeDefinitionWrapper> getChildren(String typeId);

    /**
     * Find type for Alfresco class name. Optionally, constrain match to one of
     * specified CMIS scopes
     * 
     * @param clazz QName
     * @param matchingScopes BaseTypeId...
     * @return TypeDefinitionWrapper
     */
    TypeDefinitionWrapper findTypeForClass(QName clazz, BaseTypeId... matchingScopes);

    TypeDefinitionWrapper findNodeType(QName clazz);

    TypeDefinitionWrapper findAssocType(QName clazz);

    PropertyDefinitionWrapper findProperty(String propId);

    PropertyDefinitionWrapper findPropertyByQueryName(String queryName);

    /**
     * Find a type by its query name
     * 
     * @param queryName String
     * @return TypeDefinitionWrapper
     */
    TypeDefinitionWrapper findTypeByQueryName(String queryName);

    /**
     * Get Base Types
     */
    List<TypeDefinitionWrapper> getBaseTypes();

    List<TypeDefinitionWrapper> getBaseTypes(boolean includeParent);

    /**
     * Get all Types
     * 
     */
    List<TypeDefinitionWrapper> getAllTypes();

    List<TypeDefinitionWrapper> getAllTypes(boolean includeParent);

    /**
     * Find data type
     * 
     * @param dataType QName
     * @return PropertyType
     */
    PropertyType findDataType(QName dataType);

    QName findAlfrescoDataType(PropertyType propertyType);
    
    boolean isExcluded(QName qname);
}
