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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderServiceImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
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
    
    /** Node service */
    private NodeService nodeService;
    
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Identity service */
    private IdentifierService recordsManagementIdentifierService;
    
    /** record folder service */
    private RecordFolderServiceImpl recordFolderService;
    
    /**
     * Set the policy component
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
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
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderServiceImpl recordFolderService)
    {
        this.recordFolderService = recordFolderService;
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
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, boolean isNewNode)
    {   
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Get the elements of the created association
                final NodeRef child = childAssocRef.getChildRef();
                if (nodeService.exists(child) == true)
                {
                    QName childType = nodeService.getType(child);
                    
                    // We only care about "folder" or sub-types
                    if (dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER) == true)
                    {       
                        if (dictionaryService.isSubClass(childType, ContentModel.TYPE_SYSTEM_FOLDER) == true)
                        {
                            // this is a rule container, make sure it is an file plan component
                            nodeService.addAspect(child, ASPECT_FILE_PLAN_COMPONENT, null);
                        }
                        else
                        {                
                            // We need to automatically cast the created folder to RM type if it is a plain folder
                            // This occurs if the RM folder has been created via IMap, WebDav, etc
                            if (nodeService.hasAspect(child, ASPECT_FILE_PLAN_COMPONENT) == false)
                            {   
                                // check the type of the parent to determine what 'kind' of artifact to create                              
                                NodeRef parent = childAssocRef.getParentRef();
                                QName parentType = nodeService.getType(parent);
                                
                                if (dictionaryService.isSubClass(parentType, TYPE_FILE_PLAN))
                                {
                                    // create a rma:recordCategoty since we are in the root of the file plan
                                    nodeService.setType(child, TYPE_RECORD_CATEGORY);
                                }
                                else
                                {
                                    // create a rma:recordFolder and initialise record folder
                                    nodeService.setType(child, TYPE_RECORD_FOLDER);                                    
                                    recordFolderService.initialiseRecordFolder(child);
                                }
                            }                           
    
                            // Catch all to generate the rm id (assuming it doesn't already have one!)
                            setIdenifierProperty(child);
                        }                    
                    }
                }
                
                return null;
            }            
        });
        
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
