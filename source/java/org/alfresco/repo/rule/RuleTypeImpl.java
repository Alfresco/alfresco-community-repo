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
package org.alfresco.repo.rule;

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.action.CommonResourceAbstractBase;
import org.alfresco.repo.rule.ruletrigger.RuleTrigger;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
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
     * The action service
     */
    private ActionService actionService;
    
	/**
	 * The rule service
	 */
	private RuleService ruleService;
    
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
     * Set the action service
     * 
     * @param actionService the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
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
	public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef)
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
                    logger.debug("Triggering rule " + rule.getId());
                }
                
                if (rule.getExecuteAsychronously() == true)
                {
                    // Execute the rule now since it will be be queued for async execution later
                    this.actionService.executeAction(rule, actionedUponNodeRef);
                }
                else
                {
                    // Queue the rule to be executed at the end of the transaction (but still in the transaction)
                    ((RuntimeRuleService)this.ruleService).addRulePendingExecution(nodeRef, actionedUponNodeRef, rule);
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
	
	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name)
	{
		this.name = name;	
	}
}
