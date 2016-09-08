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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Records management service implementation.
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementServiceImpl implements RecordsManagementService,
                                                     RecordsManagementModel,
                                                     RecordsManagementPolicies.OnCreateReference,
                                                     RecordsManagementPolicies.OnRemoveReference
{
    /** I18N */
    private final static String MSG_ERROR_ADD_CONTENT_CONTAINER = "rm.service.error-add-content-container";
    private final static String MSG_UPDATE_DISP_ACT_DEF = "rm.service.update-disposition-action-def";
    private final static String MSG_SET_ID = "rm.service.set-id";
    private final static String MSG_PATH_NODE = "rm.service.path-node";
    private final static String MSG_INVALID_RM_NODE = "rm.service.invalid-rm-node";
    private final static String MSG_NO_ROOT = "rm.service.no-root";
    private final static String MSG_DUP_ROOT = "rm.service.dup-root";
    private final static String MSG_ROOT_TYPE = "rm.service.root-type";
    private final static String MSG_CONTAINER_PARENT_TYPE= "rm.service.container-parent-type";
    private final static String MSG_CONTAINER_TYPE = "rm.service.container-type";
    private final static String MSG_CONTAINER_EXPECTED = "rm.service.container-expected";
    private final static String MSG_RECORD_FOLDER_EXPECTED = "rm.service.record-folder-expected";
    private final static String MSG_PARENT_RECORD_FOLDER_ROOT = "rm.service.parent-record-folder-root";
    private final static String MSG_PARENT_RECORD_FOLDER_TYPE = "rm.service.parent-record-folder-type";
    private final static String MSG_RECORD_FOLDER_TYPE = "rm.service.record-folder-type";
    private final static String MSG_NOT_RECORD = "rm.service.not-record";
    
    /** Store that the RM roots are contained within */
    @SuppressWarnings("unused")
    @Deprecated
    private StoreRef defaultStoreRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    /** Service registry */
    private RecordsManagementServiceRegistry serviceRegistry;
    
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Node DAO */
    private NodeDAO nodeDAO;

    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Records management action service */
    private RecordsManagementActionService rmActionService;

    /** Well-known location of the scripts folder. */
    private NodeRef scriptsFolderNodeRef = new NodeRef("workspace", "SpacesStore", "rm_scripts");
    
    /** List of available record meta-data aspects */
    private Set<QName> recordMetaDataAspects;
    
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
     * Set search service
     * 
     * @param nodeService   search service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the node DAO object
     * 
     * @param nodeDAO   node DAO
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    /**
     * Set records management action service
     * 
     * @param rmActionService   records management action service
     */
    public void setRmActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
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
     * Init method.  Registered behaviours.
     */
    public void init()
    {        
        // Register the association behaviours
        policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), 
                TYPE_RECORD_FOLDER, 
                ContentModel.ASSOC_CONTAINS,
                new JavaBehaviour(this, "onFileContent", NotificationFrequency.TRANSACTION_COMMIT));
        
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
     * Try to file any record created in a record folder
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    public void onFileContent(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        if (nodeService.exists(nodeRef) == true)
        {
            // Ensure that the filed item is cm:content
            QName type = nodeService.getType(nodeRef);
            if (ContentModel.TYPE_CONTENT.equals(type) == true ||
                dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) == true)
            {
                // File the document
                rmActionService.executeRecordsManagementAction(childAssocRef.getChildRef(), "file");
            }
            else
            {
                // Raise an exception since we should only be filling content into a record folder
                throw new AlfrescoRuntimeException("Unable to complete operation, because only content can be filed within a record folder.");
            }        
        }
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
    public void onAddRecordThumbnail(ChildAssociationRef childAssocRef, boolean bNew)
    {
        NodeRef thumbnail = childAssocRef.getChildRef();
        if (nodeService.exists(thumbnail) == true)
        {
            nodeService.addAspect(thumbnail, ASPECT_FILE_PLAN_COMPONENT, null);
        }
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
    public void onChangeToAnyRmProperty(NodeRef node, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        if (nodeService.exists(node) == true)
        {
            this.lookupAndExecuteScripts(node, oldProps, newProps);
        }
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
     */
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_FILE_PLAN);
    }
    
    /**
     * Utility method to safely and quickly determine if a node is a type (or sub-type) of the one specified.
     */
    private boolean instanceOf(NodeRef nodeRef, QName ofClassName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("ofClassName", ofClassName);
        boolean result = false;
        if (nodeService.exists(nodeRef) == true &&
                (ofClassName.equals(nodeService.getType(nodeRef)) == true ||
                 dictionaryService.isSubClass(nodeService.getType(nodeRef), ofClassName) == true))            
        {
            result = true;
        }    
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isFilePlanComponent(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isFilePlanComponent(NodeRef nodeRef)
    {
        boolean result = false;
        if (nodeService.exists(nodeRef) == true &&
            nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) == true)
        {
            result = true;
        }
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getFilePlanComponentKind(org.alfresco.service.cmr.repository.NodeRef)
     */
    public FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef)
    {
        FilePlanComponentKind result = null;
        
        if (isFilePlanComponent(nodeRef) == true)
        {
            result = FilePlanComponentKind.FILE_PLAN_COMPONENT;
            
            if (isFilePlan(nodeRef) == true)
            {
                result = FilePlanComponentKind.FILE_PLAN;
            }
            else if (isRecordCategory(nodeRef) == true)
            {
                result = FilePlanComponentKind.RECORD_CATEGORY;
            }
            else if (isRecordFolder(nodeRef) == true)
            {
                result = FilePlanComponentKind.RECORD_FOLDER;
            }
            else if (isRecord(nodeRef) == true)
            {
                result = FilePlanComponentKind.RECORD;
            }
            else if (isHold(nodeRef) == true)
            {
                result = FilePlanComponentKind.HOLD;
            }
            else if (isTransfer(nodeRef) == true)
            {
                result = FilePlanComponentKind.TRANSFER;
            }
            else if (instanceOf(nodeRef, TYPE_DISPOSITION_SCHEDULE) == true || instanceOf(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION) == true)
            {
                result = FilePlanComponentKind.DISPOSITION_SCHEDULE;
            }
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getFilePlanComponentKindFromType(org.alfresco.service.namespace.QName)
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKindFromType(QName type)
    {
        FilePlanComponentKind result = null;
        
        if (ASPECT_FILE_PLAN_COMPONENT.equals(type) == true)
        {
            result = FilePlanComponentKind.FILE_PLAN_COMPONENT;
        }
        else if (dictionaryService.isSubClass(type, ASPECT_RECORD) == true)
        {
            result = FilePlanComponentKind.RECORD;
        }
        else if (dictionaryService.isSubClass(type, TYPE_FILE_PLAN) == true)
        {
            result = FilePlanComponentKind.FILE_PLAN;
        }
        else if (dictionaryService.isSubClass(type, TYPE_RECORD_CATEGORY) == true)
        {
            result = FilePlanComponentKind.RECORD_CATEGORY;
        }
        else if (dictionaryService.isSubClass(type, TYPE_RECORD_FOLDER) == true)
        {
            result = FilePlanComponentKind.RECORD_FOLDER;
        }
        else if (dictionaryService.isSubClass(type, TYPE_HOLD) == true)
        {
            result = FilePlanComponentKind.HOLD;
        }
        else if (dictionaryService.isSubClass(type, TYPE_TRANSFER) == true)
        {
            result = FilePlanComponentKind.TRANSFER;
        }
        else if (dictionaryService.isSubClass(type, TYPE_DISPOSITION_SCHEDULE) == true || 
                 dictionaryService.isSubClass(type, TYPE_DISPOSITION_ACTION_DEFINITION) == true)
        {
            result = FilePlanComponentKind.DISPOSITION_SCHEDULE;
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordCategory(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecordCategory(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_RECORD_CATEGORY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecordFolder(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecordFolder(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_RECORD_FOLDER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecord(NodeRef nodeRef)
    {
        return this.nodeService.hasAspect(nodeRef, ASPECT_RECORD);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isHold(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_HOLD);
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
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_FROZEN);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#hasFrozenChildren(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        boolean result = false;
        if (isFilePlanComponent(nodeRef) == true)
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                if (isFrozen(assoc.getChildRef()) == true)
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
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
     */
    public NodeRef getFilePlan(NodeRef nodeRef)
    {
       NodeRef result = null;
               
       if (nodeRef != null)
       {
            result = (NodeRef)nodeService.getProperty(nodeRef, PROP_ROOT_NODEREF);
            if (result == null)
            {
                if (instanceOf(nodeRef, TYPE_FILE_PLAN) == true)
                {
                    result = nodeRef;
                }
                else
                {
                    ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
                    if (parentAssocRef != null)
                    {
                        result = getFilePlan(parentAssocRef.getParentRef());
                    }
                }
            }
       }      
        
       return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getNodeRefPath(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getNodeRefPath(NodeRef nodeRef)
    {
        LinkedList<NodeRef> nodeRefPath = new LinkedList<NodeRef>();
        try
        {
            getNodeRefPathRecursive(nodeRef, nodeRefPath);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PATH_NODE, nodeRef), e);
        }
        return nodeRefPath;
    }
    
    /**
     * Helper method to build a <b>NodeRef</b> path from the node to the RM root
     */
    private void getNodeRefPathRecursive(NodeRef nodeRef, LinkedList<NodeRef> nodeRefPath)
    {
        if (isFilePlanComponent(nodeRef) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_INVALID_RM_NODE, ASPECT_FILE_PLAN_COMPONENT.toString()));
        }
        // Prepend it to the path
        nodeRefPath.addFirst(nodeRef);
        // Are we at the root
        if (isFilePlan(nodeRef) == true)
        {
            // We're done
        }
        else
        {
            ChildAssociationRef assocRef = nodeService.getPrimaryParent(nodeRef);
            if (assocRef == null)
            {
                // We hit the top of the store
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NO_ROOT));
            }
            // Recurse
            nodeRef = assocRef.getParentRef();
            getNodeRefPathRecursive(nodeRef, nodeRefPath);
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecordsManagementRoots(org.alfresco.service.cmr.repository.StoreRef)
     */
    public List<NodeRef> getFilePlans()
    {
        final List<NodeRef> results = new ArrayList<NodeRef>();
        Set<QName> aspects = new HashSet<QName>(1);
        aspects.add(ASPECT_RECORDS_MANAGEMENT_ROOT);
        nodeDAO.getNodesWithAspects(aspects, Long.MIN_VALUE, Long.MAX_VALUE, new NodeDAO.NodeRefQueryCallback()
        {            
            @Override
            public boolean handle(Pair<Long, NodeRef> nodePair)
            {
                NodeRef nodeRef = nodePair.getSecond();
                if (StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(nodeRef.getStoreRef()) == false)
                {                
                    results.add(nodeRef);
                }
                
                return true;
            }
        });
        return results;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("type", type);
        
        // Check the parent is not already an RM component node
        // ie: you can't create a rm root in an existing rm hierarchy
        if (isFilePlanComponent(parent) == true)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_DUP_ROOT));
        }
                
        // Check that the passed type is a sub-type of rma:filePlan
        if (TYPE_FILE_PLAN.equals(type) == false &&
            dictionaryService.isSubClass(type, TYPE_FILE_PLAN) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_ROOT_TYPE, type.toString()));
        }
        
        // Build map of properties
        Map<QName, Serializable> rmRootProps = new HashMap<QName, Serializable>(1);
        if (properties != null && properties.size() != 0)
        {
            rmRootProps.putAll(properties);
        }
        rmRootProps.put(ContentModel.PROP_NAME, name);
        
        // Create the root
        ChildAssociationRef assocRef = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                type,
                rmRootProps);
        
        // TODO do we need to create role and security groups or is this done automatically?
        
        return assocRef.getChildRef();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return createFilePlan(parent, name, TYPE_FILE_PLAN, properties);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createFilePlan(NodeRef parent, String name)
    {
        return createFilePlan(parent, name, TYPE_FILE_PLAN, null);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createFilePlan(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName)
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type)
    {
        return createFilePlan(parent, name, type, null);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("parent", parent);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("type", type);
        
        // Check that the parent is a container
        QName parentType = nodeService.getType(parent);
        if (TYPE_RECORDS_MANAGEMENT_CONTAINER.equals(parentType) == false &&
            dictionaryService.isSubClass(parentType, TYPE_RECORDS_MANAGEMENT_CONTAINER) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_PARENT_TYPE, parentType.toString()));
        }
        
        // Check that the the provided type is a sub-type of rm:recordCategory
        if (TYPE_RECORD_CATEGORY.equals(type) == false &&
            dictionaryService.isSubClass(type, TYPE_RECORD_CATEGORY) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_TYPE, type.toString()));
        }
        
        // Set the properties for the record category
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        if (properties != null && properties.size() != 0)
        {
            props.putAll(properties);
        }
        props.put(ContentModel.PROP_NAME, name);
        
        return nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                type,
                props).getChildRef();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name)
    {
        return createRecordCategory(parent, name, TYPE_RECORD_CATEGORY);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return createRecordCategory(parent, name, TYPE_RECORD_CATEGORY, properties);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#createRecordCategory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName)
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type)
    {
        return createRecordCategory(parent, name, type, null);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getAllContained(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container)
    {
        return getAllContained(container, false);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getAllContained(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container, boolean deep)
    {
        return getContained(container, null, deep);
    }
    
    /**
     * Get contained nodes of a particular type.  If null return all.
     * 
     * @param container container node reference
     * @param typeFilter type filter, null if none
     * @return {@link List}<{@link NodeRef> list of contained node references
     */
    private List<NodeRef> getContained(NodeRef container, QName typeFilter, boolean deep)
    {   
        // Parameter check
        ParameterCheck.mandatory("container", container);
        
        // Check we have a container in our hands
        if (isRecordCategory(container) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONTAINER_EXPECTED));
        }
        
        List<NodeRef> result = new ArrayList<NodeRef>(1);
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(container, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef child = assoc.getChildRef();
            QName childType = nodeService.getType(child);
            if (typeFilter == null ||
                typeFilter.equals(childType) == true ||
                dictionaryService.isSubClass(childType, typeFilter) == true)
            {
                result.add(child);
            }
            
            // Inspect the containers and add children if deep
            if (deep == true &&
                (TYPE_RECORD_CATEGORY.equals(childType) == true ||
                 dictionaryService.isSubClass(childType, TYPE_RECORD_CATEGORY) == true))
            {
                result.addAll(getContained(child, typeFilter, deep));
            }
        }
            
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getContainedRecordCategories(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container)
    {
        return getContainedRecordCategories(container, false);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getContainedRecordCategories(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container, boolean deep)
    {
        return getContained(container, TYPE_RECORD_CATEGORY, deep);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getContainedRecordFolders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container)
    {
        return getContainedRecordFolders(container, false);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getContainedRecordFolders(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep)
    {
        return getContained(container, TYPE_RECORD_FOLDER, deep);
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
            if (isRecordDeclared(record) == false)
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
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#getRecordMetaDataAspects()
     */
    public Set<QName> getRecordMetaDataAspects() 
    {
    	if (recordMetaDataAspects == null)
    	{
    	    recordMetaDataAspects = new HashSet<QName>(7);
    		Collection<QName> aspects = dictionaryService.getAllAspects();
    		for (QName aspect : aspects) 
    		{
    		    AspectDefinition def = dictionaryService.getAspect(aspect);
    		    if (def != null)
    		    {
    		        QName parent = def.getParentName();
    		        if (parent != null && ASPECT_RECORD_META_DATA.equals(parent) == true)
    		        {
    		            recordMetaDataAspects.add(aspect);
    		        }
    		    }
			}
    	}
    	return recordMetaDataAspects;
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
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementService#isRecord(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public boolean isRecordDeclared(NodeRef record)
    {
        if (isRecord(record) == false)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_RECORD, record.toString()));
        }
        return (this.nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
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

        Set<QName> changedProps = determineChangedProps(oldProps, newProps);
        for (QName propQName : changedProps)
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
}
