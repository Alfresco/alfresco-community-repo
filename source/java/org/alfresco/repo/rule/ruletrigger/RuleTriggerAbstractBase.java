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
package org.alfresco.repo.rule.ruletrigger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;

/**
 * Rule trigger abstract base 
 * 
 * @author Roy Wetherall
 */
public abstract class RuleTriggerAbstractBase implements RuleTrigger
{
    /** the types (hardcoded) to ignore generally */
    private static final Set<QName> IGNORE_TYPES;
    
    static
    {
        IGNORE_TYPES = new HashSet<QName>(13);
        IGNORE_TYPES.add(RuleModel.TYPE_RULE);
        IGNORE_TYPES.add(ActionModel.TYPE_ACTION);
        IGNORE_TYPES.add(ContentModel.TYPE_THUMBNAIL);
        IGNORE_TYPES.add(ContentModel.TYPE_FAILED_THUMBNAIL);
        // Workaround to prevent rules running on cm:rating nodes (which happened for 'liked' folders ALF-8308 & ALF-8382)
        IGNORE_TYPES.add(ContentModel.TYPE_RATING);
        IGNORE_TYPES.add(ContentModel.TYPE_SYSTEM_FOLDER);
    }
    
    /**
     * A list of the rule types that are interested in this trigger
     */
    private Set<RuleType> ruleTypes = new HashSet<RuleType>();
    private Set<QName> ignoredAspects = Collections.emptySet();

    protected PolicyComponent policyComponent;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected AuthenticationComponent authenticationComponent;
    protected DictionaryService dictionaryService;
    protected RuleService ruleService;
    
    /** 
     * Indicates whether the rule should be executed immediately or at the end of the transaction.
     * By default this is false as all rules are executed at the end of the transaction.
     */
    protected boolean executeRuleImmediately = false;
    
    /**
     * Set the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
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
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the RuleService to assist with enabled/disabled check
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * Sets the values that indicates whether the rule should be executed immediately or not.
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
     * @param nodeRef                   the node reference who rules are to be triggered
     * @param actionedUponNodeRef       the node reference that will be actioned upon by the rules
     */
    protected void triggerRules(NodeRef nodeRef, NodeRef actionedUponNodeRef)
    {
        // Break out early if rules are off
        if (!areRulesEnabled())
        {
            return;
        }
    	// Do not trigger rules for rule and action type nodes
    	if (ignoreTrigger(actionedUponNodeRef) == false)
    	{
	        for (RuleType ruleType : this.ruleTypes)
	        {
	            ruleType.triggerRuleType(nodeRef, actionedUponNodeRef, this.executeRuleImmediately);
	        }
    	}
    }
    
    /**
     * Helper method to allow triggers to check if rules are enabled or disabled
     * (ALF-10839: Eliminate rule discovery overhead on property update when rules have been disabled)
     * @return          <tt>true</tt> if rules are enabled
     */
    protected boolean areRulesEnabled()
    {
        return ruleService.isEnabled();
    }
    
    /**
     * Indicate whether the trigger should be ignored or not
     * @param actionedUponNodeRef	  actioned upon node reference
     * @return boolean				  true if the trigger should be ignored, false otherwise
     */
    protected boolean ignoreTrigger(NodeRef actionedUponNodeRef)
    {
    	boolean result = false;    	
    	QName typeQName = nodeService.getType(actionedUponNodeRef);
    	if (IGNORE_TYPES.contains(typeQName))
    	{
    		result = true;
    	}
        for (QName aspectToIgnore : getIgnoredAspects())
        {
            if (nodeService.hasAspect(actionedUponNodeRef, aspectToIgnore))
            {
                return true;
            }
        }
    	return result;
    }
    
    public Set<QName> getIgnoredAspects()
    {
        return ignoredAspects;
    }
    
    /**
     * Converting String Aspects from Spring context to QNames
     *
     * @param ignoredAspects List of ignoredAspects
     */
    public void setIgnoredAspectsStr(List<String> ignoredAspects)
    {
        this.ignoredAspects = new HashSet<>(13);
        
        // MNT-9885 fix.
        // Converts String Aspects to QNames and adds it to ignoredAspects.
        // If afterDictionaryInit#DictionaryListener is used for setting up ignored Aspects from Spring context the
        // registerRuleTrigger#CreateNodeRuleTrigger is initialized before afterDictionaryInit#DictionaryListener
        for (String ignoredAspectStr : ignoredAspects)
        {
            this.ignoredAspects.add(QName.createQName(ignoredAspectStr));
        }
    }
}
