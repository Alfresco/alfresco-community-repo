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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Service to query the CMIS meta model
 * 
 * @author andyh
 */
public class CMISDictionaryService
{
    private CMISMapping cmisMapping;
    
    private DictionaryService dictionaryService;
    
    private boolean strict = true;

    /**
     * Set the mapping service
     * 
     * @param cmisMapping
     */
    public void setCMISMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }
    
    /**
     * @return  cmis mapping service
     */
    public CMISMapping getCMISMapping()
    {
        return cmisMapping;
    }
    
    /**
     * Set the dictionary Service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Gets the dictionary service
     * 
     * @return dictionaryService
     */
    /*package*/ DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }
    
    /**
     * Is the service strict (CMIS types only)
     * 
     * @return
     */
    public boolean isStrict()
    {
        return strict;
    }

    /**
     * Set strict mode. In strict mode only CMIS types and properties are returned
     * 
     * @param strict
     */
    public void setStrict(boolean strict)
    {
        this.strict = strict;
    }

    /**
     * Get the all the object types ids TODO: Note there can be name collisions between types and associations. e.g.
     * app:configurations
     * 
     * @return
     */
    public Collection<CMISTypeId> getAllObjectTypeIds()
    {
        Collection<QName> alfrescoTypeQNames;
        Collection<QName> alfrescoAssociationQNames;

        if (strict)
        {
            alfrescoTypeQNames = dictionaryService.getTypes(CMISMapping.CMIS_MODEL_QNAME);
            alfrescoAssociationQNames = dictionaryService.getAssociations(CMISMapping.CMIS_MODEL_QNAME);
        }
        else
        {
            alfrescoTypeQNames = dictionaryService.getAllTypes();
            alfrescoAssociationQNames = dictionaryService.getAllAssociations();
        }

        Collection<CMISTypeId> answer = new HashSet<CMISTypeId>(alfrescoTypeQNames.size() + alfrescoAssociationQNames.size());

        for (QName typeQName : alfrescoTypeQNames)
        {
            if (cmisMapping.isValidCmisDocument(typeQName))
            {
                answer.add(cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, typeQName));
            }
            else if (cmisMapping.isValidCmisFolder(typeQName))
            {
                answer.add(cmisMapping.getCmisTypeId(CMISScope.FOLDER, typeQName));
            }
            else if (typeQName.equals(CMISMapping.RELATIONSHIP_QNAME))
            {
                answer.add(cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, typeQName));
            }
        }

        for (QName associationName : alfrescoAssociationQNames)
        {
            if (cmisMapping.isValidCmisRelationship(associationName))
            {
                answer.add(cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, associationName));
            }
        }

        return answer;
    }

    /**
     * Gets all the object type ids within a type hierarchy
     * 
     * @param typeId
     * @param descendants  true => include all descendants, false => children only
     * @return
     */
    public Collection<CMISTypeId> getChildTypeIds(CMISTypeId typeId, boolean descendants)
    {
        switch (typeId.getScope())
        {
        case RELATIONSHIP:
            if (typeId.equals(CMISMapping.RELATIONSHIP_TYPE_ID))
            {
                // all associations are sub-type of RELATIONSHIP_OBJECT_TYPE
                // NOTE: ignore descendants
                Collection<QName> alfrescoAssociationQNames = dictionaryService.getAllAssociations();
                Collection<CMISTypeId> types = new HashSet<CMISTypeId>(alfrescoAssociationQNames.size());
                for (QName associationName : alfrescoAssociationQNames)
                {
                    if (cmisMapping.isValidCmisRelationship(associationName))
                    {
                        types.add(cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, associationName));
                    }
                }
                return types;
            }
            else
            {
                return Collections.emptySet();
            }
        case DOCUMENT:
        case FOLDER:
            TypeDefinition typeDefinition = dictionaryService.getType(typeId.getQName());
            if (typeDefinition != null)
            {
                if (cmisMapping.isValidCmisType(typeId.getQName()))
                {
                    QName alfrescoQName = cmisMapping.getAlfrescoType(typeId.getQName());
                    Collection<QName> alfrescoTypeQNames = dictionaryService.getSubTypes(alfrescoQName, descendants);
                    Collection<CMISTypeId> types = new HashSet<CMISTypeId>(alfrescoTypeQNames.size());
                    for (QName typeQName : alfrescoTypeQNames)
                    {
                        CMISTypeId subTypeId = cmisMapping.getCmisTypeId(typeQName);
                        if (typeId != null)
                        {
                            types.add(subTypeId);
                        }
                    }
                    return types;
                }
                else
                {
                    return Collections.emptySet();
                }
            }
            else
            {
                return Collections.emptySet();
            }
        default:
            return Collections.emptySet();
        }
    }
    
    /**
     * Get the object type definition TODO: Note there can be name collisions between types and associations. e.g.
     * app:configurations Currently clashing types will give inconsistent behaviour
     * 
     * @param typeId
     * @return
     */
    public CMISTypeDefinition getType(CMISTypeId typeId)
    {
        switch (typeId.getScope())
        {
        case RELATIONSHIP:
            // Associations
            if (cmisMapping.isValidCmisRelationship(typeId.getQName()))
            {
                return new CMISTypeDefinition(this, typeId);
            }
            else
            {
                return null;
            }
        case DOCUMENT:
        case FOLDER:
            TypeDefinition typeDefinition = dictionaryService.getType(typeId.getQName());
            if (typeDefinition != null)
            {
                if (cmisMapping.isValidCmisType(typeId.getQName()))
                {
                    return new CMISTypeDefinition(this, typeId);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        default:
            return null;
        }
    }
    
    /**
     * Get all the property definitions for a type
     * 
     * @param typeId
     * @return
     */
    public Map<String, CMISPropertyDefinition> getPropertyDefinitions(CMISTypeId typeId)
    {
        HashMap<String, CMISPropertyDefinition> properties = new HashMap<String, CMISPropertyDefinition>();

        switch (typeId.getScope())
        {
        case RELATIONSHIP:
            // Associations - only have CMIS properties
            AssociationDefinition associationDefinition = dictionaryService.getAssociation(typeId.getQName());
            if (associationDefinition != null)
            {
                if (cmisMapping.isValidCmisRelationship(typeId.getQName()))
                {
                    return getPropertyDefinitions(CMISMapping.RELATIONSHIP_TYPE_ID);
                }
                break;
            }

            if (!typeId.getQName().equals(CMISMapping.RELATIONSHIP_QNAME))
            {
                break;
            }
            // Fall through for CMISMapping.RELATIONSHIP_QNAME
        case DOCUMENT:
        case FOLDER:
            TypeDefinition typeDefinition = dictionaryService.getType(typeId.getQName());
            if (typeDefinition != null)
            {
                if (cmisMapping.isValidCmisDocumentOrFolder(typeId.getQName()) || typeId.getQName().equals(CMISMapping.RELATIONSHIP_QNAME))
                {
                    for (QName qname : typeDefinition.getProperties().keySet())
                    {
                        if (cmisMapping.getPropertyType(qname) != null)
                        {
                            CMISPropertyDefinition cmisPropDefinition = new CMISPropertyDefinition(this, qname, typeDefinition.getName());
                            properties.put(cmisPropDefinition.getPropertyName(), cmisPropDefinition);
                        }
                    }
                    for (AspectDefinition aspect : typeDefinition.getDefaultAspects())
                    {
                        for (QName qname : aspect.getProperties().keySet())
                        {
                            if (cmisMapping.getPropertyType(qname) != null)
                            {
                                CMISPropertyDefinition cmisPropDefinition = new CMISPropertyDefinition(this, qname, typeDefinition.getName());
                                properties.put(cmisPropDefinition.getPropertyName(), cmisPropDefinition);
                            }
                        }
                    }
                }
                if (cmisMapping.isValidCmisDocumentOrFolder(typeId.getQName()))
                {
                    // Add CMIS properties if required
                    if (!cmisMapping.isCmisCoreType(typeId.getQName()))
                    {
                        properties.putAll(getPropertyDefinitions(typeId.getRootTypeId()));
                    }
                }
            }
            break;
        case UNKNOWN:
        default:
            break;
        }

        return properties;
    }

    /**
     * Get a single property definition
     * 
     * @param typeId
     * @param propertyName
     * @return
     */
    public CMISPropertyDefinition getPropertyDefinition(CMISTypeId typeId, String propertyName)
    {
        return getPropertyDefinitions(typeId).get(propertyName);
    }
}
