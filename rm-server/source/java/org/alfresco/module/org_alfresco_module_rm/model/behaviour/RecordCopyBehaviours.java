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
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.copy.AbstractCopyBehaviourCallback;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class containing behaviour for the vitalRecordDefinition aspect.
 * 
 * @author neilm
 */
public class RecordCopyBehaviours implements RecordsManagementModel
{
    /** The policy component */
    private PolicyComponent policyComponent;
    
    /** The Behaviour Filter */
    private BehaviourFilter behaviourFilter;
    
    /** The rm service registry */
    private RecordsManagementServiceRegistry rmServiceRegistry;
    
    /** List of aspects to remove during move and copy */
    private List<QName> unwantedAspects = new ArrayList<QName>(5);
    
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
                new JavaBehaviour(this, "getDoNothingCopyCallback"));
        
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
                        // Remove unwanted aspects
                        removeUnwantedAspects(nodeService, newNodeRef);
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
    @SuppressWarnings("unused")
    public void onMoveRecordFolderNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        if (oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()) == false)
        {        
            final NodeRef newNodeRef = newChildAssocRef.getChildRef();
            final NodeService nodeService = rmServiceRegistry.getNodeService();
            final RecordsManagementService rmService = rmServiceRegistry.getRecordsManagementService();        
            final RecordsManagementActionService rmActionService = rmServiceRegistry.getRecordsManagementActionService();
            
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    if (nodeService.exists(newNodeRef) == true)
                    {
                        // Remove unwanted aspects
                        removeUnwantedAspects(nodeService, newNodeRef);
                        
                        // TODO .. this isn't right!!
                        //      .. what if the folder is in motion .. do we really want to lose all the disposition information?
                        //      .. how do we merge from one disposition schedule to another
                        //      .. for now throw unsupportedOperationException and refactor when considered
                        throw new UnsupportedOperationException("Moving a record folder is currently not supported.");
                        
    //                    // Trigger folder setup
    //                    rmActionService.executeRecordsManagementAction(newNodeRef, "setupRecordFolder");
    //                    
    //                    // Sort out the child records
    //                    for (NodeRef record : rmService.getRecords(newNodeRef))
    //                    {
    //                        removeUnwantedAspects(nodeService, record);
    //                        rmActionService.executeRecordsManagementAction(record, "file");
    //                    }
                    }
                    
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
    }
    
    public CopyBehaviourCallback onCopyRecordFolderNode(final QName classRef, final CopyDetails copyDetails)
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
        String id = rmIdentifierService.generateIdentifier(nodeService.getType(nodeService.getPrimaryParent(targetNodeRef).getParentRef()), (nodeService.getPrimaryParent(targetNodeRef).getParentRef()));
        
        //We need to allow the id to be overwritten disable the policy protecting changes to the id
        behaviourFilter.disableBehaviour(targetNodeRef, ASPECT_RECORD_COMPONENT_ID);
        nodeService.setProperty(targetNodeRef, PROP_IDENTIFIER, id);
        behaviourFilter.enableBehaviour(targetNodeRef, ASPECT_RECORD_COMPONENT_ID);
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
