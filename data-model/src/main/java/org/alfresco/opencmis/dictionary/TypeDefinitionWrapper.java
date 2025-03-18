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

import java.util.Collection;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

public interface TypeDefinitionWrapper
{
    TypeDefinition getTypeDefinition(boolean includePropertyDefinitions);

    String getTypeId();

    BaseTypeId getBaseTypeId();

    boolean isBaseType();

    QName getAlfrescoName();

    QName getAlfrescoClass();

    String getTenantId();

    TypeDefinitionWrapper getParent();

    // List<TypeDefinitionWrapper> getChildren();

    Collection<PropertyDefinitionWrapper> getProperties();

    Collection<PropertyDefinitionWrapper> getProperties(boolean update);

    PropertyDefinitionWrapper getPropertyById(String propertyId);

    PropertyDefinitionWrapper getPropertyByQueryName(String queryName);

    PropertyDefinitionWrapper getPropertyByQName(QName name);

    Map<Action, CMISActionEvaluator> getActionEvaluators();

    void updateDefinition(DictionaryService dictionaryService);
}
