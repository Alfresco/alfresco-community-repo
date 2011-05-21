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

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;


/**
 * CMIS Document Type Definition
 * 
 * @author davidc
 */
public class CMISDocumentTypeDefinition extends CMISAbstractTypeDefinition 
{
    private static final long serialVersionUID = -7209732754962781522L;
    
    // document specific properties
    protected boolean fileable;
    protected CMISContentStreamAllowedEnum contentStreamAllowed;


    /**
     * Construct
     * 
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     */
    public CMISDocumentTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef)
    {
        isPublic = true;
        
        // Object type properties
        this.cmisClassDef = cmisClassDef;
        objectTypeId = typeId;
        displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
        description = cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : displayName;

        QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
        if (typeId == CMISDictionaryModel.DOCUMENT_TYPE_ID)
        {
            objectTypeQueryName = typeId.getId();
            if (parentQName != null)
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.OBJECT, parentQName);
            }
        }
        else
        {
            objectTypeQueryName = ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(typeId.getQName()));
            if (cmisMapping.isValidCmisDocument(parentQName))
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, parentQName);
            }
        }
        
        creatable = true;
        queryable = true;
        fullTextIndexed = true;
        controllablePolicy = false;
        controllableACL = true;
        includedInSuperTypeQuery = cmisClassDef.getIncludedInSuperTypeQuery();

        actionEvaluators = cmisMapping.getActionEvaluators(objectTypeId.getScope());

        fileable = true;
        contentStreamAllowed = CMISContentStreamAllowedEnum.ALLOWED;
    }

    /**
     * Are objects of this type fileable?
     * 
     * @return
     */
    public boolean isFileable()
    {
        return fileable;
    }

    /**
     * Is this type versionable? If true this implies all instances of the type are versionable.
     * 
     * @return true if versionable
     */
    public boolean isVersionable()
    {
        // Documents and their subtypes are versionable
        return true;
    }

    /**
     * Is a content stream allowed for this type? It may be disallowed, optional or mandatory.
     * 
     * @return
     */
    public CMISContentStreamAllowedEnum getContentStreamAllowed()
    {
        return contentStreamAllowed;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISDocumentTypeDefinition[");
        builder.append("Id=").append(getTypeId().getId()).append(", ");
        builder.append("Namespace=").append(getTypeId().getLocalNamespace()).append(", ");
        builder.append("LocalName=").append(getTypeId().getLocalName()).append(", ");
        builder.append("QueryName=").append(getQueryName()).append(", ");
        builder.append("DisplayName=").append(getDisplayName()).append(", ");
        builder.append("ParentId=").append(getParentType() == null ? "<none>" : getParentType().getTypeId()).append(", ");
        builder.append("Description=").append(getDescription()).append(", ");
        builder.append("Creatable=").append(isCreatable()).append(", ");
        builder.append("Queryable=").append(isQueryable()).append(", ");
        builder.append("FullTextIndexed=").append(isFullTextIndexed()).append(", ");
        builder.append("IncludedInSuperTypeQuery=").append(isIncludedInSuperTypeQuery()).append(", ");
        builder.append("ControllablePolicy=").append(isControllablePolicy()).append(", ");
        builder.append("ControllableACL=").append(isControllableACL()).append(", ");
        builder.append("Fileable=").append(isFileable()).append(", ");
        builder.append("Versionable=").append(isVersionable()).append(", ");
        builder.append("ContentStreamAllowed=").append(getContentStreamAllowed()).append(", ");
        builder.append("SubTypes=").append(getSubTypes(false).size()).append(", ");
        builder.append("Properties=").append(getPropertyDefinitions().size());
        builder.append("]");
        return builder.toString();
    }

}
