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
package org.alfresco.repo.rule;

import java.util.List;
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class containing behaviour for the rules aspect
 * 
 * @author Roy Wetherall
 */
public class RulesAspect implements
                 CopyServicePolicies.OnCopyNodePolicy,
                 CopyServicePolicies.OnCopyCompletePolicy,
                 NodeServicePolicies.OnAddAspectPolicy,
                 NodeServicePolicies.BeforeRemoveAspectPolicy,
                 NodeServicePolicies.BeforeDeleteNodePolicy
{
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private RuleService ruleService;
    private NodeService nodeService;
    
    private static Log logger = LogFactory.getLog(RulesAspect.class); 
    
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

        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                RuleModel.ASPECT_RULES,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyCompletePolicy.QNAME,
                RuleModel.ASPECT_RULES,
                new JavaBehaviour(this, "onCopyComplete"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME, 
                RuleModel.ASPECT_RULES, 
                new JavaBehaviour(this, "onAddAspect"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeRemoveAspectPolicy.QNAME, 
                RuleModel.ASPECT_RULES, 
                new JavaBehaviour(this, "beforeRemoveAspect"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, 
                RuleModel.ASPECT_RULES, 
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    /**
     * Creates the rules folder below the node
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        this.ruleService.disableRules(nodeRef);
        try
        {
            int count = this.nodeService.getChildAssocs(nodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER).size();
            if (count == 0)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("rules folder does not exist: create new rules folder for: " + nodeRef);
                }
                this.nodeService.createNode(
                        nodeRef,
                        RuleModel.ASSOC_RULE_FOLDER,
                        RuleModel.ASSOC_RULE_FOLDER,
                        ContentModel.TYPE_SYSTEM_FOLDER);
            }
        }
        finally
        {
            this.ruleService.enableRules(nodeRef);
        }
    }
    
    
    /**
     * The rule folder & below will be deleted automatically in the normal way, so we don't need to worry about them.
     * But we need additional handling for any other folders which have rules linked to this folder's rules. See
     * ALF-11923, ALF-15262.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (!aspectTypeQName.equals(RuleModel.ASPECT_RULES))
        {
            return;
        }

        this.ruleService.disableRules(nodeRef);
        try
        {
            for (ChildAssociationRef childAssocRef : nodeService.getChildAssocs(nodeRef, RuleModel.ASSOC_RULE_FOLDER,
                    RuleModel.ASSOC_RULE_FOLDER, false))
            {
                // We are only interested in the deletion of primary associations to a rule folder, which usually
                // happens when all rules in a folder are deleted and the ASPECT_RULES aspect is removed
                if (!childAssocRef.isPrimary())
                {
                    continue;
                }
                NodeRef savedRuleFolderRef = childAssocRef.getChildRef();
                // Cascade the removal to all secondary (linked) parents
                List<ChildAssociationRef> linkedAssocs = nodeService.getParentAssocs(savedRuleFolderRef);
                for (ChildAssociationRef linkAssoc : linkedAssocs)
                {
                    if (!linkAssoc.isPrimary())
                    {
                        // Remove the aspect from linked parents; this will also delete the linking secondary
                        // association
                        nodeService.removeAspect(linkAssoc.getParentRef(), RuleModel.ASPECT_RULES);
                    }
                }
            }
        }
        finally
        {
            this.ruleService.enableRules(nodeRef);
        }
    }

    /**
     * <br>author Neil McErlean
     * @since 3.4.11
     */
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // In case the event isn't triggered automatically by node service (e.g. on a cascaded node tree deletion),
        // trigger handling removal of the rules aspect.
        beforeRemoveAspect(nodeRef, RuleModel.ASPECT_RULES);
    }
    
    /**
     * @return              Returns CopyBehaviourCallback
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return new RulesAspectCopyBehaviourCallback(behaviourFilter);
    }
    
    /**
     * Copy behaviour for the 'rules' model
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private class RulesAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private final BehaviourFilter behaviourFilter;
        
        private RulesAspectCopyBehaviourCallback(BehaviourFilter behaviourFilter)
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
            behaviourFilter.disableBehaviour(targetNodeRef, RuleModel.ASPECT_RULES);
            // Always copy
            return true;
        }

        /**
         * Always copy into rules folders
         * 
         * @return          Returns {@link ChildAssocCopyAction#COPY_CHILD}
         *                  for {@link RuleModel#ASSOC_RULE_FOLDER}
         */
        @Override
        public ChildAssocCopyAction getChildAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            ChildAssociationRef childAssocRef = childAssocCopyDetails.getChildAssocRef();
            if (childAssocRef.getTypeQName().equals(RuleModel.ASSOC_RULE_FOLDER))
            {
                return ChildAssocCopyAction.COPY_CHILD;
            }
            else
            {
                super.throwExceptionForUnexpectedBehaviour(copyDetails, childAssocCopyDetails.toString());
                return null;            // Never reached
            }
        }

        /**
         * Force copy recursion after copying a rules folder
         * 
         * @return          Returns {@link ChildAssocRecurseAction#FORCE_RECURSE}
         *                  for {@link RuleModel#ASSOC_RULE_FOLDER}
         */
        @Override
        public ChildAssocRecurseAction getChildAssociationRecurseAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyChildAssociationDetails childAssocCopyDetails)
        {
            ChildAssociationRef childAssocRef = childAssocCopyDetails.getChildAssocRef();
            if (childAssocRef.getTypeQName().equals(RuleModel.ASSOC_RULE_FOLDER))
            {
                return ChildAssocRecurseAction.FORCE_RECURSE;
            }
            else
            {
                super.throwExceptionForUnexpectedBehaviour(copyDetails, childAssocCopyDetails.toString());
                return null;            // Never reached
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
        behaviourFilter.enableBehaviour(destinationRef, RuleModel.ASPECT_RULES);
    }
}
