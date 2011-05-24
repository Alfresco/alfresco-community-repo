/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.opencmis.dictionary;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.opencmis.CMISActionEvaluator;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

public interface TypeDefinitionWrapper
{
    TypeDefinition getTypeDefinition(boolean includePropertyDefinitions);

    String getTypeId();

    BaseTypeId getBaseTypeId();
    
    boolean isBaseType();

    QName getAlfrescoName();
    
    QName getAlfrescoClass();

    TypeDefinitionWrapper getParent();

    List<TypeDefinitionWrapper> getChildren();

    Collection<PropertyDefintionWrapper> getProperties();

    PropertyDefintionWrapper getPropertyById(String propertyId);

    PropertyDefintionWrapper getPropertyByQueryName(String queryName);

    PropertyDefintionWrapper getPropertyByQName(QName name);
    
    Map<Action, CMISActionEvaluator<? extends Object>> getActionEvaluators();
}
