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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * CMIS <-> Alfresco mappings
 * 
 * @author andyh
 */
public class CMISMapping
{
    /**
     * The Alfresco CMIS model URI.
     */
    public static String CMIS_MODEL_URI = "http://www.alfresco.org/model/cmis/0.3";

    /**
     * The Alfresco CMIS Model name.
     */
    public static String CMIS_MODEL_NAME = "cmismodel";

    /**
     * The QName for the Alfresco CMIS Model.
     */
    public static QName CMIS_MODEL_QNAME = QName.createQName(CMIS_MODEL_URI, CMIS_MODEL_NAME);

    /**
     * Type id for CMIS documents, from the spec.
     */
    public static String DOCUMENT_OBJECT_TYPE = "DOCUMENT_OBJECT_TYPE";

    /**
     * Type is for CMIS folders, from the spec.
     */
    public static String FOLDER_OBJECT_TYPE = "FOLDER_OBJECT_TYPE";

    /**
     * Type Id for CMIS Relationships, from the spec.
     */
    public static String RELATIONSHIP_OBJECT_TYPE = "RELATIONSHIP_OBJECT_TYPE";

    /**
     * QName for CMIS documents in the Alfresco CMIS model.
     */
    public static QName DOCUMENT_QNAME = QName.createQName(CMIS_MODEL_URI, DOCUMENT_OBJECT_TYPE);

    /**
     * QName for CMIS folders in the Alfresco CMIS model
     */
    public static QName FOLDER_QNAME = QName.createQName(CMIS_MODEL_URI, FOLDER_OBJECT_TYPE);

    /**
     * QName for CMIS relationships in the the Alfresco CMIS model.
     */
    public static QName RELATIONSHIP_QNAME = QName.createQName(CMIS_MODEL_URI, RELATIONSHIP_OBJECT_TYPE);

   

    // Mappings
    // - no entry means no mapping and pass through as is
    private static HashMap<CMISTypeId, QName> cmisTypeIdToTypeQName = new HashMap<CMISTypeId, QName>();

    private static HashMap<QName, CMISTypeId> qNameToCmisTypeId = new HashMap<QName, CMISTypeId>();

    private static HashMap<QName, QName> cmisToAlfrecsoTypes = new HashMap<QName, QName>();

    private static HashMap<QName, QName> alfrescoToCmisTypes = new HashMap<QName, QName>();

    private static HashMap<QName, CMISPropertyType> alfrescoPropertyTypesToCimsPropertyTypes = new HashMap<QName, CMISPropertyType>();
    /**
     * Set up mappings
     */
    static
    {
        cmisTypeIdToTypeQName.put(new CMISTypeId(DOCUMENT_OBJECT_TYPE), DOCUMENT_QNAME);
        cmisTypeIdToTypeQName.put(new CMISTypeId(FOLDER_OBJECT_TYPE), FOLDER_QNAME);
        cmisTypeIdToTypeQName.put(new CMISTypeId(RELATIONSHIP_OBJECT_TYPE), RELATIONSHIP_QNAME);

        qNameToCmisTypeId.put(DOCUMENT_QNAME, new CMISTypeId(DOCUMENT_OBJECT_TYPE));
        qNameToCmisTypeId.put(FOLDER_QNAME, new CMISTypeId(FOLDER_OBJECT_TYPE));
        qNameToCmisTypeId.put(RELATIONSHIP_QNAME, new CMISTypeId(RELATIONSHIP_OBJECT_TYPE));

        cmisToAlfrecsoTypes.put(DOCUMENT_QNAME, ContentModel.TYPE_CONTENT);
        cmisToAlfrecsoTypes.put(FOLDER_QNAME, ContentModel.TYPE_FOLDER);
        cmisToAlfrecsoTypes.put(RELATIONSHIP_QNAME, null);

        alfrescoToCmisTypes.put(ContentModel.TYPE_CONTENT, DOCUMENT_QNAME);
        alfrescoToCmisTypes.put(ContentModel.TYPE_FOLDER, FOLDER_QNAME);
        
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.ANY, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.ASSOC_REF, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.BOOLEAN, CMISPropertyType.BOOLEAN);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.CATEGORY, CMISPropertyType.ID);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.CHILD_ASSOC_REF, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.CONTENT, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.DATE, CMISPropertyType.DATE_TIME);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.DATETIME, CMISPropertyType.DATE_TIME);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.DOUBLE, CMISPropertyType.DECIMAL);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.FLOAT, CMISPropertyType.DECIMAL);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.INT, CMISPropertyType.INTEGER);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.LOCALE, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.LONG, CMISPropertyType.INTEGER);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.MLTEXT, CMISPropertyType.STRING);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.NODE_REF, CMISPropertyType.ID);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.PATH, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.QNAME, null);
        alfrescoPropertyTypesToCimsPropertyTypes.put(DataTypeDefinition.TEXT, CMISPropertyType.STRING);

    }

    /**
     * Id this a CMIS core type defined in the Alfresco CMIS model
     * 
     * @param typeQName
     * @return
     */
    public static boolean isCmisCoreType(QName typeQName)
    {
        return qNameToCmisTypeId.get(typeQName) != null;
    }

    /**
     * Get the CMIS Type Id given the Alfresco QName for the type in any Alfresco model
     * 
     * @param typeQName
     * @return
     */
    public static CMISTypeId getCmisTypeId(QName typeQName)
    {
        CMISTypeId typeId = qNameToCmisTypeId.get(typeQName);
        if (typeId == null)
        {
            return new CMISTypeId(typeQName.toString());
        }
        else
        {
            return typeId;
        }
    }

    /**
     * Given a type id - get the appropriate Alfresco QName
     * 
     * @param typeId
     * @return
     */
    public static QName getTypeQname(CMISTypeId typeId)
    {
        QName typeQName = cmisTypeIdToTypeQName.get(typeId);
        if (typeQName != null)
        {
            return typeQName;
        }
        else
        {
            return QName.createQName(typeId.getTypeId());
        }
    }

    /**
     * Get the query name for Alfresco qname
     * 
     * @param namespaceService
     * @param typeQName
     * @return
     */
    public static String getQueryName(NamespaceService namespaceService, QName typeQName)
    {
        return buildPrefixEncodedString(namespaceService, typeQName);
    }

    private static String buildPrefixEncodedString(NamespaceService namespaceService, QName qname)
    {
        StringBuilder builder = new StringBuilder(128);

        if (!qname.getNamespaceURI().equals(CMIS_MODEL_URI))
        {
            Collection<String> prefixes = namespaceService.getPrefixes(qname.getNamespaceURI());
            if (prefixes.size() == 0)
            {
                throw new NamespaceException("A namespace prefix is not registered for uri " + qname.getNamespaceURI());
            }
            String resolvedPrefix = prefixes.iterator().next();

            builder.append(resolvedPrefix);
            builder.append("_");
        }

        builder.append(qname.getLocalName());
        return builder.toString();
    }
    
    /**
     * Is this a valid CMIS type The type must be a core CMIS type or extend cm:content or cm:folder The alfresco types
     * cm:content and cm:folder are hidden by the CMIS types
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public static boolean isValidCmisType(DictionaryService dictionaryService, QName typeQName)
    {
        if (CMISMapping.isCmisCoreType(typeQName))
        {
            return true;
        }

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) || dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (typeQName.equals(ContentModel.TYPE_CONTENT))
            {
                return false;
            }
            else if (typeQName.equals(ContentModel.TYPE_FOLDER))
            {
                return false;
            }

            else
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Is an association valid in CMIS? It must be a non-child relationship and the source and target must both be valid
     * CMIS types.
     * 
     * @param dictionaryService
     * @param associationQName
     * @return
     */
    public static boolean isValidCmisAssociation(DictionaryService dictionaryService, QName associationQName)
    {
        AssociationDefinition associationDefinition = dictionaryService.getAssociation(associationQName);
        if (associationDefinition == null)
        {
            return false;
        }
        if (associationDefinition.isChild())
        {
            return false;
        }
        if (!isValidCmisType(dictionaryService, getCmisType(associationDefinition.getSourceClass().getName())))
        {
            return false;
        }
        if (!isValidCmisType(dictionaryService, getCmisType(associationDefinition.getTargetClass().getName())))
        {
            return false;
        }
        return true;
    }

    /**
     * Given an Alfresco model type map it to the appropriate type. Maps cm:folder and cm:content to the CMIS
     * definitions
     * 
     * @param typeQName
     * @return
     */
    public static QName getCmisType(QName typeQName)
    {
        QName mapped = alfrescoToCmisTypes.get(typeQName);
        if (mapped != null)
        {
            return mapped;
        }
        return typeQName;
    }

    /**
     * Get the root CMIS object (in the alfresco model) for any type.
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public static QName getCmisRootType(DictionaryService dictionaryService, QName typeQName)
    {
        if (isCmisCoreType(typeQName))
        {
            return typeQName;
        }
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return DOCUMENT_QNAME;
        }
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            return FOLDER_QNAME;
        }
        if (isValidCmisAssociation(dictionaryService, typeQName))
        {
            return RELATIONSHIP_QNAME;
        }
        throw new UnsupportedOperationException();

    }
    
    public static String getCmisPropertyName(NamespaceService namespaceService, QName propertyQName)
    {
        return buildPrefixEncodedString(namespaceService, propertyQName);
    }
    
    public static CMISPropertyType getPropertyType(DictionaryService dictionaryService, QName propertyQName)
    {
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(propertyQName);
        DataTypeDefinition dataTypeDefinition = propertyDefinition.getDataType();
        QName dQName = dataTypeDefinition.getName();
        if(propertyQName.getNamespaceURI().equals(CMIS_MODEL_URI) && dQName.equals(DataTypeDefinition.QNAME))
        {
            return CMISPropertyType.TYPE_ID;
        }
        return alfrescoPropertyTypesToCimsPropertyTypes.get(dQName);
        
    }
        
    public static QName getPropertyQName(DictionaryService dictionaryService, NamespaceService namespaceService, String cmisPropertyName)
    {
        // Try the cmis model first - it it matches we are done
        QName cmisPropertyQName = QName.createQName(CMIS_MODEL_URI, cmisPropertyName);
        if(dictionaryService.getProperty(cmisPropertyQName) != null)
        {
            return cmisPropertyQName;
        }
        
        int split = cmisPropertyName.indexOf('_');
        String prefix = cmisPropertyName.substring(0, split);
        String localName = cmisPropertyName.substring(split+1);
        
        QName qname = QName.createQName(prefix, localName, namespaceService);
        return qname;
        
    }
}
