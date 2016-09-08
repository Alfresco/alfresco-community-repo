/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Records management service implementation.
 *
 * @author Roy Wetherall
 */
public class RecordsManagementServiceImpl extends ServiceBaseImpl
                                          implements RecordsManagementService,
                                                     RecordsManagementModel,
                                                     RecordsManagementPolicies.OnCreateReference,
                                                     RecordsManagementPolicies.OnRemoveReference
{
    /** I18N */
    private final static String MSG_ERROR_ADD_CONTENT_CONTAINER = "rm.service.error-add-content-container";
    private final static String MSG_UPDATE_DISP_ACT_DEF = "rm.service.update-disposition-action-def";
    private final static String MSG_SET_ID = "rm.service.set-id";
    private final static String MSG_RECORD_FOLDER_EXPECTED = "rm.service.record-folder-expected";
    private final static String MSG_PARENT_RECORD_FOLDER_ROOT = "rm.service.parent-record-folder-root";
    private final static String MSG_PARENT_RECORD_FOLDER_TYPE = "rm.service.parent-record-folder-type";
    private final static String MSG_RECORD_FOLDER_TYPE = "rm.service.record-folder-type";

    /** Store that the RM roots are contained within */
    @SuppressWarnings("unused")
    @Deprecated
    private StoreRef defaultStoreRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    /** Service registry */
    private RecordsManagementServiceRegistry serviceRegistry;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Well-known location of the scripts folder. */
    private NodeRef scriptsFolderNodeRef = new NodeRef("workspace", "SpacesStore", "rm_behavior_scripts");

    /** Java behaviour */
    private JavaBehaviour onChangeToDispositionActionDefinition;

    /**
     * Set the service registry service
     *
     * @param serviceRegistry   service registry
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry serviceRegistry)
    {
        // Internal ops use the unprotected services from the voter (e.g. nodeService)
        this.serviceRegistry = serviceRegistry;
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    /**
     * Set policy component
     *
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Sets the default RM store reference
     * @param defaultStoreRef    store reference
     */
    @Deprecated
    public void setDefaultStoreRef(StoreRef defaultStoreRef)
    {
        this.defaultStoreRef = defaultStoreRef;
    }

    private FilePlanService getFilePlanService()
    {
    	return (FilePlanService)applicationContext.getBean("filePlanService");
    }

    /**
     * Init method.  Registered behaviours.
     */
    public void init()
    {
        // Register the association behaviours
        policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                TYPE_FILE_PLAN,
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onAddContentToContainer", NotificationFrequency.EVERY_EVENT));
        policyComponent.bindAssociationBehaviour(
                  QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                  TYPE_RECORD_CATEGORY,
                  ContentModel.ASSOC_CONTAINS,
                  new JavaBehaviour(this, "onAddContentToContainer", NotificationFrequency.EVERY_EVENT));

        // TODO move this into the record service
        policyComponent.bindAssociationBehaviour(
               QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
               ASPECT_RECORD,
               RenditionModel.ASSOC_RENDITION,
               new JavaBehaviour(this, "onAddRecordThumbnail", NotificationFrequency.TRANSACTION_COMMIT)
               );

        // Register script execution behaviour on RM property update.
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "onChangeToAnyRmProperty", NotificationFrequency.TRANSACTION_COMMIT));

        // Disposition behaviours
        onChangeToDispositionActionDefinition = new JavaBehaviour(this, "onChangeToDispositionActionDefinition", NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                TYPE_DISPOSITION_ACTION_DEFINITION,
                onChangeToDispositionActionDefinition);

        // Reference behaviours
        policyComponent.bindClassBehaviour(RecordsManagementPolicies.ON_CREATE_REFERENCE,
                                           ASPECT_RECORD,
                                           new JavaBehaviour(this, "onCreateReference", NotificationFrequency.TRANSACTION_COMMIT));

        policyComponent.bindClassBehaviour(RecordsManagementPolicies.ON_REMOVE_REFERENCE,
                                           ASPECT_RECORD,
                                           new JavaBehaviour(this, "onRemoveReference", NotificationFrequency.TRANSACTION_COMMIT));

        // Identifier behaviours
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                                           ASPECT_RECORD_COMPONENT_ID,
                                           new JavaBehaviour(this, "onIdentifierUpdate", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * On add content to container
     *
     * Prevents content nodes being added to record series and record category folders
     * by imap, cifs etc.
     *
     * @param childAssocRef
     * @param bNew
     */
    public void onAddContentToContainer(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef parent = childAssocRef.getParentRef();
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (instanceOf(nodeRef, ContentModel.TYPE_CONTENT) == true)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_ERROR_ADD_CONTENT_CONTAINER));
        }
        if (isFilePlan(parent) == true && isRecordFolder(nodeRef) == true)
        {
            throw new AlfrescoRuntimeException("Operation failed, because you can not place a record folder in the root of the file plan.");
        }
    }

    /**
     * Make sure the thumbnails of records are marked as file plan components as are therefore subject to the same
     * permission restrictions.
     *
     * @param childAssocRef
     * @param bNew
     */
    public void onAddRecordThumbnail(final ChildAssociationRef childAssocRef, final boolean bNew)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                NodeRef thumbnail = childAssocRef.getChildRef();

                if (nodeService.exists(thumbnail) == true)
                {
                    // apply file plan component aspect to thumbnail
                    nodeService.addAspect(thumbnail, ASPECT_FILE_PLAN_COMPONENT, null);

                    // manage any extended readers
                    ExtendedSecurityService extendedSecurityService = serviceRegistry.getExtendedSecurityService();
                    NodeRef parent = childAssocRef.getParentRef();
                    Set<String> readers = extendedSecurityService.getExtendedReaders(parent);
                    Set<String> writers = extendedSecurityService.getExtendedWriters(parent);
                    if (readers != null && readers.size() != 0)
                    {
                        extendedSecurityService.addExtendedSecurity(thumbnail, readers, writers, false);
                    }
                }

                return null;
            }
        });
    }

    /**
     * Called after a DispositionActionDefinition property has been updated.
     */
    public void onChangeToDispositionActionDefinition(NodeRef node, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        if (nodeService.exists(node) == true)
        {
            onChangeToDispositionActionDefinition.disable();
            try
            {
                // Determine the properties that have changed
                Set<QName> changedProps = this.determineChangedProps(oldProps, newProps);

                if (nodeService.hasAspect(node, ASPECT_UNPUBLISHED_UPDATE) == false)
                {
                    // Apply the unpublished aspect
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(PROP_UPDATE_TO, UPDATE_TO_DISPOSITION_ACTION_DEFINITION);
                    props.put(PROP_UPDATED_PROPERTIES, (Serializable)changedProps);
                    nodeService.addAspect(node, ASPECT_UNPUBLISHED_UPDATE, props);
                }
                else
                {
                    Map<QName, Serializable> props = nodeService.getProperties(node);

                    // Check that there isn't a update currently being published
                    if ((Boolean)props.get(PROP_PUBLISH_IN_PROGRESS).equals(Boolean.TRUE) == true)
                    {
                        // Can not update the disposition schedule since there is an outstanding update being published
                        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UPDATE_DISP_ACT_DEF));
                    }

                    // Update the update information
                    props.put(PROP_UPDATE_TO, UPDATE_TO_DISPOSITION_ACTION_DEFINITION);
                    props.put(PROP_UPDATED_PROPERTIES, (Serializable)changedProps);
                    nodeService.setProperties(node, props);
                }
            }
            finally
            {
                onChangeToDispositionActionDefinition.enable();
            }
        }
    }

    /**
     * Called after any Records Management property has been updated.
     */
    public void onChangeToAnyRmProperty(final NodeRef node, final Map<QName, Serializable> oldProps, final Map<QName, Serializable> newProps)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(node) == true)
                {
                    RecordsManagementServiceImpl.this.lookupAndExecuteScripts(node, oldProps, newProps);
                }

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    /**
     * Property update behaviour implementation
     *
     * @param node
     * @param oldProps
     * @param newProps
     */
    public void onIdentifierUpdate(NodeRef node, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
       if (nodeService.exists(node) == true)
       {
           String newIdValue = (String)newProps.get(PROP_IDENTIFIER);
           if (newIdValue != null)
           {
               String oldIdValue = (String)oldProps.get(PROP_IDENTIFIER);
               if (oldIdValue != null && oldIdValue.equals(newIdValue) == false)
               {
                   throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_SET_ID, node.toString()));
               }
           }
       }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnCreateReference#onCreateReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions")) == true)
        {
            // Apply the versioned aspect to the from node
            this.nodeService.addAspect(fromNodeRef, ASPECT_VERSIONED_RECORD, null);
        }

        // Execute script if for the reference event
        executeReferenceScript("onCreate", reference, fromNodeRef, toNodeRef);
    }

    /**
     * Executes a reference script if present
     *
     * @param policy
     * @param reference
     * @param from
     * @param to
     */
    private void executeReferenceScript(String policy, QName reference, NodeRef from, NodeRef to)
    {
        String referenceId = reference.getLocalName();

        // This is the filename pattern which is assumed.
        // e.g. a script file onCreate_superceded.js for the creation of a superseded reference
        String expectedScriptName = policy + "_" + referenceId + ".js";

        NodeRef scriptNodeRef = nodeService.getChildByName(scriptsFolderNodeRef, ContentModel.ASSOC_CONTAINS, expectedScriptName);
        if (scriptNodeRef != null)
        {
            Map<String, Object> objectModel = new HashMap<String, Object>(1);
            objectModel.put("node", from);
            objectModel.put("toNode", to);
            objectModel.put("policy", policy);
            objectModel.put("reference", referenceId);

            serviceRegistry.getScriptService().executeScript(scriptNodeRef, null, objectModel);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRemoveReference#onRemoveReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onRemoveReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference)
    {
        // Deal with versioned records
        if (reference.equals(QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions")) == true)
        {
            // Apply the versioned aspect to the from node
            this.nodeService.removeAspect(fromNodeRef, ASPECT_VERSIONED_RECORD);
        }

        // Execute script if for the reference event
        executeReferenceScript("onRemove", reference, fromNodeRef, toNodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isFilePlan(org.alfresco.service.cmr.repository.NodeRef)
     *
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Deprecated
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlan(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isFilePlanContainer(NodeRef)}
     */
    @Override
    public boolean isRecordsManagementContainer(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanContainer(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isFilePlanComponent(NodeRef)}
     */
    public boolean isFilePlanComponent(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanComponent(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getFilePlanComponentKind(NodeRef)}
     */
    public FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef)
    {
    	return getFilePlanService().getFilePlanComponentKind(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getFilePlanComponentKindFromType(QName)}
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKindFromType(QName type)
    {
        return getFilePlanService().getFilePlanComponentKindFromType(type);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#isRecordCategory(NodeRef)}
     */
    public boolean isRecordCategory(NodeRef nodeRef)
    {
        return getFilePlanService().isRecordCategory(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordFolder(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecordFolder(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_RECORD_FOLDER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isTransfer(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isTransfer(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_TRANSFER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isMetadataStub(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isMetadataStub(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_GHOSTED);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isCutoff(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isCutoff(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_CUT_OFF);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getFilePlan(org.alfresco.service.cmr.repository.NodeRef)
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlan(NodeRef)}
     */
    @Deprecated
    public NodeRef getFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().getFilePlan(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getNodeRefPath(NodeRef)}
     */
    public List<NodeRef> getNodeRefPath(NodeRef nodeRef)
    {
        return getFilePlanService().getNodeRefPath(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecordsManagementRoots(org.alfresco.service.cmr.repository.StoreRef)
     *
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlans()}
     */
    @Deprecated
    public List<NodeRef> getFilePlans()
    {
        return new ArrayList<NodeRef>(getFilePlanService().getFilePlans());
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName, Map)}
     */
    public NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, type, properties);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, Map)}
     */
    public NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, properties);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String)}
     */
    public NodeRef createFilePlan(NodeRef parent, String name)
    {
        return getFilePlanService().createFilePlan(parent, name);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createFilePlan(parent, name, type);
    }

    /**
     * @deprecated as of 2.1
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, type, properties);
    }

    /**
     * @deprecated as of 2.1
     */
    public NodeRef createRecordCategory(NodeRef parent, String name)
    {
        return getFilePlanService().createRecordCategory(parent, name);
    }

    /**
     * @deprecated as of 2.1
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, properties);
    }

    /**
     * @deprecated as of 2.1
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createRecordCategory(parent, name, type);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container)
    {
        return getFilePlanService().getAllContained(container);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container, boolean deep)
    {
    	return getFilePlanService().getAllContained(container, deep);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container)
    {
        return getFilePlanService().getContainedRecordCategories(container);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordCategories(container, deep);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container)
    {
        return getFilePlanService().getContainedRecordFolders(container);
    }

    /**
     * @deprecated as of 2.1
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordFolders(container, deep);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordFolderDeclared(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecordFolderDeclared(NodeRef recordFolder)
    {
        // Check we have a record folder
        if (isRecordFolder(recordFolder) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_EXPECTED));
        }

        boolean result = true;

        // Check that each record in the record folder in declared
        List<NodeRef> records = getRecords(recordFolder);
        for (NodeRef record : records)
        {
            if (serviceRegistry.getRecordService().isDeclared(record) == false)
            {
                result = false;
                break;
            }
        }

        return result;

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordFolderClosed(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isRecordFolderClosed(NodeRef nodeRef)
    {
        // Check we have a record folder
        if (isRecordFolder(nodeRef) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_EXPECTED));
        }

        return ((Boolean)this.nodeService.getProperty(nodeRef, PROP_IS_CLOSED)).booleanValue();
    }


    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecordFolders(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getRecordFolders(NodeRef record)
    {
        List<NodeRef> result = new ArrayList<NodeRef>(1);
        if (isRecord(record) == true)
        {
            List<ChildAssociationRef> assocs = this.nodeService.getParentAssocs(record, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef parent = assoc.getParentRef();
                if (isRecordFolder(parent) == true)
                {
                    result.add(parent);
                }
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordFolder(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("rmContainer", rmContainer);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("type", type);

        // Check that we are not trying to create a record folder in a root container
        if (isFilePlan(rmContainer) == true)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PARENT_RECORD_FOLDER_ROOT));
        }

        // Check that the parent is a container
        QName parentType = nodeService.getType(rmContainer);
        if (TYPE_RECORD_CATEGORY.equals(parentType) == false &&
            dictionaryService.isSubClass(parentType, TYPE_RECORD_CATEGORY) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PARENT_RECORD_FOLDER_TYPE, parentType.toString()));
        }

        // Check that the the provided type is a sub-type of rm:recordFolder
        if (TYPE_RECORD_FOLDER.equals(type) == false &&
            dictionaryService.isSubClass(type, TYPE_RECORD_FOLDER) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_TYPE, type.toString()));
        }

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        if (properties != null && properties.size() != 0)
        {
            props.putAll(properties);
        }
        props.put(ContentModel.PROP_NAME, name);

        return nodeService.createNode(
                rmContainer,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                type,
                props).getChildRef();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordFolder(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createRecordFolder(NodeRef rmContrainer, String name)
    {
        // TODO defaults to rm:recordFolder, but in future could auto-detect sub-type of folder based on
        //      context
        return createRecordFolder(rmContrainer, name, TYPE_RECORD_FOLDER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordFolder(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public NodeRef createRecordFolder(NodeRef parent, String name,  Map<QName, Serializable> properties)
    {
        return createRecordFolder(parent, name, TYPE_RECORD_FOLDER, properties);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordFolder(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public NodeRef createRecordFolder(NodeRef parent, String name, QName type)
    {
        return createRecordFolder(parent, name, type, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecords(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getRecords(NodeRef recordFolder)
    {
        List<NodeRef> result = new ArrayList<NodeRef>(1);
        if (isRecordFolder(recordFolder) == true)
        {
            List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(recordFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef child = assoc.getChildRef();
                if (isRecord(child) == true)
                {
                    result.add(child);
                }
            }
        }
        return result;
    }

    /**
     * This method examines the old and new property sets and for those properties which
     * have changed, looks for script resources corresponding to those properties.
     * Those scripts are then called via the ScriptService.
     *
     * @param nodeWithChangedProperties the node whose properties have changed.
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     *
     * @see #lookupScripts(Map<QName, Serializable>, Map<QName, Serializable>)
     */
    private void lookupAndExecuteScripts(NodeRef nodeWithChangedProperties,
            Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        List<NodeRef> scriptRefs = lookupScripts(oldProps, newProps);

        Map<String, Object> objectModel = new HashMap<String, Object>(1);
        objectModel.put("node", nodeWithChangedProperties);
        objectModel.put("oldProperties", oldProps);
        objectModel.put("newProperties", newProps);

        for (NodeRef scriptRef : scriptRefs)
        {
            serviceRegistry.getScriptService().executeScript(scriptRef, null, objectModel);
        }
    }

    /**
     * This method determines which properties have changed and for each such property
     * looks for a script resource in a well-known location.
     *
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     * @return A list of nodeRefs corresponding to the Script resources.
     *
     * @see #determineChangedProps(Map<QName, Serializable>, Map<QName, Serializable>)
     */
    private List<NodeRef> lookupScripts(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        List<NodeRef> result = new ArrayList<NodeRef>();

        Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(oldProps, newProps);
        for (QName propQName : changedProps.keySet())
        {
            QName prefixedQName = propQName.getPrefixedQName(serviceRegistry.getNamespaceService());

            String [] splitQName = QName.splitPrefixedQName(prefixedQName.toPrefixString());
            final String shortPrefix = splitQName[0];
            final String localName = splitQName[1];

            // This is the filename pattern which is assumed.
            // e.g. a script file cm_name.js would be called for changed to cm:name
            String expectedScriptName = shortPrefix + "_" + localName + ".js";

            NodeRef nextElement = nodeService.getChildByName(scriptsFolderNodeRef, ContentModel.ASSOC_CONTAINS, expectedScriptName);
            if (nextElement != null) result.add(nextElement);
        }

        return result;
    }

    /**
     * This method compares the oldProps map against the newProps map and returns
     * a set of QNames of the properties that have changed. Changed here means one of
     * <ul>
     * <li>the property has been removed</li>
     * <li>the property has had its value changed</li>
     * <li>the property has been added</li>
     * </ul>
     */
    private Set<QName> determineChangedProps(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        Set<QName> result = new HashSet<QName>();
        for (QName qn : oldProps.keySet())
        {
            if (newProps.get(qn) == null ||
                newProps.get(qn).equals(oldProps.get(qn)) == false)
            {
                result.add(qn);
            }
        }
        for (QName qn : newProps.keySet())
        {
            if (oldProps.get(qn) == null)
            {
                result.add(qn);
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecordMetaDataAspects()
     */
    @Override
    @Deprecated
    public Set<QName> getRecordMetaDataAspects()
    {
        return serviceRegistry.getRecordService().getRecordMetaDataAspects();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordDeclared(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public boolean isRecordDeclared(NodeRef nodeRef)
    {
        return serviceRegistry.getRecordService().isDeclared(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public boolean isHold(NodeRef nodeRef)
    {
        return serviceRegistry.getFreezeService().isHold(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public boolean isFrozen(NodeRef nodeRef)
    {
        return serviceRegistry.getFreezeService().isFrozen(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#hasFrozenChildren(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        return serviceRegistry.getFreezeService().hasFrozenChildren(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public boolean isRecord(NodeRef nodeRef)
    {
        return serviceRegistry.getRecordService().isRecord(nodeRef);
    }
}
