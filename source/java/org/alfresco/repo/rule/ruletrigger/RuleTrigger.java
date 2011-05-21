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
