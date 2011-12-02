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

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;


/**
 * CMIS Folder Type Definition
 * 
 * @author davidc
 */
public class CMISFolderTypeDefinition extends CMISAbstractTypeDefinition 
{
    private static final long serialVersionUID = 7526155195125799106L;

    protected final boolean fileable = true;

    /**
     * Construct 
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     */
    public CMISFolderTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef, boolean isSystem)
    {
        isPublic = true;
        
        // Object type properties
        this.cmisClassDef = cmisClassDef;
        objectTypeId = typeId;
        displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
        description = cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : displayName;
        
        QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
        if (typeId == CMISDictionaryModel.FOLDER_TYPE_ID)
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
            if (cmisMapping.isValidCmisFolder(parentQName))
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, parentQName);
            }
        }
        
        actionEvaluators = cmisMapping.getActionEvaluators(objectTypeId.getScope());
        
        // TODO: introduce abstract into core alfresco content metamodel
        creatable = !isSystem;
        queryable = true;
        fullTextIndexed = true;
        controllablePolicy = false;
        controllableACL = true;
        includedInSuperTypeQuery = cmisClassDef.getIncludedInSuperTypeQuery();
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISFolderTypeDefinition[");
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
        builder.append("SubTypes=").append(getSubTypes(false).size()).append(", ");
        builder.append("Properties=").append(getPropertyDefinitions().size());
        builder.append("]");
        return builder.toString();
    }
    
}
