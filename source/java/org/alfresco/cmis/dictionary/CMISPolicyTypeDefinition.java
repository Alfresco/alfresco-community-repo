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

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.dictionary.CMISAbstractDictionaryService.DictionaryRegistry;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;


/**
 * CMIS Policy Type Definition
 * 
 * @author davidc
 */
public class CMISPolicyTypeDefinition extends CMISAbstractTypeDefinition 
{
    private static final long serialVersionUID = 1621538303752395828L;

    /**
     * Construct
     * 
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     */
    public CMISPolicyTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef)
    {
        isPublic = true;
        
        // Object Type definitions
        this.cmisClassDef = cmisClassDef;
        objectTypeId = typeId;
        displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
        if (typeId == CMISDictionaryModel.POLICY_TYPE_ID)
        {
            objectTypeQueryName = typeId.getId();
            QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
            if (parentQName != null)
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.OBJECT, parentQName);
            }
        }
        else
        {
            objectTypeQueryName = ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(typeId.getQName()));
            parentTypeId = CMISDictionaryModel.POLICY_TYPE_ID;
        }
        description = cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : displayName;
        
        actionEvaluators = cmisMapping.getActionEvaluators(objectTypeId.getScope());
        
        creatable = false;
        queryable = true;
        includedInSuperTypeQuery = cmisClassDef.getIncludedInSuperTypeQuery();
        fullTextIndexed = true;
        controllablePolicy = false;
        controllableACL = false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISObjectTypeDefinition#createSubTypes(org.alfresco.cmis.dictionary.CMISMapping, org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    @Override
    /*package*/ void createSubTypes(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        subTypeIds = new ArrayList<CMISTypeId>();
        if (objectTypeId.equals(CMISDictionaryModel.POLICY_TYPE_ID))
        {
            // all aspects are sub-type of POLICY_OBJECT_TYPE
            Collection<QName> aspects = dictionaryService.getAllAspects();
            for (QName aspect : aspects)
            {
                if (cmisMapping.isValidCmisPolicy(aspect))
                {
                    subTypeIds.add(cmisMapping.getCmisTypeId(CMISScope.POLICY, aspect));
                }
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
        inheritedProperties = new HashMap<String, CMISPropertyDefinition>();
        ownedProperties = new HashMap<String, CMISPropertyDefinition>();
        // NOTE: Force no inheritance of base Policy type
        inheritedProperties.putAll(properties);
        ownedProperties.putAll(properties);
        
        if (logger.isDebugEnabled())
            logger.debug("Type " + objectTypeId + " properties: " + inheritedProperties.size() + ", owned: " + ownedProperties.size());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISPolicyTypeDefinition[");
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
        builder.append("SubTypes=").append(getSubTypes(false).size()).append(", ");
        builder.append("Properties=").append(getPropertyDefinitions().size());
        builder.append("]");
        return builder.toString();
    }

}
