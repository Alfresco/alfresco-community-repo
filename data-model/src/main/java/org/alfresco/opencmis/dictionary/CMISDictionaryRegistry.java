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
import java.util.List;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author sglover
 *
 */
public interface CMISDictionaryRegistry
{
    TypeDefinitionWrapper getTypeDefByTypeId(String typeId);
    TypeDefinitionWrapper getTypeDefByTypeId(String typeId, boolean includeParent);
    TypeDefinitionWrapper getAssocDefByQName(QName qname);
    TypeDefinitionWrapper getTypeDefByQueryName(Object queryName);
    TypeDefinitionWrapper getTypeDefByQName(QName qname);
    PropertyDefinitionWrapper getPropDefByPropId(String propId);
    PropertyDefinitionWrapper getPropDefByQueryName(Object queryName);
    List<TypeDefinitionWrapper> getBaseTypes();
    List<TypeDefinitionWrapper> getBaseTypes(boolean includeParent);
    Collection<AbstractTypeDefinitionWrapper> getTypeDefs();
    Collection<AbstractTypeDefinitionWrapper> getTypeDefs(boolean includeParent);
    Collection<AbstractTypeDefinitionWrapper> getAssocDefs();
    Collection<AbstractTypeDefinitionWrapper> getAssocDefs(boolean includeParent);
    void registerTypeDefinition(AbstractTypeDefinitionWrapper typeDef);
    String getTenant();
    List<TypeDefinitionWrapper> getChildren(String typeId);
    void setChildren(String typeId, List<TypeDefinitionWrapper> children);
    void addChild(String typeId, TypeDefinitionWrapper child);

    void addModel(CompiledModel model);
    void updateModel(CompiledModel model);
    void removeModel(CompiledModel model);
}
