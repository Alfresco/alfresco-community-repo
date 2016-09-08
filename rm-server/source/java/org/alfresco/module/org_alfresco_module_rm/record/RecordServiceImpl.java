/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.record;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelAccessDeniedException;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordServiceImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.impl.ExtendedPermissionService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Record service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordServiceImpl extends ServiceBaseImpl
 							   implements RecordService,
                                          RecordsManagementModel,
                                          RecordsManagementCustomModel,
                                          NodeServicePolicies.OnCreateChildAssociationPolicy,
                                          NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          NodeServicePolicies.OnAddAspectPolicy,
                                          NodeServicePolicies.OnRemoveAspectPolicy
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordServiceImpl.class);
    
    /** Always edit property array */
    private static final QName[] ALWAYS_EDIT_PROPERTIES = new QName[]
    {
       ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA
    };
    
    private static final String[] ALWAYS_EDIT_URIS = new String[]
    {
        NamespaceService.SECURITY_MODEL_1_0_URI,
        NamespaceService.SYSTEM_MODEL_1_0_URI,
        NamespaceService.WORKFLOW_MODEL_1_0_URI,
        NamespaceService.APP_MODEL_1_0_URI,
        NamespaceService.DATALIST_MODEL_1_0_URI,
        NamespaceService.DICTIONARY_MODEL_1_0_URI,
        NamespaceService.BPM_MODEL_1_0_URI,
        NamespaceService.RENDITION_MODEL_1_0_URI
     }; 
    
    private static final String[] RECORD_MODEL_URIS = new String[]
    {
       RM_URI,
       RM_CUSTOM_URI,
       DOD5015Model.DOD_URI
    };
    
    private static final String[] NON_RECORD_MODEL_URIS = new String[]
    {
        NamespaceService.AUDIO_MODEL_1_0_URI,
        NamespaceService.CONTENT_MODEL_1_0_URI,
        NamespaceService.EMAILSERVER_MODEL_URI,
        NamespaceService.EXIF_MODEL_1_0_URI,
        NamespaceService.FORUMS_MODEL_1_0_URI,
        NamespaceService.LINKS_MODEL_1_0_URI,
        NamespaceService.REPOSITORY_VIEW_1_0_URI

    };

    /** Node service **/
    private NodeService nodeService;

    /** Indentity service */
    private IdentifierService identifierService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Permission service */
    private ExtendedPermissionService permissionService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /** Records management service */
    private RecordsManagementService recordsManagementService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Records management notification helper */
    private RecordsManagementNotificationHelper notificationHelper;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Ownable service */
    private OwnableService ownableService;
    
    /** Capability service */
    private CapabilityService capabilityService;
    
    /** Rule service */
    private RuleService ruleService;
    
    /** File folder service */
    private FileFolderService fileFolderService;
    
    /** Behaviour filter */
    @SuppressWarnings("unused")
    private BehaviourFilter behaviourFilter;

    /** List of available record meta-data aspects */
    private Set<QName> recordMetaDataAspects;    

    /** Behaviours */
    private JavaBehaviour onCreateChildAssociation = new JavaBehaviour(
                                                            this,
                                                            "onCreateChildAssociation",
                                                            NotificationFrequency.FIRST_EVENT);
    private JavaBehaviour onUpdateProperties = new JavaBehaviour(
                                                            this,
                                                            "onUpdateProperties",
                                                            NotificationFrequency.EVERY_EVENT);
    private JavaBehaviour onDeleteDeclaredRecordLink = new JavaBehaviour(
                                                            this,
                                                            "onDeleteDeclaredRecordLink",
                                                            NotificationFrequency.FIRST_EVENT);

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(ExtendedPermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param notificationHelper notification helper
     */
    public void setNotificationHelper(RecordsManagementNotificationHelper notificationHelper)
    {
        this.notificationHelper = notificationHelper;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Init method
     */
    public void init()
    {
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                TYPE_RECORD_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                onCreateChildAssociation);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
                ASPECT_RECORD, 
                onUpdateProperties);
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.BeforeDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_FOLDER,
                ContentModel.ASSOC_CONTAINS, 
                onDeleteDeclaredRecordLink);
        
        // Handle the special case when we are dealing with new records that are being added via a file protocol
        policyComponent.bindClassBehaviour(
		        NodeServicePolicies.OnAddAspectPolicy.QNAME, 
		        ContentModel.ASPECT_NO_CONTENT, 
		        new JavaBehaviour(this, "onAddAspect", NotificationFrequency.EVERY_EVENT));
		policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME, 
                ContentModel.ASPECT_NO_CONTENT, 
                new JavaBehaviour(this, "onRemoveAspect", NotificationFrequency.EVERY_EVENT));
    }
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspect)
    {

        if (nodeService.hasAspect(nodeRef, ASPECT_RECORD) == true)
        {
            ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

            // Only switch name back to the format of "name (identifierId)" if content size is non-zero, else leave it as the original name to avoid CIFS shuffling issues.
            if (contentData != null && contentData.getSize() > 0)
            {
                switchNames(nodeRef);
            }
        }
        else
        {
            // check whether filling is pending aspect removal
            Set<NodeRef> pendingFilling = TransactionalResourceHelper.getSet("pendingFilling");
            if (pendingFilling.contains(nodeRef) == true)
            {
                file(nodeRef);
            }
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        switchNames(nodeRef);
    }
   
    /**
     * Helper method to switch the name of the record around.  Used to support record creation via
     * file protocols.
     * 
     * @param nodeRef	node reference (record)
     */
    private void switchNames(NodeRef nodeRef)
    {
    	try 
    	{
    		if (nodeService.hasAspect(nodeRef, ASPECT_RECORD) == true)
    		{
    			String origionalName =  (String)nodeService.getProperty(nodeRef, PROP_ORIGIONAL_NAME);
    			if (origionalName != null)
    			{
    				String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    				fileFolderService.rename(nodeRef, origionalName);
    				nodeService.setProperty(nodeRef, PROP_ORIGIONAL_NAME, name);
    			}
    		}
		} 
    	catch (FileExistsException e) 
		{
			if (logger.isDebugEnabled() == true)
			{
			    logger.debug(e.getMessage());
			}
		}
    	catch (InvalidNodeRefException e) 
		{
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(e.getMessage());
            }
		} 
    	catch (FileNotFoundException e) 
		{
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(e.getMessage());
            }
		}
    }

    /**
     * Behaviour executed when a new item is added to a record folder.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, final boolean bNew)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                onCreateChildAssociation.disable();
                try
                {
                    NodeRef nodeRef = childAssocRef.getChildRef();
                    if (nodeService.exists(nodeRef) == true  && 
                        nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) == false &&
                        nodeService.getType(nodeRef).equals(TYPE_RECORD_FOLDER) == false && 
                        nodeService.getType(nodeRef).equals(TYPE_RECORD_CATEGORY) == false)
                    {
                        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT) == true)
                        {
                            // we need to postpone filling until the NO_CONTENT aspect is removed
                            Set<NodeRef> pendingFilling = TransactionalResourceHelper.getSet("pendingFilling");
                            pendingFilling.add(nodeRef);
                        }
                        else
                        {
                            // create and file the content as a record
                            file(nodeRef);
                        }
                    }
                }
                catch (AlfrescoRuntimeException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    onCreateChildAssociation.enable();
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Ensure that the user only updates record properties that they have permission to.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        onUpdateProperties.disable();
        try
        {
            if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
                AuthenticationUtil.isRunAsUserTheSystemUser() == false &&
                nodeService.exists(nodeRef) == true)
            {
                if (isRecord(nodeRef) == true)
                {
                    for (QName property : after.keySet())
                    {
                        Serializable beforeValue = null;
                        if (before != null)
                        {
                            beforeValue = before.get(property);
                        }
                        
                        Serializable afterValue = null;
                        if (after != null)
                        {
                            afterValue = after.get(property);
                        }
                        
                        boolean propertyUnchanged = false;
                        if (beforeValue != null && afterValue != null && 
                            beforeValue instanceof Date && afterValue instanceof Date)
                        {
                            // deal with date values
                            propertyUnchanged = (((Date)beforeValue).compareTo((Date)afterValue) == 0);
                        }
                        else
                        {
                            // otherwise
                            propertyUnchanged = EqualsHelper.nullSafeEquals(beforeValue, afterValue);
                        }
    
                        if (propertyUnchanged == false &&
                            isPropertyEditable(nodeRef, property) == false)
                        {
                            // the user can't edit the record property
                            throw new ModelAccessDeniedException(
                                "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                                " does not have the permission to edit the record property " + property.toString() +
                                " on the node " + nodeRef.toString());
                        }
                    }
                }
            }
        }
        finally
        {
            onUpdateProperties.enable();
        }
    }
    
    /**
     * Looking specifically at linked content that was declared a record from a non-rm site.
     * When the site or the folder that the link was declared in is deleted we need to remove 
     * the extended security property accounts in the tree
     * 
     * @param childAssocRef
     */
    public void onDeleteDeclaredRecordLink(ChildAssociationRef childAssocRef)
    {
        // Is the deleted child association not a primary association?
        // Does the deleted child association have the rma:recordOriginatingDetails aspect?
        // Is the parent of the deleted child association a folder (cm:folder)?
        if (!childAssocRef.isPrimary() && 
            nodeService.hasAspect(childAssocRef.getChildRef(), ASPECT_RECORD_ORIGINATING_DETAILS) && 
            nodeService.getType(childAssocRef.getParentRef()).equals(ContentModel.TYPE_FOLDER))
        {
            // ..then remove the extended readers and writers up the tree for this remaining node
            extendedSecurityService.removeExtendedSecurity(childAssocRef.getChildRef(), extendedSecurityService.getExtendedReaders(childAssocRef.getChildRef()), extendedSecurityService.getExtendedWriters(childAssocRef.getChildRef()), true);
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#disablePropertyEditableCheck()
     */
    @Override
    public void disablePropertyEditableCheck()
    {
        onUpdateProperties.disable();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#enablePropertyEditableCheck()
     */
    @Override
    public void enablePropertyEditableCheck()
    {
        onUpdateProperties.enable();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#getRecordMetaDataAspects()
     */
    @Override
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
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void createRecord(NodeRef filePlan, NodeRef nodeRef)
    {
        createRecord(filePlan, nodeRef, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final boolean isLinked)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("isLinked", isLinked);
        
        // first we do a sanity check to ensure that the user has at least write permissions on the document
        if (permissionService.hasPermission(nodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED)
        {
            throw new AccessDeniedException("Can not create record from document, because the user " +
                                            AuthenticationUtil.getFullyAuthenticatedUser() +
                                            " does not have Write permissions on the doucment " +
                                            nodeRef.toString());
        }

        // Save the id of the currently logged in user
        final String userId = AuthenticationUtil.getRunAsUser();

        // do the work of creating the record as the system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.hasAspect(nodeRef, ASPECT_RECORD) == false)
                {                    
                    // disable delete rules
                    ruleService.disableRuleType("outbound");
                    try
                    {
                        // get the new record container for the file plan
                        NodeRef newRecordContainer = filePlanService.getUnfiledContainer(filePlan);
                        if (newRecordContainer == null)
                        {
                            throw new AlfrescoRuntimeException("Unable to create record, because new record container could not be found.");
                        }
    
                        // get the documents readers
                        Long aclId = nodeService.getNodeAclId(nodeRef);
                        Set<String> readers = permissionService.getReaders(aclId);
                        Set<String> writers = permissionService.getWriters(aclId);
    
                        // add the current owner to the list of extended writers
                        String owner = ownableService.getOwner(nodeRef);
    
                        // remove the owner
                        ownableService.setOwner(nodeRef, OwnableService.NO_OWNER);
    
                        // get the documents primary parent assoc
                        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
    
                        // move the document into the file plan
                        nodeService.moveNode(nodeRef, newRecordContainer, ContentModel.ASSOC_CONTAINS, parentAssoc.getQName());
    
                        // save the information about the originating details
                        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(3);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_LOCATION, (Serializable) parentAssoc.getParentRef());
                        aspectProperties.put(PROP_RECORD_ORIGINATING_USER_ID, userId);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_CREATION_DATE, new Date());
                        nodeService.addAspect(nodeRef, ASPECT_RECORD_ORIGINATING_DETAILS, aspectProperties);
    
                        // make the document a record
                        makeRecord(nodeRef);
    
                        if (isLinked == true)
                        {
                            // turn off rules
                            ruleService.disableRules();
                            try
                            {
                                // maintain the original primary location
                                nodeService.addChild(parentAssoc.getParentRef(), nodeRef, parentAssoc.getTypeQName(), parentAssoc.getQName());
        
                                // set the extended security
                                Set<String> combinedWriters = new HashSet<String>(writers);
                                combinedWriters.add(owner);
                                combinedWriters.add(AuthenticationUtil.getFullyAuthenticatedUser());
                                
                                extendedSecurityService.addExtendedSecurity(nodeRef, readers, combinedWriters);
                            }
                            finally
                            {
                                ruleService.enableRules();
                            }
                        }
                    }
                    finally
                    {
                        ruleService.enableRuleType("outbound");
                    }
                }

                return null;
            }
        });
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map, org.alfresco.service.cmr.repository.ContentReader)
     */
    @Override
    public NodeRef createRecord(NodeRef filePlan, String name, QName type, Map<QName, Serializable> properties, ContentReader reader)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("name", name);
        
        // if none set the default record type is cm:content
        if (type == null)
        {
            type = ContentModel.TYPE_CONTENT;
        }
        // TODO ensure that the type is a sub-type of cm:content
        
        // get the unfiled record container for the file plan
        NodeRef unfiledContainer = filePlanService.getUnfiledContainer(filePlan);
        if (unfiledContainer == null)
        {
            throw new AlfrescoRuntimeException("Unable to create record, because unfiled container could not be found.");
        }
        
        // create the new record
        NodeRef record = fileFolderService.create(unfiledContainer, name, type).getNodeRef();
        
        // set the properties
        if (properties != null)
        {
            nodeService.addProperties(record, properties);
        }
        
        // set the content
        if (reader != null)
        {
            ContentWriter writer = fileFolderService.getWriter(record);
            writer.setEncoding(reader.getEncoding());
            writer.setMimetype(reader.getMimetype());
            writer.putContent(reader);
        }
        
        // make record
        makeRecord(record);
        
        return record;
    }

    /**
     * Creates a record from the given document
     *
     * @param document the document from which a record will be created
     */
    private void makeRecord(NodeRef document)
    {
        ruleService.disableRules();
        try
        {
        	// get the record id
            String recordId = identifierService.generateIdentifier(ASPECT_RECORD, 
                                                                   nodeService.getPrimaryParent(document).getParentRef());
            
            // get the record name
            String name = (String)nodeService.getProperty(document, ContentModel.PROP_NAME);
                        
            // rename the record
            int dotIndex = name.lastIndexOf(".");
            String prefix = name;
            String postfix = "";
            if (dotIndex != -1)
            {
                prefix = name.substring(0, dotIndex);
                postfix = name.substring(dotIndex);
            }            
            String recordName = prefix + " (" + recordId + ")" + postfix;
            fileFolderService.rename(document, recordName);
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Rename " + name + " to " + recordName);
            }
            
            // add the record aspect
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(PROP_IDENTIFIER, recordId);
            props.put(PROP_ORIGIONAL_NAME, name);
            nodeService.addAspect(document, RecordsManagementModel.ASPECT_RECORD, props);     
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Unable to make record, because rename failed.", e);
        }
        finally
        {
            ruleService.enableRules();
        }
        
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposableitem.RecordService#isFiled(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFiled(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean result = false;

        if (isRecord(nodeRef) == true)
        {
            ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);
            if (childAssocRef != null)
            {
                NodeRef parent = childAssocRef.getParentRef();
                if (parent != null &&
                    recordsManagementService.isRecordFolder(parent) == true)
                {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Helper method to 'file' a new document that arrived in the file plan structure.
     *
     *  TODO atm we only 'file' content as a record .. may need to consider other types if we
     *       are to support the notion of composite records.
     *
     * @param record node reference to record (or soon to be record!)
     */
    @Override
    public void file(NodeRef record)
    {
        ParameterCheck.mandatory("item", record);

        // we only support filling of content items
        // TODO composite record support needs to file containers too
        QName type = nodeService.getType(record);
        if (ContentModel.TYPE_CONTENT.equals(type) == true ||
            dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) == true)
        {
            // check whether this item is already an item or not
            if (isRecord(record) == false)
            {
                // make the item a record
                makeRecord(record);
            }

            // set filed date
            if (nodeService.getProperty(record, PROP_DATE_FILED) == null)
            {
                Calendar fileCalendar = Calendar.getInstance();
                nodeService.setProperty(record, PROP_DATE_FILED, fileCalendar.getTime());
            }

            // initialise vital record details
            // TODO .. change this to add the aspect which will trigger the init behaviour
            VitalRecordServiceImpl vitalRecordService = (VitalRecordServiceImpl)applicationContext.getBean("vitalRecordService");
            vitalRecordService.initialiseVitalRecord(record);

            // initialise disposition details
            if (nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE) == false)
            {
                DispositionSchedule di = dispositionService.getDispositionSchedule(record);
                if (di != null && di.isRecordLevelDisposition() == true)
                {
                    nodeService.addAspect(record, ASPECT_DISPOSITION_LIFECYCLE, null);
                }
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#hideRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void hideRecord(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("NodeRef", nodeRef);

        // do the work of hiding the record as the system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // remove the child association
                NodeRef originatingLocation = (NodeRef) nodeService.getProperty(nodeRef, PROP_RECORD_ORIGINATING_LOCATION);
                List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef childAssociationRef : parentAssocs)
                {
                    if (childAssociationRef.isPrimary() == false && childAssociationRef.getParentRef().equals(originatingLocation))
                    {
                        nodeService.removeChildAssociation(childAssociationRef);
                        break;
                    }
                }

                // remove the extended security from the node
                // this prevents the users from continuing to see the record in searchs and other linked locations
                extendedSecurityService.removeAllExtendedSecurity(nodeRef);

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#rejectRecord(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void rejectRecord(final NodeRef nodeRef, final String reason)
    {
        ParameterCheck.mandatory("NodeRef", nodeRef);
        ParameterCheck.mandatoryString("Reason", reason);

        // Save the id of the currently logged in user
        final String userId = AuthenticationUtil.getFullyAuthenticatedUser();

        // do the work of rejecting the record as the system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                ruleService.disableRules();
                try
                {                
                    // get record property values
                    Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
                    String recordId = (String)properties.get(PROP_IDENTIFIER);                    
                    String documentOwner = (String)properties.get(PROP_RECORD_ORIGINATING_USER_ID);
                    String origionalName = (String)properties.get(PROP_ORIGIONAL_NAME);
                    NodeRef originatingLocation = (NodeRef)properties.get(PROP_RECORD_ORIGINATING_LOCATION);
                    
                    // first remove the secondary link association
                    List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
                    for (ChildAssociationRef childAssociationRef : parentAssocs)
                    {
                        if (childAssociationRef.isPrimary() == false && childAssociationRef.getParentRef().equals(originatingLocation))
                        {
                            nodeService.removeChildAssociation(childAssociationRef);
                            break;
                        }
                    }
                    
                    // remove all RM related aspects from the node
                    Set<QName> aspects = nodeService.getAspects(nodeRef);
                    for (QName aspect : aspects)
                    {
                        if (RM_URI.equals(aspect.getNamespaceURI()) == true)
                        {
                            // remove the aspect
                            nodeService.removeAspect(nodeRef, aspect);
                        }
                    }
                    
                    // get the records primary parent association
                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
    
                    // move the record into the collaboration site
                    nodeService.moveNode(nodeRef, originatingLocation, ContentModel.ASSOC_CONTAINS, parentAssoc.getQName());
                   
                    // rename to the origional name
                    if (origionalName != null)
                    {
                        fileFolderService.rename(nodeRef, origionalName);
                        
                        if (logger.isDebugEnabled() == true)
                        {
                            String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                            logger.debug("Rename " + name + " to " + origionalName);
                        }
                    }
                    
                    // save the information about the rejection details
                    Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(3);
                    aspectProperties.put(PROP_RECORD_REJECTION_USER_ID, userId);
                    aspectProperties.put(PROP_RECORD_REJECTION_DATE, new Date());
                    aspectProperties.put(PROP_RECORD_REJECTION_REASON, reason);
                    nodeService.addAspect(nodeRef, ASPECT_RECORD_REJECTION_DETAILS, aspectProperties);
    
                    // Restore the owner of the document                
                    if (StringUtils.isBlank(documentOwner))
                    {
                        throw new AlfrescoRuntimeException("Unable to find the creator of document.");
                    }
                    ownableService.setOwner(nodeRef, documentOwner);
    
                    // send an email to the record creator
                    notificationHelper.recordRejectedEmailNotification(nodeRef, recordId, documentOwner);
                }
                finally
                {
                    ruleService.enableRules();
                }

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isPropertyEditable(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isPropertyEditable(NodeRef record, QName property)
    {
        ParameterCheck.mandatory("record", record);
        ParameterCheck.mandatory("property", property);
        
        if (isRecord(record) == false)
        {
            throw new AlfrescoRuntimeException("Can not check if the property " + property.toString() + " is editable, because node reference is not a record.");
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Checking whether property " + property.toString() + " is editable for user " + AuthenticationUtil.getRunAsUser());
        }
        
        // DEBUG ...
        FilePlanService fps = (FilePlanService)applicationContext.getBean("filePlanService");
        FilePlanRoleService fprs = (FilePlanRoleService)applicationContext.getBean("filePlanRoleService");
        PermissionService ps = (PermissionService)applicationContext.getBean("permissionService");
        
        NodeRef filePlan = fps.getFilePlan(record); 
        Set<Role> roles = fprs.getRolesByUser(filePlan, AuthenticationUtil.getRunAsUser());
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... users roles");
        }
        
        for (Role role : roles)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("     ... user has role " + role.getName() + " with capabilities ");
            }
            
            for (Capability cap : role.getCapabilities())
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("         ... " + cap.getName());
                }
            }       
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(" ... user has the following set permissions on the file plan");
        }
        Set<AccessPermission> perms = ps.getAllSetPermissions(filePlan);
        for (AccessPermission perm : perms)
        {
            if (logger.isDebugEnabled() == true && 
                (perm.getPermission().contains(RMPermissionModel.EDIT_NON_RECORD_METADATA) ||
                 perm.getPermission().contains(RMPermissionModel.EDIT_RECORD_METADATA)))
            {
                logger.debug("     ... " + perm.getAuthority() + " - " + perm.getPermission() + " - " + perm.getAccessStatus().toString());
            }           
        }
        
        if (ps.hasPermission(filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA).equals(AccessStatus.ALLOWED))
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" ... user has the edit non record metadata permission on the file plan");
            }
        }
        
        // END DEBUG ...
        
        boolean result = alwaysEditProperty(property);
        if (result == true)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" ... property marked as always editable.");
            }
        }
        else
        {
            boolean allowRecordEdit = false;
            boolean allowNonRecordEdit = false;

            AccessStatus accessNonRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_NON_RECORD_METADATA);
            AccessStatus accessDeclaredRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA);
            AccessStatus accessRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_RECORD_METADATA);
            
            if (AccessStatus.ALLOWED.equals(accessNonRecord) == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... user has edit nonrecord metadata capability");
                }
                
                allowNonRecordEdit = true;
            }
            
            if (AccessStatus.ALLOWED.equals(accessRecord) == true ||
                AccessStatus.ALLOWED.equals(accessDeclaredRecord) == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... user has edit record or declared metadata capability");
                }
                
                allowRecordEdit = true;
            }
            
            if (allowNonRecordEdit == true && allowRecordEdit == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... so all properties can be edited.");
                }
                
                result = true;
            }
            else if (allowNonRecordEdit == true && allowRecordEdit == false)
            {
                // can only edit non record properties
                if (isRecordMetadata(property) == false)                    
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(" ... property is not considered record metadata so editable.");
                    }
                    
                    result = true;
                }
                else
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(" ... property is considered record metadata so not editable.");
                    }
                }
            }
            else if (allowNonRecordEdit == false && allowRecordEdit == true)
            {
                // can only edit record properties
                if (isRecordMetadata(property) == true)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(" ... property is considered record metadata so editable.");
                    }
                    
                    result = true;
                }  
                else
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(" ... property is not considered record metadata so not editable.");
                    }
                }
            }
            // otherwise we can't edit any properties so just return the empty set
        }
        return result;
    }
    
    /**
     * Helper method that indicates whether a property is considered record metadata or not.
     * 
     * @param property  property 
     * @return boolea   true if record metadata, false otherwise
     */
    private boolean isRecordMetadata(QName property)
    {
        boolean result = ArrayUtils.contains(RECORD_MODEL_URIS, property.getNamespaceURI());
        
        if (result == false && ArrayUtils.contains(NON_RECORD_MODEL_URIS, property.getNamespaceURI()) == false)
        {
            PropertyDefinition def = dictionaryService.getProperty(property);
            if (def != null)
            {
                ClassDefinition parent = def.getContainerClass();
                if (parent != null && parent.isAspect() == true)
                {
                    result = getRecordMetaDataAspects().contains(parent.getName());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Determines whether the property should always be allowed to be edited or not.
     * 
     * @param property
     * @return
     */
    private boolean alwaysEditProperty(QName property)
    {
        return (ArrayUtils.contains(ALWAYS_EDIT_URIS, property.getNamespaceURI()) ||
                ArrayUtils.contains(ALWAYS_EDIT_PROPERTIES, property) ||
                isProtectedProperty(property));
    }
    
    /**
     * Helper method to determine whether a property is protected at a dictionary definition
     * level.
     * 
     * @param property  property qualified name
     * @return booelan  true if protected, false otherwise
     */
    private boolean isProtectedProperty(QName property)
    {
        boolean result = false;        
        PropertyDefinition def = dictionaryService.getProperty(property);
        if (def != null)
        {
            result = def.isProtected();
        }        
        return result;
    }
}
