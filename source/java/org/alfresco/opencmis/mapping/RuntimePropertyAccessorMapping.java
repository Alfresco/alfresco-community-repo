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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.opencmis.dictionary.PropertyAccessorMapping;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Registry of property accessors which map the retrieval and setting of
 * properties within Alfresco.
 * 
 * @author davidc
 */
public class RuntimePropertyAccessorMapping implements PropertyAccessorMapping, InitializingBean
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISMapping.class);

    // Service dependencies
    private ServiceRegistry serviceRegistry;
    private CMISConnector cmisConnector;
    private CMISMapping cmisMapping;
    private CMISDictionaryService cmisDictionaryService;

    private Map<String, AbstractProperty> propertyAccessors = new HashMap<String, AbstractProperty>();
    private Map<BaseTypeId, Map<Action, CMISActionEvaluator<? extends Object>>> actionEvaluators = new HashMap<BaseTypeId, Map<Action, CMISActionEvaluator<? extends Object>>>();

    /**
     * @param service
     *            registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param cmis
     *            connector
     */
    public void setCmisConnector(CMISConnector cmisConnector)
    {
        this.cmisConnector = cmisConnector;
    }

    /**
     * @param cmis
     *            mapping
     */
    public void setCmisMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }

    /**
     * @param cmis
     *            mapping
     */
    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        //
        // Property Mappings
        //

        registerPropertyAccessor(new ObjectIdProperty(serviceRegistry));
        registerPropertyAccessor(new NodeRefProperty(serviceRegistry));
        registerPropertyAccessor(new ObjectTypeIdProperty(serviceRegistry, cmisDictionaryService));
        registerPropertyAccessor(new BaseTypeIdProperty(serviceRegistry, cmisDictionaryService));
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
        registerPropertyAccessor(new AllowedChildObjectTypeIdsProperty(serviceRegistry, cmisMapping));
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
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CanCancelCheckOutActionEvaluator(serviceRegistry));
        registerEvaluator(BaseTypeId.CMIS_DOCUMENT, new CanCheckInActionEvaluator(serviceRegistry));
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
     * Gets a property accessor
     * 
     * @param propertyId
     *            property id
     * @return property accessor
     */
    public CMISPropertyAccessor getPropertyAccessor(String propertyId)
    {
        return propertyAccessors.get(propertyId);
    }

    /**
     * Create a direct node property accessor
     * 
     * @param propertyId
     *            property id
     * @param propertyName
     *            node property name
     * @return property accessor
     */
    public CMISPropertyAccessor createDirectPropertyAccessor(String propertyId, QName propertyName)
    {
        return new DirectProperty(serviceRegistry, propertyId, propertyName);
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
     * @param scope
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

}
