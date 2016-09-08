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
package org.alfresco.module.org_alfresco_module_rm.freeze;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Freeze Service Implementation
 * 
 * @author Roy Wetherall
 */
public class FreezeServiceImpl implements FreezeService, 
                                          RecordsManagementModel, 
                                          NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** Policy Component */
    private PolicyComponent policyComponent;
    
    /** Node Service */
    private NodeService nodeService;

    /** Records Management Service */
    private RecordsManagementService recordsManagementService;

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * Init service
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, 
                this, 
                new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT)); 
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){

            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(nodeRef) == true &&
                        recordsManagementService.isFilePlanComponent(nodeRef) == true)
                {
                    if (recordsManagementService.isFrozen(nodeRef) == true)
                    {
                        // never allowed to delete a frozen node 
                        throw new AccessDeniedException("Frozen nodes can not be deleted.");
                    }
                    
                    // check children
                    checkChildren(nodeService.getChildAssocs(nodeRef));            
                }
                
                return null;
            }});
        
    }
    
    /**
     * Checks the children for frozen nodes.  Throws security error if any are found.
     * 
     * @param assocs
     */
    private void checkChildren(List<ChildAssociationRef> assocs)
    {
        for (ChildAssociationRef assoc : assocs)
        {
            // we only care about primary children        
            if (assoc.isPrimary() == true)
            {
                NodeRef nodeRef = assoc.getChildRef();                
                if (recordsManagementService.isFrozen(nodeRef) == true)
                {
                    // never allowed to delete a node with a frozen child
                    throw new AccessDeniedException("Can not delete node, because it contains a frozen child node.");
                }
                
                // check children
                checkChildren(nodeService.getChildAssocs(nodeRef));
            }
        }        
    }
}
