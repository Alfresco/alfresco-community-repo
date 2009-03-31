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

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
    public static String CMIS_MODEL_URI = "http://www.alfresco.org/model/cmis/0.5";

    /**
     * The Alfresco CMIS Model name.
     */
    public static String CMIS_MODEL_NAME = "cmismodel";

    /**
     * The QName for the Alfresco CMIS Model.
     */
    public static QName CMIS_MODEL_QNAME = QName.createQName(CMIS_MODEL_URI, CMIS_MODEL_NAME);

    // CMIS Internal Types
    public static String OBJECT_OBJECT_TYPE = "Object";
    public static String FILESYSTEM_OBJECT_TYPE ="FileSystemObject";
    
    // CMIS Data Types
    public static QName CMIS_DATATYPE_ID = QName.createQName(CMIS_MODEL_URI, "id");
    public static QName CMIS_DATATYPE_URI = QName.createQName(CMIS_MODEL_URI, "uri");
    public static QName CMIS_DATATYPE_XML = QName.createQName(CMIS_MODEL_URI, "xml");
    public static QName CMIS_DATATYPE_HTML = QName.createQName(CMIS_MODEL_URI, "html");

    // CMIS Types
    public static QName OBJECT_QNAME = QName.createQName(CMIS_MODEL_URI, OBJECT_OBJECT_TYPE);
    public static QName FILESYSTEM_OBJECT_QNAME = QName.createQName(CMIS_MODEL_URI, FILESYSTEM_OBJECT_TYPE);
    public static QName DOCUMENT_QNAME = QName.createQName(CMIS_MODEL_URI, CMISDictionaryModel.DOCUMENT_OBJECT_TYPE);
    public static QName FOLDER_QNAME = QName.createQName(CMIS_MODEL_URI, CMISDictionaryModel.FOLDER_OBJECT_TYPE);
    public static QName RELATIONSHIP_QNAME = QName.createQName(CMIS_MODEL_URI, CMISDictionaryModel.RELATIONSHIP_OBJECT_TYPE);
    public static QName POLICY_QNAME = QName.createQName(CMIS_MODEL_URI, CMISDictionaryModel.POLICY_OBJECT_TYPE);

    // CMIS Internal Type Ids
    public static CMISTypeId OBJECT_TYPE_ID = new CMISTypeId(CMISScope.OBJECT, OBJECT_OBJECT_TYPE.toLowerCase(), OBJECT_QNAME);
    public static CMISTypeId FILESYSTEM_OBJECT_TYPE_ID = new CMISTypeId(CMISScope.OBJECT, FILESYSTEM_OBJECT_TYPE.toLowerCase(), FILESYSTEM_OBJECT_QNAME);

    // Properties
    public static QName PROP_OBJECT_ID_QNAME = QName.createQName(CMIS_MODEL_URI, CMISDictionaryModel.PROP_OBJECT_ID);

    // Mappings
    // - no entry means no mapping and pass through as is

    private static HashMap<QName, CMISTypeId> qNameToCmisTypeId = new HashMap<QName, CMISTypeId>();
    private static HashMap<QName, QName> cmisToAlfrecsoTypes = new HashMap<QName, QName>();
    private static HashMap<QName, QName> alfrescoToCmisTypes = new HashMap<QName, QName>();
    private static HashMap<QName, CMISDataTypeEnum> alfrescoPropertyTypesToCmisPropertyTypes = new HashMap<QName, CMISDataTypeEnum>();

    
    /**
     * Set up mappings
     */
    static
    {
        qNameToCmisTypeId.put(OBJECT_QNAME, OBJECT_TYPE_ID);
        qNameToCmisTypeId.put(FILESYSTEM_OBJECT_QNAME, FILESYSTEM_OBJECT_TYPE_ID);
        qNameToCmisTypeId.put(DOCUMENT_QNAME, CMISDictionaryModel.DOCUMENT_TYPE_ID);
        qNameToCmisTypeId.put(FOLDER_QNAME, CMISDictionaryModel.FOLDER_TYPE_ID);
        qNameToCmisTypeId.put(RELATIONSHIP_QNAME, CMISDictionaryModel.RELATIONSHIP_TYPE_ID);
        qNameToCmisTypeId.put(POLICY_QNAME, CMISDictionaryModel.POLICY_TYPE_ID);

        cmisToAlfrecsoTypes.put(DOCUMENT_QNAME, ContentModel.TYPE_CONTENT);
        cmisToAlfrecsoTypes.put(FOLDER_QNAME, ContentModel.TYPE_FOLDER);
        cmisToAlfrecsoTypes.put(RELATIONSHIP_QNAME, null);
        cmisToAlfrecsoTypes.put(POLICY_QNAME, null);

        alfrescoToCmisTypes.put(ContentModel.TYPE_CONTENT, DOCUMENT_QNAME);
        alfrescoToCmisTypes.put(ContentModel.TYPE_FOLDER, FOLDER_QNAME);

        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.ANY, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.ASSOC_REF, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.BOOLEAN, CMISDataTypeEnum.BOOLEAN);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.CATEGORY, CMISDataTypeEnum.ID);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.CHILD_ASSOC_REF, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.CONTENT, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.DATE, CMISDataTypeEnum.DATETIME);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.DATETIME, CMISDataTypeEnum.DATETIME);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.DOUBLE, CMISDataTypeEnum.DECIMAL);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.FLOAT, CMISDataTypeEnum.DECIMAL);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.INT, CMISDataTypeEnum.INTEGER);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.LOCALE, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.LONG, CMISDataTypeEnum.INTEGER);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.MLTEXT, CMISDataTypeEnum.STRING);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.NODE_REF, CMISDataTypeEnum.ID);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.PATH, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.QNAME, null);
        alfrescoPropertyTypesToCmisPropertyTypes.put(DataTypeDefinition.TEXT, CMISDataTypeEnum.STRING);
        alfrescoPropertyTypesToCmisPropertyTypes.put(CMIS_DATATYPE_ID, CMISDataTypeEnum.ID);
        alfrescoPropertyTypesToCmisPropertyTypes.put(CMIS_DATATYPE_URI, CMISDataTypeEnum.URI);
        alfrescoPropertyTypesToCmisPropertyTypes.put(CMIS_DATATYPE_XML, CMISDataTypeEnum.XML);
        alfrescoPropertyTypesToCmisPropertyTypes.put(CMIS_DATATYPE_HTML, CMISDataTypeEnum.HTML);
    }

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

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
    /* package */DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
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
     * Gets the namespace service
     * 
     * @return namespaceService
     */
    /*package*/ NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }

    /**
     * Gets the CMIS Type Id given the serialized type Id
     * 
     * @param typeId  type id in the form of <ROOT_TYPE_ID>/<PREFIX>_<LOCALNAME>
     * @return
     */
    public CMISTypeId getCmisTypeId(String typeId)
    {
        // Is it a CMIS root object type id?
        if (typeId.equalsIgnoreCase(CMISDictionaryModel.DOCUMENT_TYPE_ID.getId()))
        {
            return CMISDictionaryModel.DOCUMENT_TYPE_ID;
        }
        else if (typeId.equalsIgnoreCase(CMISDictionaryModel.FOLDER_TYPE_ID.getId()))
        {
            return CMISDictionaryModel.FOLDER_TYPE_ID;
        }
        else if (typeId.equalsIgnoreCase(CMISDictionaryModel.RELATIONSHIP_TYPE_ID.getId()))
        {
            return CMISDictionaryModel.RELATIONSHIP_TYPE_ID;
        }
        else if (typeId.equalsIgnoreCase(CMISDictionaryModel.POLICY_TYPE_ID.getId()))
        {
            return CMISDictionaryModel.POLICY_TYPE_ID;
        }
        else if (typeId.equalsIgnoreCase(OBJECT_TYPE_ID.getId()))
        {
            return OBJECT_TYPE_ID;
        }
        else if (typeId.equalsIgnoreCase(FILESYSTEM_OBJECT_TYPE_ID.getId()))
        {
            return FILESYSTEM_OBJECT_TYPE_ID;
        }

        // Is it an Alfresco type id?
        if (typeId.length() < 4 || typeId.charAt(1) != '/')
        {
            throw new AlfrescoRuntimeException("Malformed type id '" + typeId + "'");
        }

        // Alfresco type id
        CMISScope scope = CMISScope.toScope(typeId.charAt(0));
        if (scope == null)
        {
            throw new AlfrescoRuntimeException("Malformed type id '" + typeId + "'; discriminator " + typeId.charAt(0) + " unknown");
        }
        QName typeQName = QName.resolveToQName(namespaceService, typeId.substring(2).replace('_', ':'));

        // Construct CMIS Type Id
        return new CMISTypeId(scope, typeId, typeQName);
    }

    /**
     * Gets the CMIS Type Id given the Alfresco QName for the type in any Alfresco model
     * 
     * @param typeQName
     * @return
     */
    public CMISTypeId getCmisTypeId(CMISScope scope, QName typeQName)
    {
        CMISTypeId typeId = qNameToCmisTypeId.get(typeQName);
        if (typeId == null)
        {
            StringBuilder builder = new StringBuilder(128);
            builder.append(scope.discriminator());
            builder.append("/");
            builder.append(buildPrefixEncodedString(typeQName, false));
            return new CMISTypeId(scope, builder.toString(), typeQName);
        }
        else
        {
            return typeId;
        }
    }

    public CMISTypeId getCmisTypeId(QName classQName)
    {
        if (classQName.equals(ContentModel.TYPE_CONTENT))
        {
            return getCmisTypeId(CMISScope.DOCUMENT, classQName);
        }
        if (classQName.equals(ContentModel.TYPE_FOLDER))
        {
            return getCmisTypeId(CMISScope.FOLDER, classQName);
        }
        if (classQName.equals(CMISMapping.RELATIONSHIP_QNAME))
        {
            return getCmisTypeId(CMISScope.RELATIONSHIP, classQName);
        }
        if (classQName.equals(CMISMapping.POLICY_QNAME))
        {
            return getCmisTypeId(CMISScope.POLICY, classQName);
        }
        if (classQName.equals(CMISMapping.OBJECT_QNAME))
        {
            return getCmisTypeId(CMISScope.OBJECT, classQName);
        }
        if (classQName.equals(CMISMapping.FILESYSTEM_OBJECT_QNAME))
        {
            return getCmisTypeId(CMISScope.OBJECT, classQName);
        }
        if (isValidCmisDocument(classQName))
        {
            return getCmisTypeId(CMISScope.DOCUMENT, classQName);
        }
        if (isValidCmisFolder(classQName))
        {
            return getCmisTypeId(CMISScope.FOLDER, classQName);
        }
        if (isValidCmisRelationship(classQName))
        {
            return getCmisTypeId(CMISScope.RELATIONSHIP, classQName);
        }
        if (isValidCmisPolicy(classQName))
        {
            return getCmisTypeId(CMISScope.POLICY, classQName);
        }

        return null;
    }

    /*package*/ String buildPrefixEncodedString(QName qname, boolean upperCase)
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

            builder.append(upperCase ? resolvedPrefix.toUpperCase() : resolvedPrefix);
            builder.append("_");
        }

        builder.append(upperCase ? qname.getLocalName().toUpperCase() : qname.getLocalName());
        return builder.toString();
    }

    /**
     * Is this a valid cmis document or folder type (not a relationship)
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public boolean isValidCmisDocumentOrFolder(QName typeQName)
    {
        return isValidCmisFolder(typeQName) || isValidCmisDocument(typeQName);
    }

    /**
     * Is this a valid CMIS folder type?
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public boolean isValidCmisFolder(QName typeQName)
    {
        if (typeQName == null)
        {
            return false;
        }
        if (typeQName.equals(FOLDER_QNAME))
        {
            return true;
        }

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (typeQName.equals(ContentModel.TYPE_FOLDER))
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
     * Is this a valid CMIS document type?
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public boolean isValidCmisDocument(QName typeQName)
    {
        if (typeQName == null)
        {
            return false;
        }
        if (typeQName.equals(DOCUMENT_QNAME))
        {
            return true;
        }

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            if (typeQName.equals(ContentModel.TYPE_CONTENT))
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
     * Is this a valid CMIS policy type?
     * 
     * @param dictionaryService
     * @param typeQName
     * @return
     */
    public boolean isValidCmisPolicy(QName typeQName)
    {
        if (typeQName == null)
        {
            return false;
        }
        if (typeQName.equals(POLICY_QNAME))
        {
            return true;
        }

        AspectDefinition aspectDef = dictionaryService.getAspect(typeQName);
        if (aspectDef == null)
        {
            return false;
        }
        
        if (aspectDef.getName().equals(ContentModel.ASPECT_VERSIONABLE) ||
            aspectDef.getName().equals(ContentModel.ASPECT_AUDITABLE) ||
            aspectDef.getName().equals(ContentModel.ASPECT_REFERENCEABLE))
        {
            return false;
        }
        return true;
    }
    
    /**
     * Is an association valid in CMIS? It must be a non-child relationship and the source and target must both be valid
     * CMIS types.
     * 
     * @param dictionaryService
     * @param associationQName
     * @return
     */
    public boolean isValidCmisRelationship(QName associationQName)
    {
        if (associationQName == null)
        {
            return false;
        }
        if (associationQName.equals(RELATIONSHIP_QNAME))
        {
            return true;
        }
        AssociationDefinition associationDefinition = dictionaryService.getAssociation(associationQName);
        if (associationDefinition == null)
        {
            return false;
        }
        if (associationDefinition.isChild())
        {
            return false;
        }
        if (!isValidCmisDocumentOrFolder(getCmisType(associationDefinition.getSourceClass().getName())))
        {
            return false;
        }
        if (!isValidCmisDocumentOrFolder(getCmisType(associationDefinition.getTargetClass().getName())))
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
    public QName getCmisType(QName typeQName)
    {
        QName mapped = alfrescoToCmisTypes.get(typeQName);
        if (mapped != null)
        {
            return mapped;
        }
        return typeQName;
    }

    /**
     * Is Alfresco Type mapped to an alternative CMIS Type?
     * 
     * @param typeQName
     * @return
     */
    public boolean isRemappedType(QName typeQName)
    {
        return alfrescoToCmisTypes.containsKey(typeQName);
    }
    
    /**
     * Given a CMIS model type map it to the appropriate Alfresco type.
     * 
     * @param cmisTypeQName
     * @return
     */
    public QName getAlfrescoType(QName cmisTypeQName)
    {
        QName mapped = cmisToAlfrecsoTypes.get(cmisTypeQName);
        if (mapped != null)
        {
            return mapped;
        }
        return cmisTypeQName;
    }

    /**
     * Get the CMIS property name from the property QName.
     * 
     * @param namespaceService
     * @param propertyQName
     * @return
     */
    public String getCmisPropertyName(QName propertyQName)
    {
        return buildPrefixEncodedString(propertyQName, false);
    }

    /**
     * Get the CMIS property type for a property
     * 
     * @param dictionaryService
     * @param propertyQName
     * @return
     */
    public CMISDataTypeEnum getDataType(DataTypeDefinition datatype)
    {
        return getDataType(datatype.getName());
    }
    
    public CMISDataTypeEnum getDataType(QName dataType)
    {
        return alfrescoPropertyTypesToCmisPropertyTypes.get(dataType);
    }

    /**
     * Lookup a CMIS property name and get the Alfresco property QName
     * 
     * @param dictionaryService
     * @param namespaceService
     * @param cmisPropertyName
     * @return
     */
    public QName getPropertyQName(String cmisPropertyName)
    {
        // Try the cmis model first - it it matches we are done
        QName cmisPropertyQName = QName.createQName(CMIS_MODEL_URI, cmisPropertyName);
        if (dictionaryService.getProperty(cmisPropertyQName) != null)
        {
            return cmisPropertyQName;
        }

        // Find prefix and property name - in upper case

        int split = cmisPropertyName.indexOf('_');

        // CMIS case insensitive hunt - no prefix
        if (split == -1)
        {
            for (QName qname : dictionaryService.getAllProperties(null))
            {
                if (qname.getNamespaceURI().equals(CMIS_MODEL_URI))
                {
                    if (qname.getLocalName().equalsIgnoreCase(cmisPropertyName))
                    {
                        return qname;
                    }
                }
            }
            return null;
        }

        String prefix = cmisPropertyName.substring(0, split);
        String localName = cmisPropertyName.substring(split + 1);

        // Try lower case version first.

        QName propertyQName = QName.createQName(prefix.toLowerCase(), localName.toLowerCase(), namespaceService);
        if (dictionaryService.getProperty(propertyQName) != null)
        {
            return propertyQName;
        }

        // Full case insensitive hunt

        for (String test : namespaceService.getPrefixes())
        {
            if (test.equalsIgnoreCase(prefix))
            {
                prefix = test;
                break;
            }
        }
        String uri = namespaceService.getNamespaceURI(prefix);

        for (QName qname : dictionaryService.getAllProperties(null))
        {
            if (qname.getNamespaceURI().equals(uri))
            {
                if (qname.getLocalName().equalsIgnoreCase(localName))
                {
                    return qname;
                }
            }
        }

        return null;
    }

    /**
     * @param namespaceService
     * @param propertyQName
     * @return
     */
    public String getCmisPropertyId(QName propertyQName)
    {
        if (propertyQName.getNamespaceURI().equals(CMIS_MODEL_URI))
        {
            return propertyQName.getLocalName();
        }
        else
        {
            return propertyQName.toString();
        }
    }
}
