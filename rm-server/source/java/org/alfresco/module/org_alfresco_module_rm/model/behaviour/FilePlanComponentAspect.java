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
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.copy.AbstractCopyBehaviourCallback;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with the file plan component aspect
 * 
 * @author Roy Wetherall
 */
public class FilePlanComponentAspect implements RecordsManagementModel,
                                                NodeServicePolicies.OnAddAspectPolicy,
                                                NodeServicePolicies.OnMoveNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Node service */
    private NodeService nodeService;
    
    /** File plan service */
    private FilePlanService filePlanService;
    
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
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * Bean initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));    
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ASPECT_FILE_PLAN_COMPONENT,
                new JavaBehaviour(this, "getCopyCallback"));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(nodeRef) == true)
                {                   
                    // Look up the root and set on the aspect if found
                    NodeRef root = filePlanService.getFilePlan(nodeRef);
                    if (root != null)
                    {
                        nodeService.setProperty(nodeRef, PROP_ROOT_NODEREF, root);
                    }
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());        
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(final ChildAssociationRef oldChildAssocRef, final ChildAssociationRef newChildAssocRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(newChildAssocRef.getParentRef()) == true && 
                    nodeService.exists(newChildAssocRef.getChildRef()) == true)
                {
                    // Look up the root and re-set the value currently stored on the aspect
                    NodeRef root = filePlanService.getFilePlan(newChildAssocRef.getParentRef());
                    // NOTE: set the null value if no root found
                    nodeService.setProperty(newChildAssocRef.getChildRef(), PROP_ROOT_NODEREF, root);
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());        
    }
    
    /**
     * Copy behaviour call back
     * 
     * @param   classRef    class reference
     * @param   copyDetail  details of the information being copied
     * @return  CopyBehaviourCallback
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new AbstractCopyBehaviourCallback()
        {
            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getChildAssociationCopyAction(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails, org.alfresco.repo.copy.CopyBehaviourCallback.CopyChildAssociationDetails)
             */
            public ChildAssocCopyAction getChildAssociationCopyAction(
                    QName classQName,
                    CopyDetails copyDetails,
                    CopyChildAssociationDetails childAssocCopyDetails)
            {
                // Do not copy the associations
                return null;
            }
            
            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getCopyProperties(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails, java.util.Map)
             */
            public Map<QName, Serializable> getCopyProperties(
                    QName classQName,
                    CopyDetails copyDetails,
                    Map<QName, Serializable> properties)
            {
                // Only copy the root node reference if the new value can be looked up via the parent
                NodeRef root = filePlanService.getFilePlan(copyDetails.getTargetParentNodeRef());
                if (root != null)
                {
                    properties.put(PROP_ROOT_NODEREF, root);
                }
                return properties;
            }

            /**
             * @see org.alfresco.repo.copy.CopyBehaviourCallback#getMustCopy(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
             */
            public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
            {
                // Ensure the aspect is copied
                return true;
            }            
        };
    }    
}
