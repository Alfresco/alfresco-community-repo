/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
