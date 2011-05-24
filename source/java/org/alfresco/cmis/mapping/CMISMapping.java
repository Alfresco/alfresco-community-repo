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
package org.alfresco.cmis.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISActionEvaluator;
import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISPropertyId;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;


/**
 * CMIS <-> Alfresco mappings
 * 
 * @author andyh
 */
public class CMISMapping implements InitializingBean
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISMapping.class);
    
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
    public static QName FILESYSTEM_OBJECT_QNAME = QName.createQName(CMIS_MODEL_URI, "filesystemobject");
    public static QName DOCUMENT_QNAME = QName.createQName(CMIS_MODEL_URI, "document");
    public static QName FOLDER_QNAME = QName.createQName(CMIS_MODEL_URI, "folder");
    public static QName RELATIONSHIP_QNAME = QName.createQName(CMIS_MODEL_URI, "relationship");
    public static QName POLICY_QNAME = QName.createQName(CMIS_MODEL_URI, "policy");

    // CMIS Internal Type Ids
    public static CMISTypeId OBJECT_TYPE_ID = new CMISTypeId(CMISScope.OBJECT, OBJECT_QNAME, CMIS_MODEL_NS + ":" + OBJECT_QNAME.getLocalName(), OBJECT_QNAME);
    public static CMISTypeId FILESYSTEM_OBJECT_TYPE_ID = new CMISTypeId(CMISScope.OBJECT, FILESYSTEM_OBJECT_QNAME, CMIS_MODEL_NS + ":" + FILESYSTEM_OBJECT_QNAME.getLocalName(), FILESYSTEM_OBJECT_QNAME);

    // Service Dependencies
    private ServiceRegistry serviceRegistry;
    private CMISServices cmisService;

    // Mappings
    private Map<QName, CMISTypeId> mapAlfrescoQNameToTypeId = new HashMap<QName, CMISTypeId>();
    private Map<QName, QName> mapCmisQNameToAlfrescoQName = new HashMap<QName, QName>();
    private Map<QName, QName> mapAlfrescoQNameToCmisQName = new HashMap<QName, QName>();
    private Map<QName, CMISDataTypeEnum> mapAlfrescoToCmisDataType = new HashMap<QName, CMISDataTypeEnum>();
    private Map<String, AbstractProperty> propertyAccessors = new HashMap<String, AbstractProperty>();
    private Map<CMISScope, Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>>> actionEvaluators = new HashMap<CMISScope, Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>>>();
    
    
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        //
        // Type Mappings
        //
        
        mapAlfrescoQNameToTypeId.put(OBJECT_QNAME, OBJECT_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(FILESYSTEM_OBJECT_QNAME, FILESYSTEM_OBJECT_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(DOCUMENT_QNAME, CMISDictionaryModel.DOCUMENT_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(FOLDER_QNAME, CMISDictionaryModel.FOLDER_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(RELATIONSHIP_QNAME, CMISDictionaryModel.RELATIONSHIP_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(POLICY_QNAME, CMISDictionaryModel.POLICY_TYPE_ID);

        mapAlfrescoQNameToCmisQName.put(ContentModel.TYPE_CONTENT, DOCUMENT_QNAME);
        mapAlfrescoQNameToCmisQName.put(ContentModel.TYPE_FOLDER, FOLDER_QNAME);

        mapCmisQNameToAlfrescoQName.put(DOCUMENT_QNAME, ContentModel.TYPE_CONTENT);
        mapCmisQNameToAlfrescoQName.put(FOLDER_QNAME, ContentModel.TYPE_FOLDER);
        mapCmisQNameToAlfrescoQName.put(RELATIONSHIP_QNAME, null);
        mapCmisQNameToAlfrescoQName.put(POLICY_QNAME, null);

        //
        // Data Type Mappings
        //
        
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.ANY, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.ASSOC_REF, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.BOOLEAN, CMISDataTypeEnum.BOOLEAN);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CATEGORY, CMISDataTypeEnum.ID);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CHILD_ASSOC_REF, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.CONTENT, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DATE, CMISDataTypeEnum.DATETIME);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DATETIME, CMISDataTypeEnum.DATETIME);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.DOUBLE, CMISDataTypeEnum.DECIMAL);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.FLOAT, CMISDataTypeEnum.DECIMAL);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.INT, CMISDataTypeEnum.INTEGER);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.LOCALE, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.PERIOD, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.LONG, CMISDataTypeEnum.INTEGER);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.MLTEXT, CMISDataTypeEnum.STRING);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.NODE_REF, CMISDataTypeEnum.ID);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.PATH, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.QNAME, null);
        mapAlfrescoToCmisDataType.put(DataTypeDefinition.TEXT, CMISDataTypeEnum.STRING);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_ID, CMISDataTypeEnum.ID);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_URI, CMISDataTypeEnum.URI);
        mapAlfrescoToCmisDataType.put(CMIS_DATATYPE_HTML, CMISDataTypeEnum.HTML);

        //
        // Property Mappings
        //
        
        registerPropertyAccessor(new ObjectIdProperty(serviceRegistry));
        registerPropertyAccessor(new ObjectTypeIdProperty(serviceRegistry));
        registerPropertyAccessor(new BaseTypeIdProperty(serviceRegistry));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_CREATED_BY, ContentModel.PROP_CREATOR));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_CREATION_DATE, ContentModel.PROP_CREATED));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_LAST_MODIFIED_BY, ContentModel.PROP_MODIFIER));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE, ContentModel.PROP_MODIFIED));
        registerPropertyAccessor(new FixedValueProperty(serviceRegistry, CMISDictionaryModel.PROP_CHANGE_TOKEN, null));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_NAME,
                ContentModel.PROP_NAME)
        {
            @Override
            public Serializable getValue(AssociationRef assocRef)
            {
                // Let's use the association ref as the name
                return assocRef.toString();
            }
        });
        registerPropertyAccessor(new IsImmutableProperty(serviceRegistry));
        registerPropertyAccessor(new IsLatestVersionProperty(serviceRegistry));
        registerPropertyAccessor(new IsMajorVersionProperty(serviceRegistry));
        registerPropertyAccessor(new IsLatestMajorVersionProperty(serviceRegistry));
        registerPropertyAccessor(new VersionLabelProperty(serviceRegistry));
        registerPropertyAccessor(new VersionSeriesIdProperty(serviceRegistry));
        registerPropertyAccessor(new IsVersionSeriesCheckedOutProperty(serviceRegistry));
        registerPropertyAccessor(new VersionSeriesCheckedOutByProperty(serviceRegistry));
        registerPropertyAccessor(new VersionSeriesCheckedOutIdProperty(serviceRegistry));
        registerPropertyAccessor(new CheckinCommentProperty(serviceRegistry));
        registerPropertyAccessor(new ContentStreamLengthProperty(serviceRegistry));
        registerPropertyAccessor(new ContentStreamMimetypeProperty(serviceRegistry));
        registerPropertyAccessor(new ContentStreamIdProperty(serviceRegistry));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME, ContentModel.PROP_NAME));
        registerPropertyAccessor(new ParentProperty(serviceRegistry));
        registerPropertyAccessor(new PathProperty(serviceRegistry, cmisService));
        registerPropertyAccessor(new FixedValueProperty(serviceRegistry, CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS, null));
        registerPropertyAccessor(new SourceIdProperty(serviceRegistry));
        registerPropertyAccessor(new TargetIdProperty(serviceRegistry));
        
        //
        // Action Evaluator Mappings
        //
        
        // NOTE: The order of evaluators is important - they must be in the order as specified in CMIS-Core.xsd
        //       so that schema validation passes
        
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_UPDATE_PROPERTIES, PermissionService.WRITE_PROPERTIES));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_PROPERTIES, PermissionService.READ_PROPERTIES));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_RELATIONSHIPS, true));
        registerEvaluator(CMISScope.DOCUMENT, new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_PARENTS, PermissionService.READ_PERMISSIONS)));
        // Is CAN_MOVE correct mapping?
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_MOVE_OBJECT, PermissionService.DELETE_NODE));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_CONTENT_STREAM, PermissionService.WRITE_PROPERTIES, PermissionService.WRITE_CONTENT));
        registerEvaluator(CMISScope.DOCUMENT, new CanCheckOutActionEvaluator(serviceRegistry));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CANCEL_CHECKOUT, PermissionService.CANCEL_CHECK_OUT));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CHECKIN, PermissionService.CHECK_IN));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_SET_CONTENT_STREAM, PermissionService.WRITE_CONTENT));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_ALL_VERSIONS, true));
        registerEvaluator(CMISScope.DOCUMENT, new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_ADD_OBJECT_TO_FOLDER, PermissionService.LINK_CHILDREN)));
        // Is CAN_REMOVE_FROM_FOLDER correct mapping?
        registerEvaluator(CMISScope.DOCUMENT, new ParentActionEvaluator(new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_REMOVE_OBJECT_FROM_FOLDER, true)));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_CONTENT_STREAM, PermissionService.READ_CONTENT));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_POLICY, false));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_APPLIED_POLICIES, true));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_REMOVE_POLICY, false));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_CREATE_RELATIONSHIP, true));
        registerEvaluator(CMISScope.DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_RENDITIONS, true));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_ACL, PermissionService.READ_PERMISSIONS));
        registerEvaluator(CMISScope.DOCUMENT, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_ACL, PermissionService.CHANGE_PERMISSIONS));
        
        registerEvaluator(CMISScope.FOLDER, new RootActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE), false));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_UPDATE_PROPERTIES, PermissionService.WRITE_PROPERTIES));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_FOLDER_TREE, PermissionService.READ_CHILDREN));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_PROPERTIES, PermissionService.READ_PROPERTIES));
        registerEvaluator(CMISScope.FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_RELATIONSHIPS, true));
        registerEvaluator(CMISScope.FOLDER, new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_PARENTS, PermissionService.READ_PERMISSIONS)));
        registerEvaluator(CMISScope.FOLDER, new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_FOLDER_PARENT, PermissionService.READ_PERMISSIONS)));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_DESCENDANTS, PermissionService.READ_CHILDREN));
        // Is CAN_MOVE_OBJECT correct mapping?
        registerEvaluator(CMISScope.FOLDER, new RootActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_MOVE_OBJECT, PermissionService.DELETE_NODE), false));
        registerEvaluator(CMISScope.FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_POLICY, false));
        registerEvaluator(CMISScope.FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_APPLIED_POLICIES, true));
        registerEvaluator(CMISScope.FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_REMOVE_POLICY, false));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_CHILDREN, PermissionService.READ_CHILDREN));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CREATE_DOCUMENT, PermissionService.CREATE_CHILDREN));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CREATE_FOLDER, PermissionService.CREATE_CHILDREN));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_CREATE_RELATIONSHIP, PermissionService.CREATE_ASSOCIATIONS));
        registerEvaluator(CMISScope.FOLDER, new RootActionEvaluator(new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_TREE, PermissionService.DELETE_NODE), false));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_GET_ACL, PermissionService.READ_PERMISSIONS));
        registerEvaluator(CMISScope.FOLDER, new PermissionActionEvaluator(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_ACL, PermissionService.CHANGE_PERMISSIONS));

        registerEvaluator(CMISScope.RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_OBJECT, true));
        registerEvaluator(CMISScope.RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry, CMISAllowedActionEnum.CAN_UPDATE_PROPERTIES, false));
        registerEvaluator(CMISScope.RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_PROPERTIES, true));
        registerEvaluator(CMISScope.RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_ACL, false));
        registerEvaluator(CMISScope.RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_ACL, false));

        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_DELETE_OBJECT, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_UPDATE_PROPERTIES, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_PROPERTIES, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_PARENTS, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_MOVE_OBJECT, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_ADD_OBJECT_TO_FOLDER, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_REMOVE_OBJECT_FROM_FOLDER, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_OBJECT_RELATIONSHIPS, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_GET_ACL, false));
        registerEvaluator(CMISScope.POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry, CMISAllowedActionEnum.CAN_APPLY_ACL, false));
        
    }

    
    /**
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCMISService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }
    
    /**
     * @return namespaceService
     */
    /*package*/ NamespaceService getNamespaceService()
    {
        return serviceRegistry.getNamespaceService();
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
        if (typeId.length() < 4 || typeId.charAt(1) != ':')
        {
            throw new AlfrescoRuntimeException("Malformed type id '" + typeId + "'");
        }

        // Alfresco type id
        CMISScope scope = (CMISScope)CMISScope.FACTORY.fromLabel(typeId.substring(0, 1));
        if (scope == null)
        {
            throw new AlfrescoRuntimeException("Malformed type id '" + typeId + "'; discriminator " + typeId.charAt(0) + " unknown");
        }
        QName typeQName = QName.createQName(typeId.substring(2), serviceRegistry.getNamespaceService());

        // Construct CMIS Type Id
        return new CMISTypeId(scope, typeQName, typeId, typeQName);
    }

    /**
     * Gets the CMIS Type Id given the Alfresco QName for the type in any Alfresco model
     * 
     * @param typeQName
     * @return
     */
    public CMISTypeId getCmisTypeId(CMISScope scope, QName typeQName)
    {
        CMISTypeId typeId = mapAlfrescoQNameToTypeId.get(typeQName);
        if (typeId == null)
        {
            String typeIdStr = scope.getLabel() + ":" + typeQName.toPrefixString(serviceRegistry.getNamespaceService());
            return new CMISTypeId(scope, typeQName, typeIdStr, typeQName);
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
        if (isValidCmisPolicy(classQName))
        {
            return getCmisTypeId(CMISScope.POLICY, classQName);
        }
        if (isValidCmisRelationship(classQName))
        {
            return getCmisTypeId(CMISScope.RELATIONSHIP, classQName);
        }

        return null;
    }

    public String buildPrefixEncodedString(QName qname)
    {
        return qname.toPrefixString(serviceRegistry.getNamespaceService());
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

        if (serviceRegistry.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_FOLDER))
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

        if (serviceRegistry.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_CONTENT))
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

        AspectDefinition aspectDef = serviceRegistry.getDictionaryService().getAspect(typeQName);
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
        AssociationDefinition associationDefinition = serviceRegistry.getDictionaryService().getAssociation(associationQName);
        if (associationDefinition == null)
        {
            return false;
        }
        if (associationDefinition.isChild())
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
        QName mapped = mapAlfrescoQNameToCmisQName.get(typeQName);
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
        return mapAlfrescoQNameToCmisQName.containsKey(typeQName);
    }
    
    /**
     * Given a CMIS model type map it to the appropriate Alfresco type.
     * 
     * @param cmisTypeQName
     * @return
     */
    public QName getAlfrescoType(QName cmisTypeQName)
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
        return mapAlfrescoToCmisDataType.get(dataType);
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
        if (serviceRegistry.getDictionaryService().getProperty(cmisPropertyQName) != null)
        {
            return cmisPropertyQName;
        }

        // Find prefix and property name - in upper case

        int split = cmisPropertyName.indexOf('_');

        // CMIS case insensitive hunt - no prefix
        if (split == -1)
        {
            for (QName qname : serviceRegistry.getDictionaryService().getAllProperties(null))
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

        QName propertyQName = QName.createQName(prefix.toLowerCase(), localName.toLowerCase(), serviceRegistry.getNamespaceService());
        if (serviceRegistry.getDictionaryService().getProperty(propertyQName) != null)
        {
            return propertyQName;
        }

        // Full case insensitive hunt

        for (String test : serviceRegistry.getNamespaceService().getPrefixes())
        {
            if (test.equalsIgnoreCase(prefix))
            {
                prefix = test;
                break;
            }
        }
        String uri = serviceRegistry.getNamespaceService().getNamespaceURI(prefix);

        for (QName qname : serviceRegistry.getDictionaryService().getAllProperties(null))
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
        return propertyQName.toPrefixString(serviceRegistry.getNamespaceService());
    }

    /**
     * Get a Property Accessor
     * 
     * @param propertyId
     * @return
     */
    public AbstractProperty getPropertyAccessor(CMISPropertyId propertyId)
    {
        AbstractProperty propertyAccessor = propertyAccessors.get(propertyId.getId());
        if (propertyAccessor == null)
        {
            QName propertyQName = propertyId.getQName();
            if (propertyQName == null)
            {
                throw new AlfrescoRuntimeException("Can't get property accessor for property id " + propertyId.getId() + " due to unknown property QName");
            }
            propertyAccessor = new DirectProperty(serviceRegistry, propertyId.getId(), propertyQName);
        }
        return propertyAccessor;
    }
 
    /**
     * Register pre-defined Property Accessor
     * 
     * @param propertyAccessor
     */
    private void registerPropertyAccessor(AbstractProperty propertyAccessor)
    {
        propertyAccessors.put(propertyAccessor.getName(), propertyAccessor);
    }
    
    /**
     * Gets the Action Evaluators applicable for the given CMIS Scope
     * 
     * @param cmisScope
     * @return
     */
    public Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> getActionEvaluators(CMISScope scope)
    {
        Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> evaluators = actionEvaluators.get(scope);
        if (evaluators == null)
        {
            evaluators = Collections.emptyMap();
        }
        return evaluators;
    }

    /**
     * Register an Action Evaluator
     * 
     * @param scope
     * @param evaluator
     */
    private void registerEvaluator(CMISScope scope, CMISActionEvaluator<? extends Object> evaluator)
    {
        Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> evaluators = actionEvaluators.get(scope);
        if (evaluators == null)
        {
            evaluators = new LinkedHashMap<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>>();
            actionEvaluators.put(scope, evaluators);
        }
        if (evaluators.get(evaluator.getAction()) != null)
        {
            throw new AlfrescoRuntimeException("Already registered Action Evaluator " + evaluator.getAction() + " for scope " + scope);
        }
        evaluators.put(evaluator.getAction(), evaluator);
        
        if (logger.isDebugEnabled())
            logger.debug("Registered Action Evaluator: scope=" + scope + ", evaluator=" + evaluator);
    }

    public Collection<Pair<String, Boolean>> getReportedPermissions(String permission, Set<String> permissions, boolean hasFull, boolean isDirect, CMISAccessControlFormatEnum format)
    {
        ArrayList<Pair<String, Boolean>> answer = new ArrayList<Pair<String, Boolean>>(20);
        // indirect
        
        if(hasFull)
        {
            answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, false));
            answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, false));
            answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_ALL_PERMISSION, false));
        }
        
        for(String perm : permissions)
        {
            if(PermissionService.READ.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, false));
            }
            else if(PermissionService.WRITE.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, false));
            }
            else if(PermissionService.ALL_PERMISSIONS.equals(perm))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_ALL_PERMISSION, false));
            }
            
            if(hasFull)
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_ALL_PERMISSION, false));
            }
        }
        
        // permission
       
        if(format == CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS)
        {
            if(PermissionService.READ.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            }
            else if(PermissionService.WRITE.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            }
            else if(PermissionService.ALL_PERMISSIONS.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_ALL_PERMISSION, false));
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            }
            else
            {
                answer.add(new Pair<String, Boolean>(permission, isDirect));
            }
        }
        else if(format == CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS)
        {
            if(PermissionService.READ.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_READ_PERMISSION, isDirect));
            }
            else if(PermissionService.WRITE.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_WRITE_PERMISSION, isDirect));
            }
            else if(PermissionService.ALL_PERMISSIONS.equals(permission))
            {
                answer.add(new Pair<String, Boolean>(CMISAccessControlService.CMIS_ALL_PERMISSION, isDirect));
            }
            else
            {
               // else nothing
            }
        }
        
       
        
        return answer;
    }
    
    
    /**
     * @param permission
     * @return permission to set
     */
    public String getSetPermission(String permission)
    {
        if(permission.equals(CMISAccessControlService.CMIS_READ_PERMISSION))
        {
            return PermissionService.READ;
        }
        else if(permission.equals(CMISAccessControlService.CMIS_WRITE_PERMISSION))
        {
            return PermissionService.WRITE;
        }
        else if(permission.equals(CMISAccessControlService.CMIS_ALL_PERMISSION))
        {
            return PermissionService.ALL_PERMISSIONS;
        }
        else
        {
            return permission;
        }
            
    }

}
