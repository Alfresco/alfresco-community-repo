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

import java.util.ArrayList;
import java.util.Collection;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.dictionary.CMISAbstractDictionaryService.DictionaryRegistry;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

public abstract class ShadowTypeDefinitionWrapper extends AbstractTypeDefinitionWrapper
{

    private static final long serialVersionUID = 1L;

    public void connectParentAndSubTypes(CMISMapping cmisMapping, DictionaryRegistry registry,
            DictionaryService dictionaryService)
    {
        // find parent
        if (typeDef.getParentTypeId() != null)
        {
            parent = registry.typeDefsByTypeId.get(typeDef.getParentTypeId());
        } else
        {
            if (!isBaseType())
            {
                throw new AlfrescoRuntimeException("Type " + typeDef.getId() + " has no parent!");
            }

            parent = null;
        }

        // find children
        children = new ArrayList<TypeDefinitionWrapper>();
        Collection<QName> childrenNames = dictionaryService.getSubTypes(cmisMapping.getAlfrescoClass(getAlfrescoName()),
                false);
        for (QName childName : childrenNames)
        {
            if (cmisMapping.isValidCmisObject(getBaseTypeId(), childName))
            {
                TypeDefinitionWrapper child = registry.typeDefsByQName.get(childName);

                if (child == null)
                {
                    throw new AlfrescoRuntimeException("Failed to retrieve sub type for type id " + childName
                            + " for parent type " + getAlfrescoName() + "!");
                }
                children.add(child);
            }
        }
    }

    public void resolveInheritance(CMISMapping cmisMapping, ServiceRegistry serviceRegistry,
            DictionaryRegistry registry, DictionaryService dictionaryService)
    {
        PropertyDefinition<?> propertyDefintion;

        if (parent != null)
        {
            for (PropertyDefintionWrapper propDef : parent.getProperties())
            {
                if (propertiesById.containsKey(propDef.getPropertyId()))
                {
                    continue;
                }

                org.alfresco.service.cmr.dictionary.PropertyDefinition alfrescoPropDef = dictionaryService.getProperty(
                        propDef.getOwningType().getAlfrescoName(), propDef.getAlfrescoName());

                propertyDefintion = createPropertyDefinition(cmisMapping, propDef.getPropertyId(),
                        alfrescoPropDef.getName(), alfrescoPropDef, true);

                if (propertyDefintion != null)
                {
                    registerProperty(new BasePropertyDefintionWrapper(propertyDefintion, alfrescoPropDef.getName(),
                            propDef.getOwningType(), propDef.getPropertyAccessor(), propDef.getPropertyLuceneBuilder()));
                }
            }
        }

        for (TypeDefinitionWrapper child : children)
        {
            if (child instanceof AbstractTypeDefinitionWrapper)
            {
                ((AbstractTypeDefinitionWrapper) child).resolveInheritance(cmisMapping, serviceRegistry, registry,
                        dictionaryService);
            }
        }
    }
}
