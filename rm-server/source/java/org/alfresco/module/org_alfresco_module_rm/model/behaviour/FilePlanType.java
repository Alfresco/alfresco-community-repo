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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with the file plan type
 * 
 * @author Roy Wetherall
 */
public class FilePlanType implements RecordsManagementModel,
                                     NodeServicePolicies.OnCreateNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Permission service */
    private PermissionService permissionService;
    
    /** New record container name */
    private static final String NAME_NR_CONTAINER = "New Records";
    
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
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Bean initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                TYPE_FILE_PLAN,
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
	@Override
	public void onCreateNode(ChildAssociationRef assoc) 
	{
		// grab the newly created file plan
		NodeRef filePlan = assoc.getChildRef();
		
		// create the properties map
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
		properties.put(ContentModel.PROP_NAME, NAME_NR_CONTAINER);
		
		// create the 'new records' folder
		NodeRef container = nodeService.createNode(
						filePlan, 
						ASSOC_NEW_RECORDS, 
						QName.createQName(RM_URI, NAME_NR_CONTAINER), 
						TYPE_NEW_RECORDS_CONTAINER,
						properties).getChildRef();
		
		// set inheritance to false
		permissionService.setInheritParentPermissions(container, false);		
	} 
}
