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
package org.alfresco.repo.action;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * Class containing behaviour for the actions aspect
 * 
 * @author Roy Wetherall
 */
public class ActionsAspect implements CopyServicePolicies.OnCopyNodePolicy, CopyServicePolicies.OnCopyCompletePolicy, NodeServicePolicies.OnDeleteAssociationPolicy
{
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private RuleService ruleService;
    private NodeService nodeService;
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
   
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    public void init()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "ruleService", ruleService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        
        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                ActionModel.TYPE_ACTION_SCHEDULE,
                ActionModel.ASSOC_SCHEDULED_ACTION,
                new JavaBehaviour(this, "onDeleteAssociation"));
        
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ActionModel.ASPECT_ACTIONS,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyCompletePolicy.QNAME,
                ActionModel.ASPECT_ACTIONS,
                new JavaBehaviour(this, "onCopyComplete"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
                ActionModel.ASPECT_ACTIONS, 
                new JavaBehaviour(this, "onAddAspect"));
    }
    
    @Override
    public void onDeleteAssociation(AssociationRef nodeAssocRef)
    {
        // The act:actionSchedule type must have the association, so remove the source when the
        // association is deleted.
        NodeRef actionScheduleNodeRef = nodeAssocRef.getSourceRef();
        if (nodeService.exists(actionScheduleNodeRef) && !nodeService.hasAspect(actionScheduleNodeRef, ContentModel.ASPECT_PENDING_DELETE))
        {
            // Delete the source
            nodeService.deleteNode(actionScheduleNodeRef);
        }
    }

    /**
     * On add aspect policy behaviour
     * 
     * @param nodeRef NodeRef
     * @param aspectTypeQName QName
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        this.ruleService.disableRules(nodeRef);
        try
        {
            this.nodeService.createNode(
                    nodeRef,
                    ActionModel.ASSOC_ACTION_FOLDER,
                    ActionModel.ASSOC_ACTION_FOLDER,
                    ContentModel.TYPE_SYSTEM_FOLDER);
        }
        finally
        {
            this.ruleService.enableRules(nodeRef);
        }
    }
    
    /**
     * @return              Returns {@code ActionsAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new ActionsAspectCopyBehaviourCallback(behaviourFilter);
    }
    
    /**
     * Extends the default copy behaviour to include cascading to action folders.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class ActionsAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private final BehaviourFilter behaviourFilter;
        private ActionsAspectCopyBehaviourCallback(BehaviourFilter behaviourFilter)
        {
            this.behaviourFilter = behaviourFilter;
        }

        /**
         * Disables the aspect behaviour for this node
         * 
         * @return          Returns <tt>true</tt>
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            NodeRef targetNodeRef = copyDetails.getTargetNodeRef();
            behaviourFilter.disableBehaviour(targetNodeRef, ActionModel.ASPECT_ACTIONS);
            // Always copy
            return true;
        }

        /**
         * Always cascades to the action folders
         */
        @Override
        public ChildAssocCopyAction getChildAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            ChildAssociationRef childAssocRef = childAssocCopyDetails.getChildAssocRef();
            if (childAssocRef.getTypeQName().equals(ActionModel.ASSOC_ACTION_FOLDER))
            {
                return ChildAssocCopyAction.COPY_CHILD;
            }
            else
            {
                throw new IllegalStateException(
                        "Behaviour should have been invoked: \n" +
                        "   Aspect: " + this.getClass().getName() + "\n" +
                        "   " + childAssocCopyDetails + "\n" +
                        "   " + copyDetails);
            }
        }
    }

    /**
     * Re-enable aspect behaviour for the source node
     */
    public void onCopyComplete(
            QName classRef,
            NodeRef sourceNodeRef,
            NodeRef destinationRef,
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap)
    {
        behaviourFilter.enableBehaviour(sourceNodeRef, ActionModel.ASPECT_ACTIONS);
    }
}
