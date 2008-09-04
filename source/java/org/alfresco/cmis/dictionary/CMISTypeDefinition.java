/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * The base type definition for CMIS
 * 
 * @author andyh
 */
public class CMISTypeDefinition implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2216695347624799934L;

    private CMISDictionaryService cmisDictionary;
    
    private CMISTypeId objectTypeId;

    private String objectTypeQueryName;

    private String displayName;

    private CMISTypeId parentTypeId;

    private String rootTypeQueryName;

    private String description;

    private boolean creatable;

    private boolean fileable;

    private boolean queryable;

    private boolean controllable;

    private boolean versionable;

    private ContentStreamAllowed contentStreamAllowed;

    private boolean isAssociation;

    private ArrayList<CMISTypeId> allowedSourceTypes = new ArrayList<CMISTypeId>(1);

    private ArrayList<CMISTypeId> allowedTargetTypes = new ArrayList<CMISTypeId>(1);

    
    /**
     * Construct
     *  
     * @param cmisDictionary
     * @param typeId
     */
    public CMISTypeDefinition(CMISDictionaryService cmisDictionary, CMISTypeId typeId)
    {
        this.cmisDictionary = cmisDictionary;
        DictionaryService dictionaryService = this.cmisDictionary.getDictionaryService();
        CMISMapping cmisMapping = cmisDictionary.getCMISMapping();
        
        switch (typeId.getScope())
        {
        case RELATIONSHIP:
            AssociationDefinition associationDefinition = dictionaryService.getAssociation(typeId.getQName());
            if (associationDefinition != null)
            {
                objectTypeId = typeId;
                objectTypeQueryName = cmisMapping.getQueryName(typeId.getQName());
                displayName = (associationDefinition.getTitle() != null) ? associationDefinition.getTitle() : typeId.getTypeId();
                parentTypeId = CMISMapping.RELATIONSHIP_TYPE_ID;
                rootTypeQueryName = cmisMapping.getQueryName(CMISMapping.RELATIONSHIP_QNAME);
                description = associationDefinition.getDescription();
                creatable = false;
                fileable = false;
                queryable = false;
                controllable = false;
                versionable = false;
                contentStreamAllowed = ContentStreamAllowed.NOT_ALLOWED;
                isAssociation = true;

                QName sourceType = cmisMapping.getCmisType(associationDefinition.getSourceClass().getName());
                if (cmisMapping.isValidCmisDocument(sourceType))
                {
                    allowedSourceTypes.add(cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, sourceType));
                }
                else if (cmisMapping.isValidCmisFolder(sourceType))
                {
                    allowedSourceTypes.add(cmisMapping.getCmisTypeId(CMISScope.FOLDER, sourceType));
                }

                QName targetType = cmisMapping.getCmisType(associationDefinition.getTargetClass().getName());
                if (cmisMapping.isValidCmisDocument(targetType))
                {
                    allowedTargetTypes.add(cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, targetType));
                }
                else if (cmisMapping.isValidCmisFolder(targetType))
                {
                    allowedTargetTypes.add(cmisMapping.getCmisTypeId(CMISScope.FOLDER, targetType));
                }
            }
            else
            {
                // TODO: Add CMIS Association mapping??
                TypeDefinition typeDefinition = dictionaryService.getType(typeId.getQName());
                objectTypeId = typeId;
                objectTypeQueryName = cmisMapping.getQueryName(typeId.getQName());
                displayName = (typeDefinition.getTitle() != null) ? typeDefinition.getTitle() : typeId.getTypeId();
                parentTypeId = null;
                rootTypeQueryName = cmisMapping.getQueryName(CMISMapping.RELATIONSHIP_QNAME);
                description = typeDefinition.getDescription();
                creatable = false;
                fileable = false;
                queryable = false;
                controllable = false;
                versionable = false;
                contentStreamAllowed = ContentStreamAllowed.NOT_ALLOWED;
                isAssociation = true;
            }
            break;
        case DOCUMENT:
        case FOLDER:
            TypeDefinition typeDefinition = dictionaryService.getType(typeId.getQName());
            if (typeDefinition != null)
            {
                objectTypeId = typeId;

                objectTypeQueryName = cmisMapping.getQueryName(typeId.getQName());

                displayName = (typeDefinition.getTitle() != null) ? typeDefinition.getTitle() : typeId.getTypeId();

                QName parentTypeQName = cmisMapping.getCmisType(typeDefinition.getParentName());
                if (parentTypeQName == null)
                {
                    // Core and unknown types
                    parentTypeId = null;
                }
                else
                {
                    if (cmisMapping.isValidCmisDocument(parentTypeQName))
                    {
                        parentTypeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, parentTypeQName);
                    }
                    else if (cmisMapping.isValidCmisFolder(parentTypeQName))
                    {
                        parentTypeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, parentTypeQName);
                    }
                }

                rootTypeQueryName = cmisMapping.getQueryName(typeId.getRootTypeId().getQName());

                description = typeDefinition.getDescription();

                creatable = true;

                fileable = true;

                queryable = true;

                controllable = false;

                versionable = false;

                if (typeId.getScope() == CMISScope.DOCUMENT)
                {
                    List<AspectDefinition> defaultAspects = typeDefinition.getDefaultAspects();
                    for (AspectDefinition aspectDefinition : defaultAspects)
                    {
                        if (aspectDefinition.getName().equals(ContentModel.ASPECT_VERSIONABLE))
                        {
                            versionable = true;
                            break;
                        }
                    }
                }

                if (typeId.getScope() == CMISScope.DOCUMENT)
                {
                    contentStreamAllowed = ContentStreamAllowed.ALLOWED;
                }
                else
                {
                    contentStreamAllowed = ContentStreamAllowed.NOT_ALLOWED;
                }
            }

            break;
        case UNKNOWN:
        default:
            break;
        }

    }

    /**
     * Get the unique identifier for the type
     * 
     * @return - the type id
     */
    public CMISTypeId getObjectTypeId()
    {
        return objectTypeId;
    }

    /**
     * Get the table name used for queries against the type. This is also a unique identifier for the type. The string
     * conforms to SQL table naming conventions. TODO: Should we impose a maximum length and if so how do we avoid
     * collisions from truncations?
     * 
     * @return the sql table name
     */
    public String getObjectTypeQueryName()
    {
        return objectTypeQueryName;
    }

    /**
     * Get the display name for the type.
     * 
     * @return - the display name
     */
    public String getObjectTypeDisplayName()
    {
        return displayName;
    }

    /**
     * Get the type id for the parent
     * 
     * @return - the parent type id
     */
    public CMISTypeId getParentTypeId()
    {
        return parentTypeId;
    }

    /**
     * Get the sql table name for the root type of this type This will be getObjectTypeQueryName() for the base folder,
     * document or association
     * 
     * @return - the sql table name for the root type
     */
    public String getRootTypeQueryName()
    {
        return rootTypeQueryName;
    }

    /**
     * Get the description for the type
     * 
     * @return - the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Can objects of this type be created?
     * 
     * @return
     */
    public boolean isCreatable()
    {
        return creatable;
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
     * Is this type queryable? If not, the type may not appear in the FROM clause of a query. This property of the type
     * is not inherited in the type hierarchy. It is set on each type.
     * 
     * @return true if queryable
     */
    public boolean isQueryable()
    {
        return queryable;
    }

    /**
     * Are objects of this type controllable.
     * 
     * @return
     */
    public boolean isControllable()
    {
        return controllable;
    }

    /**
     * Is this type versionable? If true this implies all instances of the type are versionable.
     * 
     * @return true if versionable
     */
    public boolean isVersionable()
    {
        return versionable;
    }

    /**
     * Is a content stream allowed for this type? It may be disallowed, optional or mandatory.
     * 
     * @return
     */
    public ContentStreamAllowed getContentStreamAllowed()
    {
        return contentStreamAllowed;
    }

    /**
     * Is this an association type?
     * 
     * @return true for an association type.
     */
    public boolean isAssociation()
    {
        return isAssociation;
    }

    /**
     * For an association, get the collection of valid source types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeId> getAllowedSourceTypes()
    {
        return allowedSourceTypes;
    }

    /**
     * For an association, get the collection of valid target types. For non-associations the collection will be empty.
     * 
     * @return
     */
    public Collection<CMISTypeId> getAllowedTargetTypes()
    {
        return allowedTargetTypes;
    }

    /**
     * Gets the property definitions for this type
     * 
     * @return  property definitions
     */
    public Map<String, CMISPropertyDefinition> getPropertyDefinitions()
    {
        return cmisDictionary.getPropertyDefinitions(objectTypeId);
    }
    
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CMISTypeDefinition[");
        builder.append("ObjectTypeId=").append(getObjectTypeId()).append(", ");
        builder.append("ObjectTypeQueryName=").append(getObjectTypeQueryName()).append(", ");
        builder.append("ObjectTypeDisplayName=").append(getObjectTypeDisplayName()).append(", ");
        builder.append("ParentTypeId=").append(getParentTypeId()).append(", ");
        builder.append("RootTypeQueryName=").append(getRootTypeQueryName()).append(", ");
        builder.append("Description=").append(getDescription()).append(", ");
        builder.append("Creatable=").append(isCreatable()).append(", ");
        builder.append("Fileable=").append(isFileable()).append(", ");
        builder.append("Queryable=").append(isQueryable()).append(", ");
        builder.append("Controllable=").append(isControllable()).append(", ");
        builder.append("Versionable=").append(isVersionable()).append(", ");
        builder.append("ContentStreamAllowed=").append(getContentStreamAllowed()).append(", ");
        builder.append("IsAssociation=").append(isAssociation()).append(", ");
        builder.append("AllowedSourceTypes=").append(getAllowedSourceTypes()).append(", ");
        builder.append("AllowedTargetTypes=").append(getAllowedTargetTypes());
        builder.append("]");
        return builder.toString();
    }
}
