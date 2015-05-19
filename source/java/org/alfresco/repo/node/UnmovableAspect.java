/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.node;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeMoveNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Unmovable aspect behaviour bean.
 * 
 * Deletions of nodes with the {@link ContentModel#ASPECT_UNMOVABLE} are not allowed by default.
 * This class registers the behaviour that prevents the move.
 * <p/>
 * This aspect/behaviour combination allows for detailed application control of when node deletion is allowed
 * or disallowed for particular nodes. It is not related to the normal permissions controls, which of course apply.
 * <p/>
 * @author Sergey Scherbovich
 * @since 4.2.5
 */
public class UnmovableAspect implements NodeServicePolicies.BeforeMoveNodePolicy
{
   private PolicyComponent policyComponent;
   private NodeService nodeService;
   
   /**
    * Set the policy component
    * 
    * @param policyComponent   policy component
    */
   public void setPolicyComponent(PolicyComponent policyComponent)
   {
       this.policyComponent = policyComponent;
   }
   
   /**
    * Set the node service
    * 
    * @param nodeService   node service
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }
   
   /**
    * Initialise method
    */
   public void init()
   {
       this.policyComponent.bindClassBehaviour(BeforeMoveNodePolicy.QNAME,
               ContentModel.ASPECT_UNMOVABLE,
               new JavaBehaviour(this, "beforeMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
   }

   /**
    * Prevents node from to be moved
    */
    @Override
    public void beforeMoveNode(ChildAssociationRef oldChildAssocRef, NodeRef newParentRef)
    {
        QName nodeType = nodeService.getType(oldChildAssocRef.getChildRef());
        throw new AlfrescoRuntimeException(nodeType.toPrefixString() + " move is not allowed. Attempted to move " + oldChildAssocRef.getChildRef());
    }
}
