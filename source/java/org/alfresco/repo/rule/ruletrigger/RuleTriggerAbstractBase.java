/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.rule.ruletrigger;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleType;

/**
 * Rule trigger abstract base
 * 
 * @author Roy Wetherall
 */
public abstract class RuleTriggerAbstractBase implements RuleTrigger
{
    /**
     * A list of the rule types that are interested in this trigger
     */
    private Set<RuleType> ruleTypes = new HashSet<RuleType>();

    /**
     * The policy component
     */
    protected PolicyComponent policyComponent;

    /**
     * The node service
     */
    protected NodeService nodeService;

    /**
     * The authentication Component
     */

    protected AuthenticationComponent authenticationComponent;

    /**
     * Set the policy component
     * 
     * @param policyComponent
     *            the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service
     * 
     * @param nodeService
     *            the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the authenticationComponent
     */

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Registration of an interested rule type
     */
    public void registerRuleType(RuleType ruleType)
    {
        this.ruleTypes.add(ruleType);
    }

    /**
     * Trigger the rules that relate to any interested rule types for the node
     * references passed.
     * 
     * @param nodeRef
     *            the node reference who rules are to be triggered
     * @param actionedUponNodeRef
     *            the node reference that will be actioned upon by the rules
     */
    protected void triggerRules(NodeRef nodeRef, NodeRef actionedUponNodeRef)
    {
        for (RuleType ruleType : this.ruleTypes)
        {
            ruleType.triggerRuleType(nodeRef, actionedUponNodeRef);
        }
    }
}
