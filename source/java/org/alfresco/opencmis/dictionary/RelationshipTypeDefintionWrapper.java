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
import java.util.Collections;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.CMISUtils;
import org.alfresco.opencmis.dictionary.CMISAbstractDictionaryService.DictionaryRegistry;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;

public class RelationshipTypeDefintionWrapper extends AbstractTypeDefinitionWrapper
{
    private static final long serialVersionUID = 1L;

    private RelationshipTypeDefinitionImpl typeDef;
    private RelationshipTypeDefinitionImpl typeDefInclProperties;

    public RelationshipTypeDefintionWrapper(CMISMapping cmisMapping, ServiceRegistry serviceRegistry, String typeId,
            ClassDefinition cmisClassDef)
    {
        alfrescoName = cmisClassDef.getName();
        alfrescoClass = cmisMapping.getAlfrescoClass(alfrescoName);

        typeDef = new RelationshipTypeDefinitionImpl();

        typeDef.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        typeDef.setId(typeId);
        typeDef.setLocalName(alfrescoName.getLocalName());
        typeDef.setLocalNamespace(alfrescoName.getNamespaceURI());

        if (BaseTypeId.CMIS_RELATIONSHIP.value().equals(typeId))
        {
            typeDef.setQueryName(typeId);
            typeDef.setParentTypeId(null);
        } else
        {
            typeDef.setQueryName(cmisMapping.buildPrefixEncodedString(alfrescoName));
            typeDef.setParentTypeId(BaseTypeId.CMIS_RELATIONSHIP.value());
        }

        typeDef.setDisplayName(cmisClassDef.getTitle() != null ? cmisClassDef.getTitle() : typeId);
        typeDef.setDescription(cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : typeDef
                .getDisplayName());

        typeDef.setIsCreatable(true);
        typeDef.setIsQueryable(false);
        typeDef.setIsFulltextIndexed(false);
        typeDef.setIsControllablePolicy(false);
        typeDef.setIsControllableAcl(false);
        typeDef.setIsIncludedInSupertypeQuery(true);
        typeDef.setIsFileable(false);

        typeDefInclProperties = CMISUtils.copy(typeDef);
        setTypeDefinition(typeDef, typeDefInclProperties);

        createOwningPropertyDefinitions(cmisMapping, serviceRegistry, cmisClassDef);

        actionEvaluators = cmisMapping.getActionEvaluators(BaseTypeId.CMIS_RELATIONSHIP);
    }

    public RelationshipTypeDefintionWrapper(CMISMapping cmisMapping, String typeId, AssociationDefinition cmisAssocDef)
    {
        alfrescoName = cmisAssocDef.getName();
        alfrescoClass = cmisMapping.getAlfrescoClass(alfrescoName);

        typeDef = new RelationshipTypeDefinitionImpl();

        typeDef.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        typeDef.setId(typeId);
        typeDef.setLocalName(alfrescoName.getLocalName());
        typeDef.setLocalNamespace(alfrescoName.getNamespaceURI());

        typeDef.setQueryName(cmisMapping.buildPrefixEncodedString(alfrescoName));
        typeDef.setParentTypeId(BaseTypeId.CMIS_RELATIONSHIP.value());

        typeDef.setDisplayName(cmisAssocDef.getTitle() != null ? cmisAssocDef.getTitle() : typeId);
        typeDef.setDescription(cmisAssocDef.getDescription() != null ? cmisAssocDef.getDescription() : typeDef
                .getDisplayName());

        typeDef.setIsCreatable(true);
        typeDef.setIsQueryable(false);
        typeDef.setIsFulltextIndexed(false);
        typeDef.setIsControllablePolicy(false);
        typeDef.setIsControllableAcl(false);
        typeDef.setIsIncludedInSupertypeQuery(true);
        typeDef.setIsFileable(false);

        String sourceTypeId = cmisMapping.getCmisTypeId(cmisMapping
                .getCmisType(cmisAssocDef.getSourceClass().getName()));
        if (sourceTypeId != null)
        {
            typeDef.setAllowedSourceTypes(Collections.singletonList(sourceTypeId));
        }

        String targetTypeId = cmisMapping.getCmisTypeId(cmisMapping
                .getCmisType(cmisAssocDef.getTargetClass().getName()));
        if (targetTypeId != null)
        {
            typeDef.setAllowedTargetTypes(Collections.singletonList(targetTypeId));
        }

        typeDefInclProperties = CMISUtils.copy(typeDef);
        setTypeDefinition(typeDef, typeDefInclProperties);

        actionEvaluators = cmisMapping.getActionEvaluators(BaseTypeId.CMIS_RELATIONSHIP);
    }

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
        if (isBaseType())
        {
            for (TypeDefinitionWrapper child : registry.assocDefsByQName.values())
            {
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
