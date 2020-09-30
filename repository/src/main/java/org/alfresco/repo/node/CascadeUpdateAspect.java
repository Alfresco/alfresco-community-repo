/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Andy
 * @since 5.1
 */
public class CascadeUpdateAspect implements OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy, OnMoveNodePolicy
{
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private SearchTrackingComponent searchTrackingComponent;

  
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
 
    public void setSearchTrackingComponent(SearchTrackingComponent searchTrackingComponent)
    {
        this.searchTrackingComponent = searchTrackingComponent;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // need to listen to:
        // invokeOnCreateChildAssociation(newParentAssocRef, false);
        // invokeOnDeleteChildAssociation(oldParentAssocRef);
        // invokeOnMoveNode(oldParentAssocRef, newParentAssocRef); 
        // categories affect paths via membership (not paths beneath nodes that are categories) 
        // - only changing category structure requires a cascade not changing a node's on a categories
        
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindAssociationBehaviour(OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onDeleteChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        markCascadeUpdate(oldChildAssocRef.getChildRef());
        markCascadeUpdate(newChildAssocRef.getChildRef());
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        markCascadeUpdate(childAssocRef.getChildRef());
    }

    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if(!isNewNode)
        {
            markCascadeUpdate(childAssocRef.getChildRef());
        }
    }
    
    private void markCascadeUpdate(NodeRef nodeRef)
    {
        Status status = nodeService.getNodeStatus(nodeRef);
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_CRC, searchTrackingComponent.getCRC(status.getDbId()));
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_TX, status.getDbTxnId());   
    }
}
