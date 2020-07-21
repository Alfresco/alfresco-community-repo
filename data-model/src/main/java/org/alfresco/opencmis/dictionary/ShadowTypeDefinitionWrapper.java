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
import java.util.LinkedList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

public abstract class ShadowTypeDefinitionWrapper extends AbstractTypeDefinitionWrapper
{

    private static final long serialVersionUID = 1L;

    @Override
    public List<TypeDefinitionWrapper> connectParentAndSubTypes(CMISMapping cmisMapping, CMISDictionaryRegistry registry,
            DictionaryService dictionaryService)
    {
    	String parentTypeId = typeDef.getParentTypeId();

        // find parent
        if (parentTypeId != null)
        {
            parent = registry.getTypeDefByTypeId(parentTypeId);
            if(registry.getTenant() != null && parent != null && registry.getTypeDefByTypeId(parentTypeId, false) == null)
            {
            	// this is a tenant registry and the parent is not defined locally so add this type as a child of it
            	registry.addChild(parent.getTypeId(), this);
            }
        }
        else
        {
            if (!isBaseType())
            {
                throw new AlfrescoRuntimeException("Type " + typeDef.getId() + " has no parent!");
            }

            parent = null;
        }

//        if(parent != null)
//        {
//            List<TypeDefinitionWrapper> children = new LinkedList<TypeDefinitionWrapper>();
//            children.add(this);
//            registry.setChildren(parent.getTypeId(), children);
//        }

        // find children
//        children = new ArrayList<TypeDefinitionWrapper>();
        List<TypeDefinitionWrapper> children = new LinkedList<TypeDefinitionWrapper>();
        Collection<QName> childrenNames = dictionaryService.getSubTypes(cmisMapping.getAlfrescoClass(getAlfrescoName()),
                false);
        for (QName childName : childrenNames)
        {
            if (cmisMapping.isValidCmisObject(getBaseTypeId(), childName))
            {
                TypeDefinitionWrapper child = registry.getTypeDefByQName(childName);

                if (child == null)
                {
                    throw new AlfrescoRuntimeException("Failed to retrieve sub type for type id " + childName
                            + " for parent type " + getAlfrescoName() + "!");
                }
                children.add(child);
            }
        }
        return children;
//        registry.setChildren(typeDef.getId(), children);
    }

    public void resolveInheritance(CMISMapping cmisMapping,
            CMISDictionaryRegistry registry, DictionaryService dictionaryService)
    {
        PropertyDefinition<?> propertyDefintion;

        if (parent != null)
        {
            for (PropertyDefinitionWrapper propDef : parent.getProperties(false))
            {
                if (propertiesById.containsKey(propDef.getPropertyId()))
                {
                    continue;
                }

                org.alfresco.service.cmr.dictionary.PropertyDefinition alfrescoPropDef = dictionaryService.getProperty(
                        propDef.getOwningType().getAlfrescoName(), propDef.getAlfrescoName());

                propertyDefintion = createPropertyDefinition(cmisMapping, propDef.getPropertyId(),
                        alfrescoPropDef.getName(), dictionaryService, alfrescoPropDef, true);

                if (propertyDefintion != null)
                {
                    registerProperty(new BasePropertyDefintionWrapper(propertyDefintion, alfrescoPropDef.getName(),
                            propDef.getOwningType(), propDef.getPropertyAccessor(), propDef.getPropertyLuceneBuilder()));
                }
            }
        }

        List<TypeDefinitionWrapper> children = registry.getChildren(typeDef.getId());
        for (TypeDefinitionWrapper child : children)
        {
            if (child instanceof AbstractTypeDefinitionWrapper)
            {
                ((AbstractTypeDefinitionWrapper) child).resolveInheritance(cmisMapping, registry,
                        dictionaryService);
            }
        }
    }
}
