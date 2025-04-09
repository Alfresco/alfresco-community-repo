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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.action.CommonResourceAbstractBase;
import org.alfresco.repo.rule.ruletrigger.RuleTrigger;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;

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
     * @param ruleTriggers
     *            the rule triggers
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
     * @param ruleService
     *            the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
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
     * Rule type initialise method
     */
    public void init()
    {
        ((RuntimeRuleService) this.ruleService).registerRuleType(this);
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
     * @see org.alfresco.service.cmr.rule.RuleType#triggerRuleType(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately)
    {
        if (ruleService.isEnabled() == true &&
                nodeService.exists(actionedUponNodeRef) == true &&
                ruleService.isRuleTypeEnabled(this.getName()) == true)
        {
            List<Rule> rules = ruleService.getRules(
                    nodeRef,
                    true,
                    this.name);

            String ruleContext = null;
            if (logger.isDebugEnabled() == true)
            {
                if (actionedUponNodeRef != null)
                {
                    ruleContext = (executeRuleImmediately ? " now" : "    ") + " on " + nodeService.getPath(actionedUponNodeRef).toString().replaceAll("\\{[^}]*}", "");
                }
            }

            if (rules.size() != 0)
            {
                for (Rule rule : rules)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        if (nodeRef != null)
                        {
                            ruleContext = " " + rule.getTitle() + ruleContext;
                        }
                    }

                    // Only queue if the rule is not disabled
                    boolean exists = nodeService.exists(rule.getNodeRef());
                    if (exists && rule.getRuleDisabled() == false && ruleService.rulesEnabled(ruleService.getOwningNodeRef(rule)))
                    {
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Triggering rule" + ruleContext);
                        }
                        if (executeRuleImmediately == false)
                        {
                            // Queue the rule to be executed at the end of the transaction (but still in the transaction)
                            ((RuntimeRuleService) ruleService).addRulePendingExecution(nodeRef, actionedUponNodeRef, rule);
                        }
                        else
                        {
                            // Execute the rule now
                            ((RuntimeRuleService) ruleService).executeRule(rule, actionedUponNodeRef, null);
                        }
                    }
                    else if (logger.isDebugEnabled() == true)
                    {
                        String message = null;
                        if (exists)
                        {
                            message = "Disabled rule " + ruleContext;
                        }
                        else
                        {
                            message = "Rule " + rule.getNodeRef() + "no longer exist";
                        }
                        logger.debug(message);
                    }
                }
            }
            else
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("No rules to trigger" + ruleContext);
                }
            }
        }
    }
}
