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
package org.alfresco.module.org_alfresco_module_rm.model.behaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderServiceImpl;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Class containing behaviour for the vitalRecordDefinition aspect.
 * 
 * @author neilm
 */
public class RecordCopyBehaviours implements RecordsManagementModel,
                                             ApplicationContextAware
{
    /** The policy component */
    private PolicyComponent policyComponent;
    
    /** The Behaviour Filter */
    private BehaviourFilter behaviourFilter;
    
    /** The rm service registry */
    private RecordsManagementServiceRegistry rmServiceRegistry;
    
    /** List of aspects to remove during move and copy */
    private List<QName> unwantedAspects = new ArrayList<QName>(5);
    
    /** Application context */
    private ApplicationContext applicationContext;

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
       this.applicationContext = applicationContext; 
    } 
    
    /**
     * Set the policy component
     * 
     * @param policyComponent   the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the behaviour Filter
     * 
     * @param behaviourFilter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * Set the rm service registry.
     * 
     * @param recordsManagementServiceRegistry   the rm service registry.
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry recordsManagementServiceRegistry)
    {
        this.rmServiceRegistry = recordsManagementServiceRegistry;
    }

    /**
     * Initialise the vitalRecord aspect policies
     */
    public void init()
    {
        // Set up list of unwanted aspects
        unwantedAspects.add(ASPECT_VITAL_RECORD);
        unwantedAspects.add(ASPECT_DISPOSITION_LIFECYCLE);
        unwantedAspects.add(RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH);
        
        // Do not copy any of the Alfresco-internal 'state' aspects
        for (QName aspect : unwantedAspects)
        {
            this.policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                    aspect,
                    new JavaBehaviour(this, "getDoNothingCopyCallback"));
        }
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ASPECT_RECORD_COMPONENT_ID,
                new JavaBehaviour(this, "getDoNothingCopyCallback"));
        
        //On Copy we need a new ID
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                ASPECT_RECORD_COMPONENT_ID,
                new JavaBehaviour(this, "generateId", NotificationFrequency.TRANSACTION_COMMIT));
        
        //Don't copy the Aspect Record -- it should be regenerated
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ASPECT_RECORD, 
                new JavaBehaviour(this, "onCopyRecord"));
        
        // Move behaviour 
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
                RecordsManagementModel.ASPECT_RECORD, 
                new JavaBehaviour(this, "onMoveRecordNode", NotificationFrequency.FIRST_EVENT));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
                RecordsManagementModel.TYPE_RECORD_FOLDER, 
                new JavaBehaviour(this, "onMoveRecordFolderNode", NotificationFrequency.FIRST_EVENT));
        
        //Copy Behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), 
                RecordsManagementModel.TYPE_RECORD_FOLDER, 
                new JavaBehaviour(this, "onCopyRecordFolderNode"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), 
                RecordsManagementModel.TYPE_RECORD_CATEGORY, 
                new JavaBehaviour(this, "onCopyRecordCategoryNode"));
    }
    
    /**
     * onMove record behaviour
     * 
     * @param oldChildAssocRef
     * @param newChildAssocRef
     */
    public void onMoveRecordNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // check the records parent has actually changed
        if (oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()) == false)
        {
            final NodeRef newNodeRef = newChildAssocRef.getChildRef();
            final NodeService nodeService = rmServiceRegistry.getNodeService();
            
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    if (nodeService.exists(newNodeRef) == true)
                    {
                        // only remove the search details .. the rest will be resolved automatically
                        nodeService.removeAspect(newNodeRef, RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH);
                    }
                    
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
    }
    
    /**
     * onMove record folder behaviour
     * 
     * @param oldChildAssocRef
     * @param newChildAssocRef
     */
    public void onMoveRecordFolderNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        final NodeService nodeService = rmServiceRegistry.getNodeService();
        
        if (!nodeService.getType(newChildAssocRef.getParentRef()).equals(TYPE_RECORD_FOLDER))
        {        
            if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
            {
                //final NodeRef oldNodeRef = oldChildAssocRef.getChildRef();
                final NodeRef newNodeRef = newChildAssocRef.getChildRef();
            
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        final RecordsManagementService rmService = rmServiceRegistry.getRecordsManagementService();
                        final RecordService rmRecordService = rmServiceRegistry.getRecordService();
                        final RecordFolderServiceImpl recordFolderService = (RecordFolderServiceImpl)applicationContext.getBean("recordFolderService");
                        final DispositionServiceImpl dispositionService = (DispositionServiceImpl)applicationContext.getBean("dispositionService");
                               
                        behaviourFilter.disableBehaviour();
                        try
                        {
                            // Remove unwanted aspects
                            removeUnwantedAspects(nodeService, newNodeRef);
                            
                            // reinitialise the record folder
                            recordFolderService.initialiseRecordFolder(newNodeRef);
                            
                            // reinitialise the record folder disposition action details
                            dispositionService.refreshDispositionAction(newNodeRef);
    
                            // Sort out the child records
                            for (NodeRef record : rmService.getRecords(newNodeRef))
                            {
                                // Remove unwanted aspects
                                removeUnwantedAspects(nodeService, record);
                                
                                // Re-initiate the records in the new folder.
                                rmRecordService.file(record);
                            }
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour();
                        }

                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot move record folder into another record folder.");
        }
    }
    
    /**
     * Handle the copying of the record aspect.
     * Excludes the Date Filed property.  The Date Filed will be generated on copy.
     * 
     * @param classRef
     * @param copyDetails
     * @return
     */
    public CopyBehaviourCallback onCopyRecord(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {

            @Override
            public Map<QName, Serializable> getCopyProperties(QName classRef, CopyDetails copyDetails,
                    Map<QName, Serializable> properties)
            {
                Map<QName, Serializable> sourceProperties = super.getCopyProperties(classRef, copyDetails, properties);
                
                // Remove the Date Filed property from record properties on copy.
                // It will be generated for the copy
                if (sourceProperties.containsKey(PROP_DATE_FILED))
                {
                    sourceProperties.remove(PROP_DATE_FILED);
                }

                return sourceProperties;
            }
            
        };
    }
    
    /**
     * Record Folder Copy Behaviour
     * 
     * <li> Do not allow copy of record folder into another record folder</li>
     * 
     * @param classRef
     * @param copyDetails
     * @return
     */
    public CopyBehaviourCallback onCopyRecordFolderNode(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {
            final NodeService nodeService = rmServiceRegistry.getNodeService();
            
            @Override
            public Map<QName, Serializable> getCopyProperties(QName classRef, CopyDetails copyDetails,  Map<QName, Serializable> properties)
            {
                Map<QName, Serializable> sourceProperties = super.getCopyProperties(classRef, copyDetails, properties);
                
                // ensure that the 'closed' status of the record folder is not copied
                if (sourceProperties.containsKey(PROP_IS_CLOSED))
                {
                    sourceProperties.remove(PROP_IS_CLOSED);
                }

                return sourceProperties;
            }
            
            
            /**
             * If the targets parent is a Record Folder -- Do Not Allow Copy
             * 
             * @param classQName
             * @param copyDetails
             * @return boolean
             */
            @Override
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                boolean result = true;
                
                if (nodeService.getType(copyDetails.getTargetParentNodeRef()).equals(TYPE_RECORD_FOLDER) == true)
                {
                    result = false;
                }
                else if (unwantedAspects.contains(classQName) == true)
                {
                    result = false;
                }
                
                return result;
            }
        };
    }
    
    /**
     * Record Category Copy Behaviour
     * 
     * <li> Do not allow copy of record category into a record folder</li>
     * 
     * @param classRef
     * @param copyDetails
     * @return
     */
    public CopyBehaviourCallback onCopyRecordCategoryNode(final QName classRef, final CopyDetails copyDetails)
    {
        return new DefaultCopyBehaviourCallback()
        {
            final NodeService nodeService = rmServiceRegistry.getNodeService();
            
            /**
             * If the targets parent is a Record Folder -- Do Not Allow Copy
             * 
             * @param classQName
             * @param copyDetails
             * @return boolean
             */
            @Override
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                return nodeService.getType(copyDetails.getTargetParentNodeRef()).equals(TYPE_RECORD_FOLDER) ? false : true;
            }
        };
    }
    
    /**
     * Removes unwanted aspects
     * 
     * @param nodeService
     * @param nodeRef
     */
    private void removeUnwantedAspects(NodeService nodeService, NodeRef nodeRef)
    {
        // Remove unwanted aspects
        for (QName aspect : unwantedAspects)
        {
            if (nodeService.hasAspect(nodeRef, aspect) == true)
            {
                nodeService.removeAspect(nodeRef, aspect);
            }
        }
    }
    
    /**
     * Get the "do nothing" call back behaviour
     * 
     * @param classRef
     * @param copyDetails
     * @return
     */
    public CopyBehaviourCallback getDoNothingCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new DoNothingCopyBehaviourCallback();
    }
    
    /**
     * Generate and set a new ID for copy of a record
     * 
     * @param classRef
     * @param sourceNodeRef
     * @param targetNodeRef
     * @param copyToNewNode
     * @param copyMap
     */
    @SuppressWarnings("rawtypes")
    public void generateId(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map copyMap)
    {
        final IdentifierService rmIdentifierService = rmServiceRegistry.getIdentifierService();
        final NodeService nodeService = rmServiceRegistry.getNodeService();
        
        //Generate the id for the copy
        String id = rmIdentifierService.generateIdentifier(
                                            nodeService.getType(nodeService.getPrimaryParent(targetNodeRef).getParentRef()), 
                                            (nodeService.getPrimaryParent(targetNodeRef).getParentRef()));
        
        //We need to allow the id to be overwritten disable the policy protecting changes to the id
        behaviourFilter.disableBehaviour();
        try
        {
            nodeService.setProperty(targetNodeRef, PROP_IDENTIFIER, id);
        }
        finally
        {
            behaviourFilter.enableBehaviour();
        }
    }
    
    /**
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s     String to pad with leading zero '0' characters
     * @param len   Length to pad to
     * 
     * @return padded string or the original if already at >=len characters 
     */
    protected String padString(String s, int len)
    {
       String result = s;
       for (int i=0; i<(len - s.length()); i++)
       {
           result = "0" + result;
       }
       return result;
    }
}
