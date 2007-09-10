/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.rule;

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.CommonResourceAbstractBase;
import org.alfresco.repo.rule.ruletrigger.RuleTrigger;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rule type implementation class.
 * 
 * @author Roy Wetherall
 */
public class RuleTypeImpl extends CommonResourceAbstractBase implements RuleType
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(RuleTypeImpl.class); 
    
	/**
	 * The rule service
	 */
	private RuleService ruleService;
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
    
    /**
     * Constructor
     * 
     * @param ruleTriggers	the rule triggers
     */
    public RuleTypeImpl(List<RuleTrigger> ruleTriggers)
    {
    	if (ruleTriggers != null)
    	{
	    	for (RuleTrigger trigger : ruleTriggers)
			{
				trigger.registerRuleType(this);
			}
    	}
    }
    
    /**
     * Set the rule service
     * 
     * @param ruleService  the rule service
     */
    public void setRuleService(RuleService ruleService)
	{
		this.ruleService = ruleService;
	}
    
    /**
     * Set the node service
     * 
     * @param nodeService	the node service
     */
    public void setNodeService(NodeService nodeService)
    {
    	this.nodeService = nodeService;
    }

    /**
     * Rule type initialise method
     */
    public void init()
    {
    	((RuntimeRuleService)this.ruleService).registerRuleType(this);
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleType#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.alfresco.service.cmr.rule.RuleType#getDisplayLabel()
     */
    public String getDisplayLabel()
    {
        return I18NUtil.getMessage(this.name + "." + "display-label");
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleType#triggerRuleType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately)
	{
		if (this.ruleService.isEnabled() == true && 
        	this.nodeService.exists(actionedUponNodeRef) == true && 
        	this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_TEMPORARY) == false)
        {
    		if (this.ruleService.hasRules(nodeRef) == true)
            {
                List<Rule> rules = this.ruleService.getRules(
                		nodeRef, 
                        true,
                        this.name);
    			
                for (Rule rule : rules)
                {   
                    if (logger.isDebugEnabled() == true)
                    {
                        NodeRef ruleNodeRef = rule.getNodeRef();
                        if (nodeRef != null)
                        {
                            logger.debug("Triggering rule " + ruleNodeRef.toString());
                        }
                    }
                    
                    // Only queue if the rule is not disabled
                    if (rule.getRuleDisabled() == false)
                    {
                        if (executeRuleImmediately == false)
                        {
                            // Queue the rule to be executed at the end of the transaction (but still in the transaction)
                            ((RuntimeRuleService)this.ruleService).addRulePendingExecution(nodeRef, actionedUponNodeRef, rule);
                        }
                        else
                        {
                            // Execute the rule now
                            ((RuntimeRuleService)this.ruleService).executeRule(rule, actionedUponNodeRef, null);
                        }
                    }
                }
            }
            else
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("This node has no rules to trigger.");
                }
            }
        }
	}
	
	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name)
	{
		this.name = name;	
	}
}
