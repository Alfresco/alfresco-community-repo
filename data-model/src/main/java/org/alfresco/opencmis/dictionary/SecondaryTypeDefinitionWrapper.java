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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.CMISUtils;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SecondaryTypeDefinitionWrapper extends AbstractTypeDefinitionWrapper
{
    private static final long serialVersionUID = 1L;
    // Logger
    protected static final Log logger = LogFactory.getLog(SecondaryTypeDefinitionWrapper.class);

    private SecondaryTypeDefinitionImpl typeDef;
    private SecondaryTypeDefinitionImpl typeDefInclProperties;
    private DictionaryService dictionaryService;

    public SecondaryTypeDefinitionWrapper(CMISMapping cmisMapping, PropertyAccessorMapping propertyAccessorMapping, 
            PropertyLuceneBuilderMapping luceneBuilderMapping, String typeId, DictionaryService dictionaryService, ClassDefinition cmisClassDef)
    {
        this.dictionaryService = dictionaryService;
        alfrescoName = cmisClassDef.getName();
        alfrescoClass = cmisMapping.getAlfrescoClass(alfrescoName);

        typeDef = new SecondaryTypeDefinitionImpl();

        typeDef.setBaseTypeId(BaseTypeId.CMIS_SECONDARY);
        typeDef.setId(typeId);
        typeDef.setLocalName(alfrescoName.getLocalName());
        typeDef.setLocalNamespace(alfrescoName.getNamespaceURI());

        if (BaseTypeId.CMIS_SECONDARY.value().equals(typeId))
        {
            typeDef.setQueryName(ISO9075.encodeSQL(typeId));
            typeDef.setParentTypeId(null);
        }
        else
        {
            typeDef.setQueryName(ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(alfrescoName)));
            QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
            if (parentQName == null)
            {
                typeDef.setParentTypeId(cmisMapping.getCmisTypeId(CMISMapping.SECONDARY_TYPES_QNAME));
            } else if (cmisMapping.isValidCmisSecondaryType(parentQName))
            {
                typeDef.setParentTypeId(cmisMapping.getCmisTypeId(BaseTypeId.CMIS_SECONDARY, parentQName));
            } else
            {
                throw new IllegalStateException("The CMIS type model should ignore aspects that inherit from excluded aspects");
            }
        }

        typeDef.setDisplayName(null);
        typeDef.setDescription(null);

        typeDef.setIsCreatable(false);
        typeDef.setIsQueryable(true);
        typeDef.setIsFulltextIndexed(true);
        typeDef.setIsControllablePolicy(false);
        typeDef.setIsControllableAcl(false);
        typeDef.setIsIncludedInSupertypeQuery(cmisClassDef.getIncludedInSuperTypeQuery());
        typeDef.setIsFileable(false);

        typeDefInclProperties = CMISUtils.copy(typeDef);
        setTypeDefinition(typeDef, typeDefInclProperties);

        createOwningPropertyDefinitions(cmisMapping, propertyAccessorMapping, luceneBuilderMapping, dictionaryService, cmisClassDef);
        createActionEvaluators(propertyAccessorMapping, BaseTypeId.CMIS_SECONDARY);
    }

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

        // find children
//        children = new ArrayList<TypeDefinitionWrapper>();
        Collection<QName> childrenNames = null;

        if (isBaseType())
        {
//            // add the "Aspects" type to the CMIS secondary type
//            childrenNames = new ArrayList<QName>();
//            childrenNames.add(CMISMapping.SECONDARY_TYPES_QNAME);
//        } else if (getAlfrescoName().equals(CMISMapping.SECONDARY_TYPES_QNAME))
//        {
            // add all root aspects to the "Aspects" type
            childrenNames = new ArrayList<QName>();

            String aspectsTypeId = cmisMapping.getCmisTypeId(CMISMapping.SECONDARY_TYPES_QNAME);
            for (AbstractTypeDefinitionWrapper tdw : registry.getTypeDefs())
            {
                String parentId = tdw.getTypeDefinition(false).getParentTypeId();
                if ((parentId != null) && parentId.equals(aspectsTypeId))
                {
                    childrenNames.add(tdw.getAlfrescoName());
                }
            }
        } else
        {
            // add all non-root aspects to their parent
            childrenNames = dictionaryService.getSubAspects(cmisMapping.getAlfrescoClass(getAlfrescoName()), false);
        }

        List<TypeDefinitionWrapper> children = new LinkedList<TypeDefinitionWrapper>();
        for (QName childName : childrenNames)
        {
            if (cmisMapping.isValidCmisSecondaryType(childName))
            {
                TypeDefinitionWrapper child = registry.getTypeDefByQName(childName);

                if (child == null)
                {
                    throw new AlfrescoRuntimeException("Failed to retrieve sub type for type id " + childName
                            + " for parent type " + getAlfrescoName() + "!");
                }
                children.add(child);
            }
            else
            {
                logger.info("Not a secondary type: " + childName);
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
    
    @Override
    public void updateDefinition(DictionaryService dictionaryService)
    {
        AspectDefinition aspectDef = dictionaryService.getAspect(alfrescoName);

        if (aspectDef != null)
        {
            setTypeDefDisplayName(aspectDef.getTitle(dictionaryService));
            setTypeDefDescription(aspectDef.getDescription(dictionaryService));
        }
        else
        {
            super.updateDefinition(dictionaryService);
        }
        
        updateTypeDefInclProperties();
    }
    
    @Override
    public PropertyDefinitionWrapper getPropertyById(String propertyId)
    {
        updateProperty(dictionaryService, propertiesById.get(propertyId));
        return propertiesById.get(propertyId);
    }

    @Override
    public Collection<PropertyDefinitionWrapper> getProperties()
    {
        updateProperties(dictionaryService);
        return propertiesById.values();
    }
    
    @Override
    public Collection<PropertyDefinitionWrapper> getProperties(boolean update)
    {
        if (update)
        {
            return getProperties();
        }
        else
        {
            return propertiesById.values();
        }
    }
}
