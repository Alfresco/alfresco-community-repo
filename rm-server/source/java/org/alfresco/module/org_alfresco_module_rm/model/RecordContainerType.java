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
package org.alfresco.module.org_alfresco_module_rm.model;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with the record container type
 * 
 * @author Roy Wetherall
 */
public class RecordContainerType implements RecordsManagementModel,
                                            NodeServicePolicies.OnCreateChildAssociationPolicy,
                                            NodeServicePolicies.OnCreateNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Records Management Action Service */
    private RecordsManagementActionService recordsManagementActionService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Identity service */
    private IdentifierService recordsManagementIdentifierService;
    
    /**
     * Set the policy component
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the records management action service
     * 
     * @param recordsManagementActionService  records management action service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }
    
    /**
     * Set node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set dictionary service
     * 
     * @param dictionaryService dictionary serviceS
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set the identity service
     * 
     * @param recordsManagementIdentifierService  identity service
     */
    public void setRecordsManagementIdentifierService(IdentifierService recordsManagementIdentifierService)
    {
        this.recordsManagementIdentifierService = recordsManagementIdentifierService;
    }
    
    /**
     * Bean initialisation method
     */
    public void init()
    {
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), 
                TYPE_RECORDS_MANAGEMENT_CONTAINER, 
                ContentModel.ASSOC_CONTAINS, 
                new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME, 
                TYPE_FILE_PLAN, 
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }   
    
    /**
     * Deal with something created within a record container
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {   
        // Get the elements of the created association
        final NodeRef child = childAssocRef.getChildRef();
        QName childType = nodeService.getType(child);
        
        // We only care about "folder" or sub-types
        if (dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER) == true)
        {       
            // We need to automatically cast the created folder to RM type if it is a plain folder
            // This occurs if the RM folder has been created via IMap, WebDav, etc
            if (nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT) == false)
            {                
                // TODO it may not always be a record folder ... perhaps if the current user is a admin it would be a record category?? 
                
                // Assume any created folder is a rma:recordFolder
                nodeService.setType(child, TYPE_RECORD_FOLDER);     
            }
                            
            if (TYPE_RECORD_FOLDER.equals(nodeService.getType(child)) == true)
            {            
                // Setup record folder
                recordsManagementActionService.executeRecordsManagementAction(child, "setupRecordFolder"); 
            }

            // Catch all to generate the rm id (assuming it doesn't already have one!)
            setIdenifierProperty(child);
            
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        // When a new root container is created, make sure the identifier is set
        setIdenifierProperty(childAssocRef.getChildRef());
    }
    
    /**
     * 
     * @param nodeRef
     */
    private void setIdenifierProperty(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception 
            {
                if (nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT) == true && 
                    nodeService.getProperty(nodeRef, PROP_IDENTIFIER) == null)
                {
                    String id = recordsManagementIdentifierService.generateIdentifier(nodeRef);                    
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_IDENTIFIER, id);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
