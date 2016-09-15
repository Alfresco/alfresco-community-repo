/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISAccessControlFormatEnum;
import org.alfresco.opencmis.dictionary.QNameFilter;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.InitializingBean;

/**
 * CMIS <-> Alfresco mappings. It additionally excludes a list of QNames based
 * on a user defined list
 * 
 * @author andyh
 */
public class CMISMapping implements InitializingBean
{

    /**
     * The Alfresco CMIS Namespace
     */
    public static String CMIS_MODEL_NS = "cmis";
    public static String CMIS_MODEL_URI = "http://www.alfresco.org/model/cmis/1.0/cs01";

    public static String CMIS_EXT_NS = "cmisext";
    public static String CMIS_EXT_URI = "http://www.alfresco.org/model/cmis/1.0/cs01ext";

    /**
     * The Alfresco CMIS Model name.
     */
    public static String CMIS_MODEL_NAME = "cmismodel";

    /**
     * The QName for the Alfresco CMIS Model.
     */
    public static QName CMIS_MODEL_QNAME = QName.createQName(CMIS_MODEL_URI, CMIS_MODEL_NAME);

    // CMIS Data Types
    public static QName CMIS_DATATYPE_ID = QName.createQName(CMIS_MODEL_URI, "id");
    public static QName CMIS_DATATYPE_URI = QName.createQName(CMIS_MODEL_URI, "uri");
    public static QName CMIS_DATATYPE_XML = QName.createQName(CMIS_MODEL_URI, "xml");
    public static QName CMIS_DATATYPE_HTML = QName.createQName(CMIS_MODEL_URI, "html");

    // CMIS Types
    public static QName OBJECT_QNAME = QName.createQName(CMIS_EXT_URI, "object");
    public static QName DOCUMENT_QNAME = QName.createQName(CMIS_MODEL_URI, "document");
    public static QName FOLDER_QNAME = QName.createQName(CMIS_MODEL_URI, "folder");
    public static QName RELATIONSHIP_QNAME = QName.createQName(CMIS_MODEL_URI, "relationship");
    public static QName POLICY_QNAME = QName.createQName(CMIS_MODEL_URI, "policy");
    public static QName SECONDARY_TYPES_QNAME = QName.createQName(CMIS_MODEL_URI, "secondary"); // cmis 1.1
    public static QName ASPECTS_QNAME = QName.createQName(CMIS_EXT_URI, "aspects"); // cmis 1.0
    public static QName ITEM_QNAME = QName.createQName(CMIS_MODEL_URI, "item"); // cmis 1.1

    // CMIS Internal Type Ids
    public static String OBJECT_TYPE_ID = "cmisext:object";

    /**
     * Basic permissions.
     */
    public static final String CMIS_READ = "cmis:read";
    public static final String CMIS_WRITE = "cmis:write";
    public static final String CMIS_ALL = "cmis:all";

    // Service Dependencies
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

    // Mappings
    private Map<QName, String> mapAlfrescoQNameToTypeId = new HashMap<QName, String>();
    private Map<QName, QName> mapCmisQNameToAlfrescoQName = new HashMap<QName, QName>();
    private Map<QName, QName> mapAlfrescoQNameToCmisQName = new HashMap<QName, QName>();
    private Map<QName, PropertyType> mapAlfrescoToCmisDataType = new HashMap<QName, PropertyType>();
    private Map<PropertyType, QName> mapCmisDataTypeToAlfresco = new HashMap<PropertyType, QName>();
    
    private QNameFilter filter;

    private CmisVersion cmisVersion;
    
    public void setCmisVersion(CmisVersion cmisVersion)
    {
		this.cmisVersion = cmisVersion;
	}
    
    public CmisVersion getCmisVersion()
    {
		return cmisVersion;
	}

	/*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
    {
        //
        // Type Mappings
        //

        mapAlfrescoQNameToTypeId.put(OBJECT_QNAME, OBJECT_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(DOCUMENT_QNAME, BaseTypeId.CMIS_DOCUMENT.value());
        mapAlfrescoQNameToTypeId.put(FOLDER_QNAME, BaseTypeId.CMIS_FOLDER.value());
        mapAlfrescoQNameToTypeId.put(RELATIONSHIP_QNAME, BaseTypeId.CMIS_RELATIONSHIP.value());
        mapAlfrescoQNameToTypeId.put(SECONDARY_TYPES_QNAME, BaseTypeId.CMIS_SECONDARY.value());
        mapAlfrescoQNameToTypeId.put(ITEM_QNAME, BaseTypeId.CMIS_ITEM.value());
        mapAlfrescoQNameToTypeId.put(POLICY_QNAME, BaseTypeId.CMIS_POLICY.value());

        mapAlfrescoQNameToCmisQName.put(ContentModel.TYPE_CONTENT, DOCUMENT_QNAME);
        mapAlfrescoQNameToCmisQName.put(ContentModel.TYPE_FOLDER, FOLDER_QNAME);
        mapAlfrescoQNameToCmisQName.put(ContentModel.TYPE_BASE, ITEM_QNAME);

        mapCmisQNameToAlfrescoQName.put(DOCUMENT_QNAME, ContentModel.TYPE_CONTENT);
        mapCmisQNameToAlfrescoQName.put(FOLDER_QNAME, ContentModel.TYPE_FOLDER);
        mapCmisQNameToAlfrescoQName.put(ITEM_QNAME, ContentModel.TYPE_BASE);
        mapCmisQNameToAlfrescoQName.put(RELATIONSHIP_QNAME, null);
        mapCmisQNameToAlfrescoQName.put(POLICY_QNAME, null);

        //
        // Data Type Mappings
        //

        mapAlfrescoToCmisDataType.put(DataTypeDefinition.ANY, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.ENCRYPTED, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.ASSOC_REF, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.BOOLEAN, PropertyType.BOOLEAN);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CATEGORY, PropertyType.ID);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CHILD_ASSOC_REF, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CONTENT, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DATE, PropertyType.DATETIME);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DATETIME, PropertyType.DATETIME);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DOUBLE, PropertyType.DECIMAL);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.FLOAT, PropertyType.DECIMAL);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.INT, PropertyType.INTEGER);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.LOCALE, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.PERIOD, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.LONG, PropertyType.INTEGER);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.MLTEXT, PropertyType.STRING);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.NODE_REF, PropertyType.ID);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.PATH, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.QNAME, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.TEXT, PropertyType.STRING);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_ID, PropertyType.ID);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_URI, PropertyType.URI);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_HTML, PropertyType.HTML);

        mapCmisDataTypeToAlfresco.put(PropertyType.ID, DataTypeDefinition.TEXT);
        mapCmisDataTypeToAlfresco.put(PropertyType.INTEGER, DataTypeDefinition.LONG);
        mapCmisDataTypeToAlfresco.put(PropertyType.STRING, DataTypeDefinition.TEXT);
        mapCmisDataTypeToAlfresco.put(PropertyType.DECIMAL, DataTypeDefinition.DOUBLE);
        mapCmisDataTypeToAlfresco.put(PropertyType.BOOLEAN, DataTypeDefinition.BOOLEAN);
        mapCmisDataTypeToAlfresco.put(PropertyType.DATETIME, DataTypeDefinition.DATETIME);
        mapCmisDataTypeToAlfresco.put(PropertyType.URI, DataTypeDefinition.TEXT);
        mapCmisDataTypeToAlfresco.put(PropertyType.HTML, DataTypeDefinition.TEXT);
    }

    public void setFilter(QNameFilter filter)
    {
        this.filter = filter;
    }

    /**
     * @param dictionaryService dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param namespaceService service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @return namespaceService
     */
    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    /**
     * @return dictionaryService
     */
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /*
     * Is the type excluded from the CMIS dictionary and therefore not visible to clients?
     */
    public boolean isExcluded(QName typeQName)
    {
    	boolean isExcluded = false;

		// check for exclusion of the type and, if necessary, its parents
    	if(filter != null && typeQName != null)
    	{
    		isExcluded = filter.isExcluded(typeQName);
    		if(!isExcluded)
    		{

    			// check parent, if any
	    		AspectDefinition aspectDef = dictionaryService.getAspect(typeQName);
	    		QName parentType = null;
	    		if(aspectDef != null)
	    		{
	    			parentType = aspectDef.getParentName();
	    		}
	    		else
	    		{
	    	    	TypeDefinition typeDef = dictionaryService.getType(typeQName);
	    	    	if(typeDef != null)
	    	    	{
	    	    		parentType = typeDef.getParentName();
	    	    	}
		    		else
		    		{
		    			parentType = null;
		    		}
	    		}
	    		if(parentType != null)
	    		{
	    			isExcluded = isExcluded(parentType);
	    		}
    		}

        	filter.setExcluded(typeQName, Boolean.valueOf(isExcluded));
    	}

    	return isExcluded;
    }
    
    /**
     * Gets the CMIS Type Id given the Alfresco QName for the type in any
     * Alfresco model
     * 
     * @param scope BaseTypeId
     * @param typeQName QName
     * @return String
     */
    public String getCmisTypeId(BaseTypeId scope, QName typeQName)
    {
        String typeId = mapAlfrescoQNameToTypeId.get(typeQName);
        if (typeId == null)
        {
            String p = null;
            switch (scope)
            {
            case CMIS_DOCUMENT:
                p = "D";
                break;
            case CMIS_FOLDER:
                p = "F";
                break;
            case CMIS_RELATIONSHIP:
                p = "R";
                break;
            case CMIS_SECONDARY:
                p = "P";
                break;
            case CMIS_POLICY:
                p = "P";
                break;
            case CMIS_ITEM:
                p = "I";
                break;
            default:
                throw new CmisRuntimeException("Invalid base type!");
            }

            return p + ":" + typeQName.toPrefixString(namespaceService);
        } 
        else
        {
            return typeId;
        }
    }

    public String getCmisTypeId(QName classQName)
    {
        if (classQName.equals(ContentModel.TYPE_CONTENT))
        {
            return getCmisTypeId(BaseTypeId.CMIS_DOCUMENT, classQName);
        }
        if (classQName.equals(ContentModel.TYPE_FOLDER))
        {
            return getCmisTypeId(BaseTypeId.CMIS_FOLDER, classQName);
        }
        if (classQName.equals(CMISMapping.RELATIONSHIP_QNAME))
        {
            return getCmisTypeId(BaseTypeId.CMIS_RELATIONSHIP, classQName);
        }
        if (classQName.equals(CMISMapping.POLICY_QNAME))
        {
            return getCmisTypeId(BaseTypeId.CMIS_POLICY, classQName);
        }
        if (classQName.equals(CMISMapping.ASPECTS_QNAME))
        {
            return getCmisTypeId(BaseTypeId.CMIS_POLICY, classQName);
        }
        if (isValidCmisDocument(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_DOCUMENT, classQName);
        }
        if (isValidCmisFolder(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_FOLDER, classQName);
        }
        if (isValidCmisRelationship(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_RELATIONSHIP, classQName);
        }
        if (cmisVersion.equals(CmisVersion.CMIS_1_1) && isValidCmisSecondaryType(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_SECONDARY, classQName);
        }
        if (cmisVersion.equals(CmisVersion.CMIS_1_1) && isValidCmisItem(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_ITEM, classQName);
        }
        if (cmisVersion.equals(CmisVersion.CMIS_1_0) && isValidCmisPolicy(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_POLICY, classQName);
        }

        return null;
    }

    public String buildPrefixEncodedString(QName qname)
    {
        return qname.toPrefixString(namespaceService);
    }

    public QName getAlfrescoName(String typeId)
    {
        // Is it an Alfresco type id?
        if (typeId.length() < 4 || typeId.charAt(1) != ':')
        {
            throw new CmisInvalidArgumentException("Malformed type id '" + typeId + "'");
        }

        return QName.createQName(typeId.substring(2), namespaceService);
    }

    public boolean isValidCmisObject(BaseTypeId scope, QName qname)
    {
        switch (scope)
        {
        case CMIS_DOCUMENT:
            return isValidCmisDocument(qname);
        case CMIS_FOLDER:
            return isValidCmisFolder(qname);
        case CMIS_POLICY:
            return isValidCmisPolicy(qname);
        case CMIS_RELATIONSHIP:
            return isValidCmisRelationship(qname);
        case CMIS_SECONDARY:
            return isValidCmisSecondaryType(qname);
        case CMIS_ITEM:
            return isValidCmisItem(qname);
        }

        return false;
    }
    
    /**
     * Is this a valid CMIS folder type?
     * 
     * @param typeQName QName
     * @return boolean
     */
    public boolean isValidCmisFolder(QName typeQName)
    {
    	if(isExcluded(typeQName))
    	{
            return false;
        }
        
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
            } else
            {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Is this a valid CMIS document type?
     * 
     * @param typeQName QName
     * @return boolean
     */
    public boolean isValidCmisDocument(QName typeQName)
    {
    	if(isExcluded(typeQName))
    	{
            return false;
        }

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
            } else
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Is this a valid CMIS secondary type?
     * 
     * @param typeQName QName
     * @return boolean
     */
    public boolean isValidCmisSecondaryType(QName typeQName)
    {
    	if(isExcluded(typeQName))
    	{
            return false;
        }

        if (typeQName == null)
        {
            return false;
        }

        if (typeQName.equals(SECONDARY_TYPES_QNAME))
        {
            return true;
        }

        AspectDefinition aspectDef = dictionaryService.getAspect(typeQName);
        if (aspectDef == null)
        {
            return false;
        }

        // Anything derived from the aspects here would at some point have to linked up with an invalid parent so exclude these aspects 
        // AND any that are derived from them.
        if (       dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_VERSIONABLE)
                || dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_AUDITABLE)
                || dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_REFERENCEABLE))
        {
            return false;
        }
        return true;
    }

    /**
     * Is this a valid CMIS policy type?
     * 
     * @param typeQName QName
     * @return boolean
     */
    public boolean isValidCmisPolicy(QName typeQName)
    {        if (typeQName == null)
        {
            return false;
        }
        if (typeQName.equals(POLICY_QNAME))
        {
            return true;
        }
        
        if(cmisVersion.equals(CmisVersion.CMIS_1_0))
        {
        	if (typeQName.equals(ASPECTS_QNAME))
        	{
        		return true;
        	}

        	AspectDefinition aspectDef = dictionaryService.getAspect(typeQName);
        	if (aspectDef == null)
        	{
        		return false;
        	}

        	// Anything derived from the aspects here would at some point have to linked up with an invalid parent so exclude these aspects 
        	// AND any that are derived from them.
        	if (       dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_VERSIONABLE)
        			|| dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_AUDITABLE)
        			|| dictionaryService.isSubClass(aspectDef.getName(), ContentModel.ASPECT_REFERENCEABLE))
        	{
        		return false;
        	}
        	return true;
        }
        else
        {
        	return false;
        }
    }

    /**
     * Is an association valid in CMIS? It must be a non-child relationship and
     * the source and target must both be valid CMIS types.
     * 
     * @param associationQName QName
     * @return boolean
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
        AssociationDefinition associationDefinition = dictionaryService.getAssociation(
                associationQName);
        if (associationDefinition == null)
        {
            return false;
        }
        if (associationDefinition.isChild())
        {
            return false;
        }
        if(!isValidCmisRelationshipEndPoint(associationDefinition.getTargetClass().getName()))
        {
            return false;
        }
        if(!isValidCmisRelationshipEndPoint(associationDefinition.getSourceClass().getName()))
        {
            return false;
        }
        return true;
    }
    
    public boolean isValidCmisRelationshipEndPoint(QName typeQName)
    {
        if(dictionaryService.getClass(typeQName).isAspect())
        {
            return true;
        }
        
        if (typeQName.equals(FOLDER_QNAME))
        {
            return true;
        }

        
        if (typeQName.equals(DOCUMENT_QNAME))
        {
            return true;
        }

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_BASE))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Is this a valid CMIS item type?
     * 
     * @param typeQName QName
     * @return boolean
     */
    public boolean isValidCmisItem(QName typeQName)
    {
    	if(isExcluded(typeQName))
    	{
            return false;
        }

        if (typeQName == null)
        {
            return false;
        }
        
        if(typeQName.equals(ITEM_QNAME))
        {
        	return true;
        }
        
        if(typeQName.equals(ContentModel.TYPE_BASE))
        {
        	return false;
        }
        
        AspectDefinition aspectDef = dictionaryService.getAspect(typeQName);
        if (aspectDef != null)
        {
        	// aspects are not items - this stops warning from getType
            return false;
        }
        
        TypeDefinition typeDef = dictionaryService.getType(typeQName);
        if (typeDef == null)
        {
        	// type does not exist
            return false;
        }
        
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_BASE))
        {
        	if(dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        	{
        		return false;
        	}
        	if(dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        	{
        		return false;
        	}
        	
        	return true;
        }
        
        return false;
    }

    
    
    /**
     * Given an Alfresco model type map it to the appropriate type. Maps
     * cm:folder and cm:content to the CMIS definitions
     */
    public QName getCmisType(QName typeQName)
    {
        QName mapped = mapAlfrescoQNameToCmisQName.get(typeQName);
        if (mapped != null)
        {
            return mapped;
        }
        return typeQName;
    }

    /**
     * Is Alfresco Type mapped to an alternative CMIS Type?
     */
    public boolean isRemappedType(QName typeQName)
    {
        return mapAlfrescoQNameToCmisQName.containsKey(typeQName);
    }

    /**
     * Given a CMIS model type map it to the appropriate Alfresco type.
     * 
     * @param cmisTypeQName QName
     * @return QName
     */
    public QName getAlfrescoClass(QName cmisTypeQName)
    {
        QName mapped = mapCmisQNameToAlfrescoQName.get(cmisTypeQName);
        if (mapped != null)
        {
            return mapped;
        }
        return cmisTypeQName;
    }

    /**
     * Get the CMIS property type for a property
     * 
     * @param datatype DataTypeDefinition
     * @return PropertyType
     */
    public PropertyType getDataType(DataTypeDefinition datatype)
    {
        return getDataType(datatype.getName());
    }

    public PropertyType getDataType(QName dataType)
    {
        return mapAlfrescoToCmisDataType.get(dataType);
    }

    public QName getAlfrescoDataType(PropertyType propertyType)
    {
        return mapCmisDataTypeToAlfresco.get(propertyType);
    }

    /**
     * @param propertyQName QName
     * @return String
     */
    public String getCmisPropertyId(QName propertyQName)
    {
        return propertyQName.toPrefixString(namespaceService);
    }

    public Collection<Pair<String, Boolean>> getReportedPermissions(String permission, Set<String> permissions,
            boolean hasFull, boolean isDirect, CMISAccessControlFormatEnum format)
    {
        ArrayList<Pair<String, Boolean>> answer = new ArrayList<Pair<String, Boolean>>(20);
        // indirect

        if (hasFull)
        {
            answer.add(new Pair<String, Boolean>(CMIS_READ, false));
            answer.add(new Pair<String, Boolean>(CMIS_WRITE, false));
            answer.add(new Pair<String, Boolean>(CMIS_ALL, false));
        }

        for (String perm : permissions)
        {
            if (PermissionService.READ.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMIS_READ, false));
            } else if (PermissionService.WRITE.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMIS_WRITE, false));
            } else if (PermissionService.ALL_PERMISSIONS.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMIS_READ, false));
                answer.add(new Pair<String, Boolean>(CMIS_WRITE, false));
                answer.add(new Pair<String, Boolean>(CMIS_ALL, false));
            }

            if (hasFull)
            {
                answer.add(new Pair<String, Boolean>(CMIS_READ, false));
                answer.add(new Pair<String, Boolean>(CMIS_WRITE, false));
                answer.add(new Pair<String, Boolean>(CMIS_ALL, false));
            }
        }

        // permission

        if (format == CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS)
        {
            if (PermissionService.READ.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_READ, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            } else if (PermissionService.WRITE.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_WRITE, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            } else if (PermissionService.ALL_PERMISSIONS.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_ALL, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            } else
            {
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            }
        } else if (format == CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS)
        {
            if (PermissionService.READ.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_READ, isDirect));
            } else if (PermissionService.WRITE.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_WRITE, isDirect));
            } else if (PermissionService.ALL_PERMISSIONS.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMIS_ALL, isDirect));
            } else
            {
                // else nothing
            }
        }

        return answer;
    }

    /**
     * @param permission String
     * @return permission to set
     */
    public String getSetPermission(String permission)
    {
        if (permission.equals(CMIS_READ))
        {
            return PermissionService.READ;
        } else if (permission.equals(CMIS_WRITE))
        {
            return PermissionService.WRITE;
        } else if (permission.equals(CMIS_ALL))
        {
            return PermissionService.ALL_PERMISSIONS;
        } else
        {
            return permission;
        }
    }

}
