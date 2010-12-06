/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.rule.ruletrigger;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;

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
     * The content service
     */
    protected ContentService contentService;

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
     * Set the content service
     * 
     * @param contentService	the content service
     */
    public void setContentService(ContentService contentService) 
    {
		this.contentService = contentService;
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
     * 
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
    	// Do not trigger rules for rule and action type nodes
    	if (ignoreTrigger(actionedUponNodeRef) == false)
    	{
	        for (RuleType ruleType : this.ruleTypes)
	        {
	            ruleType.triggerRuleType(nodeRef, actionedUponNodeRef, this.executeRuleImmediately);
	        }
    	}
    }
    
    private boolean ignoreTrigger(NodeRef actionedUponNodeRef)
    {
    	boolean result = false;    	
    	QName typeQName = nodeService.getType(actionedUponNodeRef);
    	if (typeQName.equals(RuleModel.TYPE_RULE) == true ||
    		typeQName.equals(ActionModel.TYPE_ACTION) == true ||
    		typeQName.equals(ActionModel.TYPE_COMPOSITE_ACTION) == true ||
    		typeQName.equals(ContentModel.TYPE_THUMBNAIL) == true)
    	{
    		result = true;
    	}
    	return result;
    }
}
