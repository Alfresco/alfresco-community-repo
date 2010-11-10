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
package org.alfresco.cmis.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.dictionary.CMISAbstractDictionaryService.DictionaryRegistry;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Relationship Type Definition
 * 
 * @author davidc
 */
public class CMISRelationshipTypeDefinition extends CMISAbstractTypeDefinition 
{
    private static final long serialVersionUID = 5291428171784061346L;
    
    // Relationship properties
    private List<CMISTypeId> allowedSourceTypeIds = new ArrayList<CMISTypeId>();
    private List<CMISTypeDefinition> allowedSourceTypes = new ArrayList<CMISTypeDefinition>();
    private List<CMISTypeDefinition> inheritedAllowedSourceTypes = new ArrayList<CMISTypeDefinition>();
    private List<CMISTypeId> allowedTargetTypeIds = new ArrayList<CMISTypeId>();
    private List<CMISTypeDefinition> allowedTargetTypes = new ArrayList<CMISTypeDefinition>();
    private List<CMISTypeDefinition> inheritedAllowedTargetTypes = new ArrayList<CMISTypeDefinition>();


    /**
     * Construct
     *
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     * @param assocDef
     */
    public CMISRelationshipTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef, AssociationDefinition assocDef)
    {
        isPublic = true;
        this.cmisClassDef = cmisClassDef;
        objectTypeId = typeId;
        
        actionEvaluators = cmisMapping.getActionEvaluators(objectTypeId.getScope());
        
        queryable = false;
        fullTextIndexed = false;
        includedInSuperTypeQuery = true;
        controllablePolicy = false;
        controllableACL = false;
        
        if (assocDef == null)
        {
            // TODO: Add CMIS Association mapping??
            creatable = false;
            displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
            objectTypeQueryName = typeId.getId();
            QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
            if (parentQName != null)
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.OBJECT, parentQName);
            }
            description = cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : displayName;
        }
        else
        {
            creatable = true;
            displayName = (assocDef.getTitle() != null) ? assocDef.getTitle() : typeId.getId();
            objectTypeQueryName = cmisMapping.buildPrefixEncodedString(typeId.getQName());
            parentTypeId = CMISDictionaryModel.RELATIONSHIP_TYPE_ID;
            description = assocDef.getDescription() != null ? assocDef.getDescription() : displayName;

            CMISTypeId sourceTypeId = cmisMapping.getCmisTypeId(cmisMapping.getCmisType(assocDef.getSourceClass().getName()));
            if (sourceTypeId != null)
            {
                allowedSourceTypeIds.add(sourceTypeId);
            }

            CMISTypeId targetTypeId = cmisMapping.getCmisTypeId(cmisMapping.getCmisType(assocDef.getTargetClass().getName()));
            if (targetTypeId != null)
            {
                allowedTargetTypeIds.add(targetTypeId);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISObjectTypeDefinition#createProperties(org.alfresco.cmis.dictionary.CMISMapping, org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    @Override
    /*package*/ Map<String, CMISPropertyDefinition> createProperties(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        if (objectTypeId.equals(CMISDictionaryModel.RELATIONSHIP_TYPE_ID))
        {
            return super.createProperties(cmisMapping, dictionaryService);
        }
        properties = new HashMap<String, CMISPropertyDefinition>();
        return properties;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISObjectTypeDefinition#createSubTypes(org.alfresco.cmis.dictionary.CMISMapping, org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    @Override
    /*package*/ void createSubTypes(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        subTypeIds = new ArrayList<CMISTypeId>();
        if (objectTypeId.equals(CMISDictionaryModel.RELATIONSHIP_TYPE_ID))
        {
            // all associations are sub-type of RELATIONSHIP_OBJECT_TYPE
            Collection<QName> assocs = dictionaryService.getAllAssociations();
            for (QName assoc : assocs)
            {
                if (cmisMapping.isValidCmisRelationship(assoc))
                {
                    subTypeIds.add(cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, assoc));
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISObjectTypeDefinition#resolveDependencies(org.alfresco.cmis.dictionary.AbstractCMISDictionaryService.DictionaryRegistry)
     */
    @Override
    /*package*/ void resolveDependencies(DictionaryRegistry registry)
    {
        super.resolveDependencies(registry);
        for (CMISTypeId sourceTypeId : allowedSourceTypeIds)
        {
            CMISTypeDefinition type = registry.objectDefsByTypeId.get(sourceTypeId);
            if (type == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve allowed source type for type id " + sourceTypeId);
            }
            if (type.isPublic() == isPublic)
            {
                allowedSourceTypes.add(type);
            }
        }
        for (CMISTypeId targetTypeId : allowedTargetTypeIds)
        {
            CMISTypeDefinition type = registry.objectDefsByTypeId.get(targetTypeId);
            if (type == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve allowed target type for type id " + targetTypeId);
            }
            if (type.isPublic() == isPublic)
            {
                allowedTargetTypes.add(type);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISObjectTypeDefinition#resolveInheritance(org.alfresco.cmis.dictionary.AbstractCMISDictionaryService.DictionaryRegistry)
     */
    @Override
    /*package*/ void resolveInheritance(DictionaryRegistry registry)
    {
        super.resolveInheritance(registry);
        inheritedAllowedSourceTypes.addAll(allowedSourceTypes);
        inheritedAllowedTargetTypes.addAll(allowedTargetTypes);
        if (internalParentType != null)
        {
            inheritedAllowedSourceTypes.addAll(internalParentType.getAllowedSourceTypes());
            inheritedAllowedTargetTypes.addAll(internalParentType.getAllowedTargetTypes());
        }
    }
    
    /**
     * For an association, get the collection of valid source types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedSourceTypes()
    {
        return inheritedAllowedSourceTypes;
    }

    /**
     * For an association, get the collection of valid target types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedTargetTypes()
    {
        return inheritedAllowedTargetTypes;
    }
    

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISRelationshipTypeDefinition[");
        builder.append("Id=").append(getTypeId().getId()).append(", ");
        builder.append("Namespace=").append(getTypeId().getLocalNamespace()).append(", ");
        builder.append("LocalName=").append(getTypeId().getLocalName()).append(", ");
        builder.append("QueryName=").append(getQueryName()).append(", ");
        builder.append("DisplayName=").append(getDisplayName()).append(", ");
        builder.append("ParentId=").append(getParentType() == null ? "<none>" : getParentType().getTypeId()).append(", ");
        builder.append("Description=").append(getDescription()).append(", ");
        builder.append("Creatable=").append(isCreatable()).append(", ");
        builder.append("Queryable=").append(isQueryable()).append(", ");
        builder.append("Controllable=").append(isControllablePolicy()).append(", ");
        builder.append("IncludedInSuperTypeQuery=").append(isIncludedInSuperTypeQuery()).append(", ");
        builder.append("AllowedSourceTypes=[");
        for (CMISTypeDefinition type : getAllowedSourceTypes())
        {
            builder.append(type.getTypeId()).append(",");
        }
        builder.append("], ");
        builder.append("AllowedTargetTypes=[");
        for (CMISTypeDefinition type : getAllowedTargetTypes())
        {
            builder.append(type.getTypeId()).append(",");
        }
        builder.append("], ");
        builder.append("SubTypes=").append(getSubTypes(false).size()).append(", ");
        builder.append("Properties=").append(getPropertyDefinitions().size());
        builder.append("]");
        return builder.toString();
    }

}
