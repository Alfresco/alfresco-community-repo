package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.service.cmr.rule.RuleType;

/**
 * Rule trigger interface
 * 
 * @author Roy Wetherall
 */
public interface RuleTrigger
{
    /** Key to store newly-created nodes for the controlling of rule triggers */
    public static final String RULE_TRIGGER_NEW_NODES = "RuleTrigger.NewNodes";
    /** Key to store renamed nodes for the controlling of rule triggers */
    public static final String RULE_TRIGGER_RENAMED_NODES = "RuleTrigger.RenamedNodes";
    
    /**
     * Register the rule trigger
     */
	void registerRuleTrigger();
	
    /**
     * Register the rule type as using this trigger
     * 
     * @param ruleType  the rule type
     */
	void registerRuleType(RuleType ruleType);
}
