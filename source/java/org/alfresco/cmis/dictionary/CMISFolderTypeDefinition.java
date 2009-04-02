/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.dictionary;

import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Folder Type Definition
 * 
 * @author davidc
 */
public class CMISFolderTypeDefinition extends CMISAbstractTypeDefinition 
{
    private static final long serialVersionUID = 7526155195125799106L;

    /**
     * Construct 
     * @param cmisMapping
     * @param typeId
     * @param cmisClassDef
     */
    public CMISFolderTypeDefinition(CMISMapping cmisMapping, CMISTypeId typeId, ClassDefinition cmisClassDef)
    {
        isPublic = true;
        
        // Object type properties
        this.cmisClassDef = cmisClassDef;
        objectTypeId = typeId;
        displayName = (cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId.getId();
        description = cmisClassDef.getDescription();
        
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
            objectTypeQueryName = cmisMapping.buildPrefixEncodedString(typeId.getQName(), false);
            if (cmisMapping.isValidCmisFolder(parentQName))
            {
                parentTypeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, parentQName);
            }
        }
        
        creatable = true;
        queryable = true;
        controllable = false;
        includeInSuperTypeQuery = true;
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
        builder.append("ObjectTypeId=").append(getTypeId()).append(", ");
        builder.append("ObjectTypeQueryName=").append(getQueryName()).append(", ");
        builder.append("ObjectTypeDisplayName=").append(getDisplayName()).append(", ");
        builder.append("ParentTypeId=").append(getParentType() == null ? "<none>" : getParentType().getTypeId()).append(", ");
        builder.append("Description=").append(getDescription()).append(", ");
        builder.append("Creatable=").append(isCreatable()).append(", ");
        builder.append("Queryable=").append(isQueryable()).append(", ");
        builder.append("Controllable=").append(isControllable()).append(", ");
        builder.append("IncludeInSuperTypeQuery=").append(isIncludeInSuperTypeQuery()).append(", ");
        builder.append("SubTypes=").append(getSubTypes(false).size()).append(", ");
        builder.append("Properties=").append(getPropertyDefinitions().size());
        builder.append("]");
        return builder.toString();
    }
    
}
