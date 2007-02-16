/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.rule.ruletrigger;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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

    /** The dictionary service */
    protected DictionaryService dictionaryService;
    
    /** 
     * Indicates whether the rule should be executed immediately or at the end of the transaction.
     * By default this is false as all rules are executed at the end of the transaction.
     */
    protected boolean executeRuleImmediately = false;
    
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
     * Set the dictionary service
     * 
     * @param dictionaryService     the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Sets the values that indicates whether the rule should be executed immediately
     * or not.
     * 
     * @param executeRuleImmediately    true execute the rule immediaely, false otherwise
     */
    public void setExecuteRuleImmediately(boolean executeRuleImmediately)
    {
        this.executeRuleImmediately = executeRuleImmediately;
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
            ruleType.triggerRuleType(nodeRef, actionedUponNodeRef, this.executeRuleImmediately);
        }
    }
}
