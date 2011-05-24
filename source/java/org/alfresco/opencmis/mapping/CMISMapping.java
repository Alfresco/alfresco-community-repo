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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISActionEvaluator;
import org.alfresco.opencmis.CMISConnector;
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
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
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
    public static QName DOCUMENT_QNAME = QName.createQName(CMIS_MODEL_URI, "document");
    public static QName FOLDER_QNAME = QName.createQName(CMIS_MODEL_URI, "folder");
    public static QName RELATIONSHIP_QNAME = QName.createQName(CMIS_MODEL_URI, "relationship");
    public static QName POLICY_QNAME = QName.createQName(CMIS_MODEL_URI, "policy");
    public static QName ASPECTS_QNAME = QName.createQName(CMIS_EXT_URI, "aspects");

    // CMIS Internal Type Ids
    public static String OBJECT_TYPE_ID = "cmisext:object";

    /**
     * Basic permissions.
     */
    public static final String CMIS_READ = "cmis:read";
    public static final String CMIS_WRITE = "cmis:write";
    public static final String CMIS_ALL = "cmis:all";

    // Service Dependencies
    private ServiceRegistry serviceRegistry;
    private CMISConnector cmisConnector;

    // Mappings
    private Map<QName, String> mapAlfrescoQNameToTypeId = new HashMap<QName, String>();
    private Map<QName, QName> mapCmisQNameToAlfrescoQName = new HashMap<QName, QName>();
    private Map<QName, QName> mapAlfrescoQNameToCmisQName = new HashMap<QName, QName>();
    private Map<QName, PropertyType> mapAlfrescoToCmisDataType = new HashMap<QName, PropertyType>();
    private Map<PropertyType, QName> mapCmisDataTypeToAlfresco = new HashMap<PropertyType, QName>();
    private Map<String, AbstractProperty> propertyAccessors = new HashMap<String, AbstractProperty>();
    private Map<BaseTypeId, Map<Action, CMISActionEvaluator<? extends Object>>> actionEvaluators = new HashMap<BaseTypeId, Map<Action, CMISActionEvaluator<? extends Object>>>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        //
        // Type Mappings
        //

        mapAlfrescoQNameToTypeId.put(OBJECT_QNAME, OBJECT_TYPE_ID);
        mapAlfrescoQNameToTypeId.put(DOCUMENT_QNAME, BaseTypeId.CMIS_DOCUMENT.value());
        mapAlfrescoQNameToTypeId.put(FOLDER_QNAME, BaseTypeId.CMIS_FOLDER.value());
        mapAlfrescoQNameToTypeId.put(RELATIONSHIP_QNAME, BaseTypeId.CMIS_RELATIONSHIP.value());
        mapAlfrescoQNameToTypeId.put(POLICY_QNAME, BaseTypeId.CMIS_POLICY.value());

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

        //
        // Property Mappings
        //

        registerPropertyAccessor(new ObjectIdProperty(serviceRegistry));
        registerPropertyAccessor(new NodeRefProperty(serviceRegistry));
        registerPropertyAccessor(new ObjectTypeIdProperty(serviceRegistry));
        registerPropertyAccessor(new BaseTypeIdProperty(serviceRegistry));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.CREATED_BY, ContentModel.PROP_CREATOR));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.CREATION_DATE,
                ContentModel.PROP_CREATED));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.LAST_MODIFIED_BY,
                ContentModel.PROP_MODIFIER));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.LAST_MODIFICATION_DATE,
                ContentModel.PROP_MODIFIED));
        registerPropertyAccessor(new FixedValueProperty(serviceRegistry, PropertyIds.CHANGE_TOKEN, null));
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.NAME, ContentModel.PROP_NAME)
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
        registerPropertyAccessor(new DirectProperty(serviceRegistry, PropertyIds.CONTENT_STREAM_FILE_NAME,
                ContentModel.PROP_NAME));
        registerPropertyAccessor(new ParentProperty(serviceRegistry));
        registerPropertyAccessor(new PathProperty(serviceRegistry, cmisConnector));
        registerPropertyAccessor(new AllowedChildObjectTypeIdsProperty(serviceRegistry, this));
        registerPropertyAccessor(new SourceIdProperty(serviceRegistry));
        registerPropertyAccessor(new TargetIdProperty(serviceRegistry));

        //
        // Action Evaluator Mappings
        //

        // NOTE: The order of evaluators is important - they must be in the
        // order as specified in CMIS-Core.xsd
        // so that schema validation passes

        registerEvaluator(BaseTypeId.CMIS_DOCUMENT,
                new CurrentVersionEvaluator(serviceRegistry, new PermissionActionEvaluator(serviceRegistry,
                        Action.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_UPDATE_PROPERTIES,
                        PermissionService.WRITE_PROPERTIES), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_PROPERTIES, PermissionService.READ_PROPERTIES));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_OBJECT_RELATIONSHIPS, true));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new ParentActionEvaluator(new PermissionActionEvaluator(
                serviceRegistry, Action.CAN_GET_OBJECT_PARENTS, PermissionService.READ_PERMISSIONS)));
        // Is CAN_MOVE correct mapping?
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_MOVE_OBJECT, PermissionService.DELETE_NODE),
                false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_DELETE_CONTENT_STREAM,
                        PermissionService.WRITE_PROPERTIES, PermissionService.WRITE_CONTENT), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new CanCheckOutActionEvaluator(serviceRegistry), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_CANCEL_CHECK_OUT,
                        PermissionService.CANCEL_CHECK_OUT), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new PermissionActionEvaluator(serviceRegistry, Action.CAN_CHECK_IN,
                PermissionService.CHECK_IN));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_SET_CONTENT_STREAM,
                        PermissionService.WRITE_CONTENT), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_ALL_VERSIONS, true));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry,
                        Action.CAN_ADD_OBJECT_TO_FOLDER, PermissionService.LINK_CHILDREN)), false));
        // Is CAN_REMOVE_FROM_FOLDER correct mapping?
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new ParentActionEvaluator(new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                        Action.CAN_REMOVE_OBJECT_FROM_FOLDER, true)), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_CONTENT_STREAM, PermissionService.READ_CONTENT));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_APPLY_POLICY, false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_APPLIED_POLICIES, true));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_REMOVE_POLICY, false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new FixedValueActionEvaluator<NodeRef>(serviceRegistry, Action.CAN_CREATE_RELATIONSHIP, true), false));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_RENDITIONS, true));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new PermissionActionEvaluator(serviceRegistry, Action.CAN_GET_ACL,
                PermissionService.READ_PERMISSIONS));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CurrentVersionEvaluator(serviceRegistry,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_APPLY_ACL,
                        PermissionService.CHANGE_PERMISSIONS), false));

        registerEvaluator(BaseTypeId.CMIS_FOLDER,
                new RootFolderEvaluator(serviceRegistry, cmisConnector, new PermissionActionEvaluator(serviceRegistry,
                        Action.CAN_DELETE_OBJECT, PermissionService.DELETE_NODE), false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_UPDATE_PROPERTIES, PermissionService.WRITE_PROPERTIES));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_FOLDER_TREE, PermissionService.READ_CHILDREN));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_PROPERTIES, PermissionService.READ_PROPERTIES));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_OBJECT_RELATIONSHIPS, true));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new ParentActionEvaluator(new PermissionActionEvaluator(
                serviceRegistry, Action.CAN_GET_OBJECT_PARENTS, PermissionService.READ_PERMISSIONS)));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new RootFolderEvaluator(serviceRegistry, cmisConnector,
                new ParentActionEvaluator(new PermissionActionEvaluator(serviceRegistry, Action.CAN_GET_FOLDER_PARENT,
                        PermissionService.READ_PERMISSIONS)), false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_DESCENDANTS, PermissionService.READ_CHILDREN));
        // Is CAN_MOVE_OBJECT correct mapping?
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new RootFolderEvaluator(serviceRegistry, cmisConnector,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_MOVE_OBJECT, PermissionService.DELETE_NODE),
                false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_APPLY_POLICY, false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_APPLIED_POLICIES, true));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_REMOVE_POLICY, false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_GET_CHILDREN, PermissionService.READ_CHILDREN));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_CREATE_DOCUMENT, PermissionService.CREATE_CHILDREN));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_CREATE_FOLDER, PermissionService.CREATE_CHILDREN));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry,
                Action.CAN_CREATE_RELATIONSHIP, PermissionService.CREATE_ASSOCIATIONS));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new RootFolderEvaluator(serviceRegistry, cmisConnector,
                new PermissionActionEvaluator(serviceRegistry, Action.CAN_DELETE_TREE, PermissionService.DELETE_NODE),
                false));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry, Action.CAN_GET_ACL,
                PermissionService.READ_PERMISSIONS));
        registerEvaluator(BaseTypeId.CMIS_FOLDER, new PermissionActionEvaluator(serviceRegistry, Action.CAN_APPLY_ACL,
                PermissionService.CHANGE_PERMISSIONS));

        registerEvaluator(BaseTypeId.CMIS_RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry,
                Action.CAN_DELETE_OBJECT, true));
        registerEvaluator(BaseTypeId.CMIS_RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry,
                Action.CAN_UPDATE_PROPERTIES, false));
        registerEvaluator(BaseTypeId.CMIS_RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry,
                Action.CAN_GET_PROPERTIES, true));
        registerEvaluator(BaseTypeId.CMIS_RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry,
                Action.CAN_GET_ACL, false));
        registerEvaluator(BaseTypeId.CMIS_RELATIONSHIP, new FixedValueActionEvaluator<AssociationRef>(serviceRegistry,
                Action.CAN_APPLY_ACL, false));

        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_DELETE_OBJECT, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_UPDATE_PROPERTIES, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_PROPERTIES, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_OBJECT_PARENTS, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_MOVE_OBJECT, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_ADD_OBJECT_TO_FOLDER, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_REMOVE_OBJECT_FROM_FOLDER, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_OBJECT_RELATIONSHIPS, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_GET_ACL, false));
        registerEvaluator(BaseTypeId.CMIS_POLICY, new FixedValueActionEvaluator<NodeRef>(serviceRegistry,
                Action.CAN_APPLY_ACL, false));

    }

    /**
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCmisConnector(CMISConnector cmisConnector)
    {
        this.cmisConnector = cmisConnector;
    }

    /**
     * @return namespaceService
     */
    /* package */NamespaceService getNamespaceService()
    {
        return serviceRegistry.getNamespaceService();
    }

    /**
     * Gets the CMIS Type Id given the Alfresco QName for the type in any
     * Alfresco model
     * 
     * @param typeQName
     * @return
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
            case CMIS_POLICY:
                p = "P";
                break;
            default:
                throw new CmisRuntimeException("Invalid base type!");
            }

            return p + ":" + typeQName.toPrefixString(serviceRegistry.getNamespaceService());
        } else
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
        if (isValidCmisPolicy(classQName))
        {
            return getCmisTypeId(BaseTypeId.CMIS_POLICY, classQName);
        }

        return null;
    }

    public String buildPrefixEncodedString(QName qname)
    {
        return qname.toPrefixString(serviceRegistry.getNamespaceService());
    }

    public QName getAlfrescoName(String typeId)
    {
        // Is it an Alfresco type id?
        if (typeId.length() < 4 || typeId.charAt(1) != ':')
        {
            throw new CmisInvalidArgumentException("Malformed type id '" + typeId + "'");
        }

        return QName.createQName(typeId.substring(2), serviceRegistry.getNamespaceService());
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
        }

        return false;
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
            } else
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
        if (typeQName.equals(ASPECTS_QNAME))
        {
            return true;
        }

        AspectDefinition aspectDef = serviceRegistry.getDictionaryService().getAspect(typeQName);
        if (aspectDef == null)
        {
            return false;
        }

        if (aspectDef.getName().equals(ContentModel.ASPECT_VERSIONABLE)
                || aspectDef.getName().equals(ContentModel.ASPECT_AUDITABLE)
                || aspectDef.getName().equals(ContentModel.ASPECT_REFERENCEABLE))
        {
            return false;
        }
        return true;
    }

    /**
     * Is an association valid in CMIS? It must be a non-child relationship and
     * the source and target must both be valid CMIS types.
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
        AssociationDefinition associationDefinition = serviceRegistry.getDictionaryService().getAssociation(
                associationQName);
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
     * @param cmisTypeQName
     * @return
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
     * @param dictionaryService
     * @param propertyQName
     * @return
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
     */
    public AbstractProperty getPropertyAccessor(String propertyId)
    {
        return propertyAccessors.get(propertyId);
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
     */
    public Map<Action, CMISActionEvaluator<? extends Object>> getActionEvaluators(BaseTypeId scope)
    {
        Map<Action, CMISActionEvaluator<? extends Object>> evaluators = actionEvaluators.get(scope);
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
    private void registerEvaluator(BaseTypeId scope, CMISActionEvaluator<? extends Object> evaluator)
    {
        Map<Action, CMISActionEvaluator<? extends Object>> evaluators = actionEvaluators.get(scope);
        if (evaluators == null)
        {
            evaluators = new LinkedHashMap<Action, CMISActionEvaluator<? extends Object>>();
            actionEvaluators.put(scope, evaluators);
        }
        if (evaluators.get(evaluator.getAction()) != null)
        {
            throw new AlfrescoRuntimeException("Already registered Action Evaluator " + evaluator.getAction()
                    + " for scope " + scope);
        }
        evaluators.put(evaluator.getAction(), evaluator);

        if (logger.isDebugEnabled())
            logger.debug("Registered Action Evaluator: scope=" + scope + ", evaluator=" + evaluator);
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
     * @param permission
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
