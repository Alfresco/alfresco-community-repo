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

import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Object Type Definition
 * 
 * @author davidc
 */
public class CMISObjectTypeDefinition extends CMISAbstractTypeDefinition
{
    private static final long serialVersionUID = -3131505923356013430L;


    /**
     * Construct
     * 
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     */
    public CMISObjectTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef, boolean isPublic)
    {
        this.isPublic = isPublic;
        
        // Object type properties
        objectTypeId = typeId;
        objectTypeQueryName = cmisMapping.buildPrefixEncodedString(typeId.getQName());

        if (cmisClassDef != null)
        {
            this.cmisClassDef = cmisClassDef;
            displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
            description = cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : displayName;
            QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
            if (parentQName != null)
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.OBJECT, parentQName);
            }
        }
        
        actionEvaluators = cmisMapping.getActionEvaluators(objectTypeId.getScope());
                
        creatable = false;
        queryable = false;
        fullTextIndexed = false;
        includedInSuperTypeQuery = cmisClassDef.getIncludedInSuperTypeQuery();
        controllablePolicy = false;
        controllableACL = false;
    }
    
    /**
     * Create Sub Types
     * 
     * @param cmisMapping
     * @param dictionaryService
     */
    /*package*/ void createSubTypes(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        subTypeIds = new ArrayList<CMISTypeId>();
        Collection<QName> subTypes = dictionaryService.getSubTypes(objectTypeId.getQName(), false);
        for (QName subType : subTypes)
        {
            CMISTypeId subTypeId = cmisMapping.getCmisTypeId(subType);
            if (subTypeId != null)
            {
                subTypeIds.add(subTypeId);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISObjectTypeDefinition[");
        builder.append("Public=").append(isPublic()).append(", ");
        builder.append("ObjectTypeId=").append(getTypeId()).append(", ");
        builder.append("ObjectTypeQueryName=").append(getQueryName()).append(", ");
        builder.append("ObjectTypeDisplayName=").append(getDisplayName()).append(", ");
        builder.append("ParentTypeId=").append(getParentType() == null ? "<none>" : getParentType().getTypeId()).append(", ");
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
