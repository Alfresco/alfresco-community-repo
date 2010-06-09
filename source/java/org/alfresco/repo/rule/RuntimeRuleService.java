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
package org.alfresco.repo.rule;

import java.util.Set;

import org.alfresco.repo.rule.RuleServiceImpl.ExecutedRuleData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;

/**
 * Runtime rule service
 * 
 * @author Roy Wetherall
 */
public interface RuntimeRuleService 
{
    /**
     * Execute a rule
     * 
     * @param rule                  rule
     * @param actionedUponNodeRef   actioned upon node reference
     * @param executedRules         already executed rules
     */
    void executeRule(Rule rule, NodeRef actionedUponNodeRef, Set<ExecutedRuleData> executedRules);
    
    /**
     * Add a rule to the pending execution list
     * 
     * @param actionableNodeRef     actionable node reference
     * @param actionedUponNodeRef   actioned upon node reference
     * @param rule                  rule
     */
	void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule);
    
	/**
	 * Add a rule to the pending execution list
	 * 
	 * @param actionableNodeRef    actionable node reference
	 * @param actionedUponNodeRef  actioned upon node reference
	 * @param rule                 rule
	 * @param executeAtEnd         true if execute rule at the end of the transaction, false otherwise
	 */
    void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule, boolean executeAtEnd);

    /**
     * Remove all pending rules that are actioning upon the given node reference
     * 
     * @param actionedUponNodeRef   actioned upon node reference
     */
    public void removeRulePendingExecution(NodeRef actionedUponNodeRef);
    
    /**
     * Execute all pending rules
     */
	void executePendingRules();	
	
	/**
	 * Register a rule type
	 * 
	 * @param ruleType rule type
	 */
	void registerRuleType(RuleType ruleType);
	
	/**
	 * Get the folder that the rules are saved within for a given actionable node
	 * 
	 * @param nodeRef              node reference
	 * @return ChildAssocationref  child association reference to the rule folder
	 */
	ChildAssociationRef getSavedRuleFolderAssoc(NodeRef nodeRef);
}
