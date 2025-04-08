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
package org.alfresco.repo.rule.ruletrigger;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * We use this specialised trigger for create node beaucse of a problem with the CIFS integration.
 * <p>
 * The create node trigger will only be fired if the object is NOT a sub-type of content.
 * <p>
 * Policy names supported are:
 * <ul>
 * <li>{@linkplain org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy}</li>
 * <li>{@linkplain org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteChildAssociationPolicy}</li>
 * <li>{@linkplain org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy}</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
public class CreateNodeRuleTrigger extends RuleTriggerAbstractBase
        implements NodeServicePolicies.OnCreateNodePolicy
{
    private static Log logger = LogFactory.getLog(CreateNodeRuleTrigger.class);

    private static final String POLICY = "onCreateNode";

    /** Indicates whether this is a class behaviour or not */
    private boolean isClassBehaviour = false;

    /** Runtime rule service */
    RuntimeRuleService runtimeRuleService;

    /**
     * Set whether this is a class behaviour or not
     */
    public void setIsClassBehaviour(boolean isClassBehaviour)
    {
        this.isClassBehaviour = isClassBehaviour;
    }

    /**
     * Set the rule service
     */
    public void setRuntimeRuleService(RuntimeRuleService runtimeRuleService)
    {
        this.runtimeRuleService = runtimeRuleService;
    }

    /**
     * {@inheritDoc}
     */
    public void registerRuleTrigger()
    {
        if (isClassBehaviour == true)
        {
            this.policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, POLICY),
                    this,
                    new JavaBehaviour(this, POLICY));
        }
        else
        {
            this.policyComponent.bindAssociationBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, POLICY),
                    this,
                    new JavaBehaviour(this, POLICY));
        }

        for (QName ignoreAspect : getIgnoredAspects())
        {
            // Register interest in the addition and removal of the sys:noContent aspect
            this.policyComponent.bindClassBehaviour(
                    NodeServicePolicies.OnAddAspectPolicy.QNAME,
                    ignoreAspect,
                    new JavaBehaviour(this, "onAddAspect", NotificationFrequency.EVERY_EVENT));
            this.policyComponent.bindClassBehaviour(
                    NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                    ignoreAspect,
                    new JavaBehaviour(this, "onRemoveAspect", NotificationFrequency.EVERY_EVENT));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        NodeRef nodeRef = childAssocRef.getChildRef();

        // Keep track of new nodes to prevent firing of updates in the same transaction
        Set<NodeRef> newNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_NEW_NODES);
        newNodeRefSet.add(nodeRef);

        if (nodeRef != null &&
                nodeService.exists(nodeRef) == true &&
                nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT) == false)
        {
            NodeRef parentNodeRef = childAssocRef.getParentRef();

            if (logger.isDebugEnabled() == true)
            {
                logger.debug(
                        "Create node rule trigger fired for parent node " +
                                this.nodeService.getType(parentNodeRef).toString() + " " + parentNodeRef +
                                " and child node " +
                                this.nodeService.getType(nodeRef).toString() + " " + nodeRef);
            }

            triggerRules(parentNodeRef, nodeRef);
        }
    }

    /**
     * On add aspect behaviour
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (!nodeService.exists(nodeRef))
        {
            return;
        }
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(
                    "Removing the pending rules for the node " +
                            nodeRef.toString() +
                            " since the noContent aspect has been applied.");
        }

        // Removes any rules that have already been triggered for that node
        runtimeRuleService.removeRulePendingExecution(nodeRef);
    }

    /**
     * On remove aspect behaviour
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (!nodeService.exists(nodeRef))
        {
            return;
        }
        // We can assume it is the primary parent since it is only in the CIFS use case this aspect
        // is added. It's added during create, therefore we must be talking about the primary parent
        NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

        if (logger.isDebugEnabled() == true)
        {
            logger.debug(
                    "Create node rule trigger fired for parent node " +
                            this.nodeService.getType(parentNodeRef).toString() + " " + parentNodeRef +
                            " and child node " +
                            this.nodeService.getType(nodeRef).toString() + " " + nodeRef +
                            " (this was triggered on removal of the noContent aspect)");
        }

        // Do not trigger rules for rule and action type nodes
        if (ignoreTrigger(nodeRef) == false)
        {
            triggerRules(parentNodeRef, nodeRef);
        }
    }
}
