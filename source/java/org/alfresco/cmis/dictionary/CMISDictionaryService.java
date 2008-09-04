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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Service to query the CMIS meta model
 * 
 * @author andyh
 */
public class CMISDictionaryService
{

    private DictionaryService dictionaryService;

    private NamespaceService namespaceService;

    private boolean strict = true;

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
     * Set the namespace service
     * 
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
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
            if (CMISMapping.isValidCmisType(dictionaryService, typeQName))
            {
                answer.add(CMISMapping.getCmisTypeId(typeQName));
            }
        }

        for (QName associationName : alfrescoAssociationQNames)
        {
            if (CMISMapping.isValidCmisAssociation(dictionaryService, associationName))
            {
                answer.add(CMISMapping.getCmisTypeId(associationName));
            }
        }

        return answer;
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
        // Types
        QName typeQName = CMISMapping.getTypeQname(typeId);
        TypeDefinition typeDefinition = dictionaryService.getType(typeQName);
        if (typeDefinition != null)
        {
            if (CMISMapping.isValidCmisType(dictionaryService, typeQName))
            {
                return new CMISTypeDefinition(dictionaryService, namespaceService, typeQName);
            }
            else
            {
                return null;
            }
        }

        // Associations
        AssociationDefinition associationDefinition = dictionaryService.getAssociation(typeQName);
        if (associationDefinition != null)
        {
            if (CMISMapping.isValidCmisAssociation(dictionaryService, typeQName))
            {
                return new CMISTypeDefinition(dictionaryService, namespaceService, typeQName);
            }
            else
            {
                return null;
            }
        }

        // Unknown type
        return null;
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

        QName typeQName = CMISMapping.getTypeQname(typeId);
        TypeDefinition typeDefinition = dictionaryService.getType(typeQName);
        if (typeDefinition != null)
        {
            if (CMISMapping.isValidCmisType(dictionaryService, typeQName))
            {
                for (QName qname : typeDefinition.getProperties().keySet())
                {
                    if (CMISMapping.getPropertyType(dictionaryService, qname) != null)
                    {
                        CMISPropertyDefinition cmisPropDefinition = new CMISPropertyDefinition(dictionaryService, namespaceService, qname);
                        properties.put(cmisPropDefinition.getPropertyName(), cmisPropDefinition);
                    }
                }
                for (AspectDefinition aspect : typeDefinition.getDefaultAspects())
                {
                    for (QName qname : aspect.getProperties().keySet())
                    {
                        if (CMISMapping.getPropertyType(dictionaryService, qname) != null)
                        {
                            CMISPropertyDefinition cmisPropDefinition = new CMISPropertyDefinition(dictionaryService, namespaceService, qname);
                            properties.put(cmisPropDefinition.getPropertyName(), cmisPropDefinition);
                        }
                    }
                }
            }
            else
            {
                return properties;
            }
        }

        // Associations
        AssociationDefinition associationDefinition = dictionaryService.getAssociation(typeQName);
        if (associationDefinition != null)
        {
            if (CMISMapping.isValidCmisAssociation(dictionaryService, typeQName))
            {
                return getPropertyDefinitions(new CMISTypeId(CMISMapping.RELATIONSHIP_OBJECT_TYPE));
            }
            else
            {
                return properties;
            }
        }

        // Unknown type
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
