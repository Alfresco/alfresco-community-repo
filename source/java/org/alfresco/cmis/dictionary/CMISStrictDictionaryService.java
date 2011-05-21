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

import java.util.Collection;

import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Dictionary which provides Types that strictly conform to the CMIS specification.
 * 
 * That is, only maps types to one of root Document, Folder, Relationship & Policy.
 *  
 * @author davidc
 */
public class CMISStrictDictionaryService extends CMISAbstractDictionaryService
{
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.AbstractCMISDictionaryService#createDefinitions(org.alfresco.cmis.dictionary.AbstractCMISDictionaryService.DictionaryRegistry)
     */
    @Override
    protected void createDefinitions(DictionaryRegistry registry)
    {
        createTypeDefs(registry, dictionaryService.getAllTypes());
        createAssocDefs(registry, dictionaryService.getAllAssociations());
        createTypeDefs(registry, dictionaryService.getAllAspects());
    }

    /**
     * Create Type Definitions
     * 
     * @param registry
     * @param classQNames
     */
    private void createTypeDefs(DictionaryRegistry registry, Collection<QName> classQNames)
    {
        for (QName classQName : classQNames)
        {
            // skip items that are remapped to CMIS model
            if (cmisMapping.isRemappedType(classQName))
                continue;
            
            // skip all items that are not mapped to CMIS model
            CMISTypeId typeId = cmisMapping.getCmisTypeId(classQName);
            if (typeId == null)
                continue;
            if (typeId.getScope() == CMISScope.RELATIONSHIP)
                continue;
            
            // create appropriate kind of type definition
            ClassDefinition classDef = dictionaryService.getClass(cmisMapping.getCmisType(typeId.getQName()));
            CMISAbstractTypeDefinition objectTypeDef = null;
            if (typeId.getScope() == CMISScope.DOCUMENT)
            {
                objectTypeDef = new CMISDocumentTypeDefinition(cmisMapping, typeId, classDef);
            }
            else if (typeId.getScope() == CMISScope.FOLDER)
            {
                boolean isSystem = dictionaryService.isSubClass(classDef.getName(), ContentModel.TYPE_SYSTEM_FOLDER);
                objectTypeDef = new CMISFolderTypeDefinition(cmisMapping, typeId, classDef, isSystem);
            }
            else if (typeId.getScope() == CMISScope.POLICY)
            {
                objectTypeDef = new CMISPolicyTypeDefinition(cmisMapping, typeId, classDef);
            }
            else if (typeId.getScope() == CMISScope.OBJECT)
            {
                objectTypeDef = new CMISObjectTypeDefinition(cmisMapping, typeId, classDef, false);
            }

            registry.registerTypeDefinition(objectTypeDef);
        }
    }

    /**
     * Create Relationship Definitions
     * 
     * @param registry
     * @param classQNames
     */
    private void createAssocDefs(DictionaryRegistry registry, Collection<QName> classQNames)
    {
        CMISTypeId typeId = cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, CMISMapping.RELATIONSHIP_QNAME);
        ClassDefinition classDef = dictionaryService.getClass(cmisMapping.getCmisType(typeId.getQName()));
        CMISAbstractTypeDefinition objectTypeDef = new CMISRelationshipTypeDefinition(cmisMapping, typeId, classDef, null);
        registry.registerTypeDefinition(objectTypeDef);
        
        for (QName classQName : classQNames)
        {
            if (!cmisMapping.isValidCmisRelationship(classQName))
                continue;

            // create appropriate kind of type definition
            typeId = cmisMapping.getCmisTypeId(CMISScope.RELATIONSHIP, classQName);
            AssociationDefinition assocDef = dictionaryService.getAssociation(classQName);
            objectTypeDef = new CMISRelationshipTypeDefinition(cmisMapping, typeId, null, assocDef);

            registry.registerTypeDefinition(objectTypeDef);
        }
    }

}
