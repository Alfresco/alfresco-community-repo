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
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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

    /**
     * @return File plan service
     */
    private FilePlanService getFilePlanService()
    {
    	return serviceRegistry.getFilePlanService();
    }

    /**
     * @return Record Folder Service
     */
    private RecordFolderService getRecordFolderService()
    {
        return serviceRegistry.getRecordFolderService();
    }

    /**
     * @return Record Service
     */
    private RecordService getRecordService()
    {
        return serviceRegistry.getRecordService();
    }

    /**
     * @return Freeze Service
     */
    private FreezeService getFreezeService()
    {
        return serviceRegistry.getFreezeService();
    }

    /**
     * @return Disposition Service
     */
    private DispositionService getDispositionService()
    {
        return serviceRegistry.getDispositionService();
    }

    /**
     * @return Extended Security Service
     */
    private ExtendedSecurityService getExtendedSecurityService()
    {
        return serviceRegistry.getExtendedSecurityService();
    }

    /**
     * @return Script Service
     */
    private ScriptService getScriptService()
    {
        return serviceRegistry.getScriptService();
    }

    /**
     * @return Namespace service
     */
    private NamespaceService getNamespaceService()
    {
        return serviceRegistry.getNamespaceService();
    }

    /**
     * @return Transfer service
     */
    private TransferService getTransferService()
    {
        return serviceRegistry.getTransferService();
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
                    NodeRef parent = childAssocRef.getParentRef();
                    ExtendedSecurityService extendedSecurityService = getExtendedSecurityService();
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

        ScriptService scriptService = getScriptService();
        for (NodeRef scriptRef : scriptRefs)
        {
            scriptService.executeScript(scriptRef, null, objectModel);
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
            QName prefixedQName = propQName.getPrefixedQName(getNamespaceService());

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

            getScriptService().executeScript(scriptNodeRef, null, objectModel);
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
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlanComponent(NodeRef)}
     */
    @Override
    public boolean isFilePlanComponent(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanComponent(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKind(NodeRef)}
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef)
    {
        return getFilePlanService().getFilePlanComponentKind(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKindFromType(QName)}
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKindFromType(QName type)
    {
        return getFilePlanService().getFilePlanComponentKindFromType(type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlanContainer(NodeRef)}
     */
    @Override
    public boolean isRecordsManagementContainer(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanContainer(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Override
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlan(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isRecordCategory(NodeRef)}
     */
    @Override
    public boolean isRecordCategory(NodeRef nodeRef)
    {
        return getFilePlanService().isRecordCategory(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolder(NodeRef)}
     */
    @Override
    public boolean isRecordFolder(NodeRef nodeRef)
    {
        return getRecordFolderService().isRecordFolder(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link TransferService#isTransfer(NodeRef)}
     */
    @Override
    public boolean isTransfer(NodeRef nodeRef)
    {
        return getTransferService().isTransfer(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordService#isMetadataStub(NodeRef)}
     */
    @Override
    public boolean isMetadataStub(NodeRef nodeRef)
    {
        return getRecordService().isMetadataStub(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link DispositionService#isCutoff(NodeRef)}
     */
    @Override
    public boolean isCutoff(NodeRef nodeRef)
    {
        return getDispositionService().isCutoff(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getNodeRefPath(NodeRef)}
     */
    @Override
    public List<NodeRef> getNodeRefPath(NodeRef nodeRef)
    {
        return getFilePlanService().getNodeRefPath(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlan(NodeRef)}
     */
    @Override
    public NodeRef getFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().getFilePlan(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlans()}
     */
    @Override
    public List<NodeRef> getFilePlans()
    {
        return new ArrayList<NodeRef>(getFilePlanService().getFilePlans());
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createFilePlan(parent, name, type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, type, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name)
    {
        return getFilePlanService().createFilePlan(parent, name);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, Map)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container, boolean deep)
    {
        return getFilePlanService().getAllContained(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef)}
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container)
    {
        return getFilePlanService().getAllContained(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordCategories(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef)}
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container)
    {
        return getFilePlanService().getContainedRecordCategories(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordFolders(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef)}
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container)
    {
        return getFilePlanService().getContainedRecordFolders(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createRecordCategory(parent, name, type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, type, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name)
    {
        return getFilePlanService().createRecordCategory(parent, name);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, Map)}
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderDeclared(NodeRef)}
     */
    @Override
    public boolean isRecordFolderDeclared(NodeRef recordFolder)
    {
        return getRecordFolderService().isRecordFolderDeclared(recordFolder);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderClosed(NodeRef)}
     */
    @Override
    public boolean isRecordFolderClosed(NodeRef nodeRef)
    {
        return getRecordFolderService().isRecordFolderClosed(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef parent, String name, QName type)
    {
        return getRecordFolderService().createRecordFolder(parent, name, type);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties)
    {
        return getRecordFolderService().createRecordFolder(rmContainer, name, type, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContrainer, String name)
    {
        return getRecordFolderService().createRecordFolder(rmContrainer, name);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, Map)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef parent, String name,  Map<QName, Serializable> properties)
    {
        return getRecordFolderService().createRecordFolder(parent, name, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordService#getRecords(NodeRef)}
     */
    @Override
    public List<NodeRef> getRecords(NodeRef recordFolder)
    {
        return getRecordService().getRecords(recordFolder);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#getRecordFolders(NodeRef)}
     */
    @Override
    public List<NodeRef> getRecordFolders(NodeRef record)
    {
        return getRecordFolderService().getRecordFolders(record);
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#getRecordMetaDataAspects()}
     */
    @Override
    public Set<QName> getRecordMetaDataAspects()
    {
        return getRecordService().getRecordMetaDataAspects();
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#isDeclared(NodeRef)}
     */
    @Override
    public boolean isRecordDeclared(NodeRef nodeRef)
    {
        return getRecordService().isDeclared(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#isHold(NodeRef)}
     */
    @Override
    public boolean isHold(NodeRef nodeRef)
    {
        return getFreezeService().isHold(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#isFrozen(NodeRef)}
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        return getFreezeService().isFrozen(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#hasFrozenChildren(NodeRef)}
     */
    @Override
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        return getFreezeService().hasFrozenChildren(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#isRecord(NodeRef)}
     */
    @Override
    public boolean isRecord(NodeRef nodeRef)
    {
        return getRecordService().isRecord(nodeRef);
    }
}
