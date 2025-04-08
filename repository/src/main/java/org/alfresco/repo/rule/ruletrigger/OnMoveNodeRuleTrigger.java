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

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A rule trigger for when nodes are moved.
 * 
 * @since 3.4.6
 */
public class OnMoveNodeRuleTrigger extends RuleTriggerAbstractBase implements NodeServicePolicies.OnMoveNodePolicy
{
    private static final String POLICY_NAME = NodeServicePolicies.OnMoveNodePolicy.QNAME.getLocalName();

    private boolean isClassBehaviour = false;

    public void setIsClassBehaviour(boolean isClassBehaviour)
    {
        this.isClassBehaviour = isClassBehaviour;
    }

    /**
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        if (isClassBehaviour == true)
        {
            this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), this, new JavaBehaviour(this, POLICY_NAME));
        }
        else
        {
            this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), this, new JavaBehaviour(this, POLICY_NAME));
        }
    }

    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        // Check that it is not rename operation.
        if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
        {
            triggerChildrenRules(newChildAssocRef);
        }
    }

    private void triggerChildrenRules(ChildAssociationRef newChildAssocRef)
    {
        NodeRef nodeRef = newChildAssocRef.getChildRef();
        boolean enabled = ruleService.rulesEnabled(nodeRef);
        try
        {
            if (enabled)
            {
                ruleService.disableRules(nodeRef);
            }
            triggerRules(newChildAssocRef.getParentRef(), nodeRef);
            for (ChildAssociationRef ref : nodeService.getChildAssocs(nodeRef))
            {
                triggerChildrenRules(ref);
            }
        }
        finally
        {
            if (enabled)
            {
                ruleService.enableRules(nodeRef);
            }
        }
    }
}
