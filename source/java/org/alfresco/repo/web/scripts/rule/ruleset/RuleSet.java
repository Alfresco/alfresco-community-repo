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
package org.alfresco.repo.web.scripts.rule.ruleset;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author unknown
 *
 */
public class RuleSet implements Serializable
{

    private static final long serialVersionUID = 6985140035928444095L;

    private List<RuleRef> rules = null;

    private List<RuleRef> inheritedRules = null;

    private NodeRef rulesetNodeRef;

    private NodeRef linkedToRuleSet;

    private List<NodeRef> linkedFromRuleSets;

    /**
     * Set list of the rules "owned" by this rule set
     * 
     * @param rules the list of rules to set
     */
    public void setRules(List<RuleRef> rules)
    {
        this.rules = rules;
    }

    /**
     * Get list of the rules "owned" by this rule set
     * 
     * @return list of "owned" rules
     */
    public List<RuleRef> getRules()
    {
        return rules;
    }

    /**
     * Set list of the rules inherited by this rule set from parent
     * 
     * @param inheritedRules the list of inherited rules to set
     */
    public void setInheritedRules(List<RuleRef> inheritedRules)
    {
        this.inheritedRules = inheritedRules;
    }

    /**
     * Get list of the rules inherited by this rule set from parent
     * 
     * @return list of inherited rules
     */
    public List<RuleRef> getInheritedRules()
    {
        return inheritedRules;
    }

    /**
     * Set the nodeRef to which this ruleset belongs
     * 
     * @param rulesetNodeRef the ruleset nodeRef to set
     */
    public void setRulesetNodeRef(NodeRef rulesetNodeRef)
    {
        this.rulesetNodeRef = rulesetNodeRef;
    }

    /**
     * Get the nodeRef to which this ruleset belongs
     * 
     * @return ruleset nodeRef
     */
    public NodeRef getRulesetNodeRef()
    {
        return rulesetNodeRef;
    }

    /**
     * Set the nodeRef to which this ruleset linked to 
     * 
     * @param linkedToRuleSet the nodeRef to set
     */
    public void setLinkedToRuleSet(NodeRef linkedToRuleSet)
    {
        this.linkedToRuleSet = linkedToRuleSet;
    }

    /**
     * Get the nodeRef to which this ruleset linked to
     * 
     * @return linked to nodeRef
     */
    public NodeRef getLinkedToRuleSet()
    {
        return linkedToRuleSet;
    }

    /**
     * Set the list of nodeRefs that link to this ruleset
     * 
     * @param linkedFromRuleSets the list of nodeRefs to set
     */
    public void setLinkedFromRuleSets(List<NodeRef> linkedFromRuleSets)
    {
        this.linkedFromRuleSets = linkedFromRuleSets;
    }

    /**
     * Get the list of nodeRefs that link to this ruleset
     * 
     * @return the list of nodeRefs
     */
    public List<NodeRef> getLinkedFromRuleSets()
    {
        return linkedFromRuleSets;
    }

}
