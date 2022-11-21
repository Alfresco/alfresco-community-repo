/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.service.cmr.rule;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Rule service interface.
 * 
 * @author Roy Wetherall
 */
public interface RuleService
{
    /**
     * Get the rule types currently defined in the repository.
     * 
     * @return a list of rule types
     */
    @Auditable
    public List<RuleType> getRuleTypes();

    /**
     * Gets a rule type by name.
     * 
     * @param name 	the name of the rule type
     * @return 		the rule type, null if not found
     */
    @Auditable(parameters = {"name"})
    public RuleType getRuleType(String name);
    
    /**
     * Enable rules for the current thread
     * 
     * @see #isEnabled
     * @see #disableRules
     */
    @Auditable
    public void enableRules();
    
    /**
     * Disable rules for the current thread
     * @see #enableRules
     * @see #isEnabled
     */
    @Auditable
    public void disableRules();
    
    /**
     * Indicates whether rules are currently enabled for the current thread or not
     * 
     * @see #enableRules
     * @see #disableRules
     * 
     * @return  true if rules are enabled for the current thread, false otherwise
     */
    @Auditable
    public boolean isEnabled();
    
    /**
     * Indicates whether the rules for a given node are enabled or not.  If the 
     * rules are not enabled then they will not be executed.
     * 
     * @param nodeRef       the node reference
     * @return              true if the rules are enabled, false otherwise
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean rulesEnabled(NodeRef nodeRef);
    
    /**
     * Disables the rules for a given node reference.  When the rules are disabled they
     * will not execute.
     * 
     * @param nodeRef  the node reference
     */
    @Auditable(parameters = {"nodeRef"})
    public void disableRules(NodeRef nodeRef);
    
    /**
     * Enables the rules for a given node reference.  When the rules are enabled they
     * will execute as usual.  By default all rules are enabled.
     * 
     * @param nodeRef   the node reference
     */
    @Auditable(parameters = {"nodeRef"})
    public void enableRules(NodeRef nodeRef);
    
    /**
     * Disables a rule, preventing it from being fired.
     * 
     * @see #enableRule(Rule)
     * 
     * @param rule  the rule to disable
     */
    @Auditable(parameters = {"rule"})
    public void disableRule(Rule rule);
    
    /**
     * Enables a rule previously disabled.
     * 
     * @see #disableRule(Rule)
     * 
     * @param rule  the rule to enable
     */
    @Auditable(parameters = {"rule"})
    public void enableRule(Rule rule);
    
    /**
     * Disables the rules of a given type.
     * 
     * @param ruleType	rule type
     */
    @Auditable(parameters = {"ruleType"})
    public void disableRuleType(String ruleType);

    /**
     * Enables rules of a given type.
     * 
     * @param ruleType 	rule type
     */
    @Auditable(parameters = {"ruleType"})
    public void enableRuleType(String ruleType);
    
    /**
     * Indicates whether the rule is enabled or not
     * @param ruleType	rule type
     * @return boolean	true if enabled false otherwise
     */
    @Auditable(parameters = {"ruleType"})
    public boolean isRuleTypeEnabled(String ruleType);

    /**
     * Indicates whether the node in question has any rules associated with it.
     * 
     * @param nodeRef 	the node reference
     * @return 			true if the node has rules associated, false otherwise
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean hasRules(NodeRef nodeRef);

    /**
     * Get all the rules associated with an actionable node, including those
     * inherited from parents.
     * <p>
     * An exception is raised if the actionable aspect is not present on the
     * passed node.
     * 
     * @param nodeRef 	the node reference
     * @return 			a list of the rules associated with the node
     */
    @Auditable(parameters = {"nodeRef"})
    public List<Rule>   getRules(NodeRef nodeRef);

    /**
     * Get the rules associated with an actionable node.
     * <p>
     * Optionally this list includes rules inherited from its parents.
     * <p>
     * An exception is raised if the actionable aspect is not present on the
     * passed node.
     * 
     * @param nodeRef 			the node reference
     * @param includeInhertied	indicates whether the inherited rules should be included in
     *            				the result list or not
     * @return 					a list of the rules associated with the node
     */
    @Auditable(parameters = {"nodeRef", "includeInhertied"})
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInhertied);
    
    /**
     * Get the rules associated with an actionable node that are of a specific rule type.
     * 
     * @param nodeRef					the node reference
     * @param includeInhertiedRuleType	indicates whether the inherited rules should be included in 
     * 									the result list or not
     * @param ruleTypeName				the name of the rule type, if null is passed all rule types 
     * 									are returned
     * @return							a list of the rules associated with the node
     */
    @Auditable(parameters = {"nodeRef", "includeInhertiedRuleType", "ruleTypeName"})
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInhertiedRuleType, String ruleTypeName);

    /**
     * Count the number of rules associated with an actionable node.
     *  
     * @param nodeRef                   the node reference
     * @return                          a list of the rules associated with the node
     */
    @Auditable(parameters = {"nodeRef"})
    public int countRules(NodeRef nodeRef);

    /**
     * Traverse the folder hierarchy find all the folder nodes that could supply rules by inheritance.
     *
     * @param nodeRef The starting node ref.
     * @return A list of node refs, starting with the first parent of the first parent of ... and ending with the object generated by the
     * given node ref.
     */
    @Auditable (parameters = { "nodeRef" })
    @Experimental
    List<NodeRef> getNodesSupplyingRuleSets(NodeRef nodeRef);

    /**
     * Get a list of folders inheriting the specified rule set.
     *
     * @param ruleSet The rule set node.
     * @param maxFoldersToReturn A limit on the number of folders to return (since otherwise this could traverse a very large proportion of
     * the repository).
     * @return The list of the inheriting folders.
     */
    @Auditable (parameters = { "ruleSet", "maxFoldersToReturn" })
    @Experimental
    List<NodeRef> getFoldersInheritingRuleSet(NodeRef ruleSet, int maxFoldersToReturn);

    /**
     * Get a list of folders linking to the specified rule set.
     *
     * @param ruleSet The rule set node.
     * @param maxFoldersToReturn A limit on the number of folders to return.
     * @return The list linking folders.
     */
    @Auditable (parameters = { "ruleSet" })
    @Experimental
    List<NodeRef> getFoldersLinkingToRuleSet(NodeRef ruleSet, int maxFoldersToReturn);

    /**
     * Get the rule given its node reference
     * 
     * @param nodeRef the node reference
     * @return the rule corresponding to the node reference
     */
    @Auditable(parameters = {"nodeRef"})
    public Rule getRule(NodeRef nodeRef);

    /**
     * Saves the details of the rule to the specified node reference.
     * <p>	
     * If the rule is already associated with the node, the details are updated
     * with those specified.
     * 
     * @param nodeRef NodeRef
     * @param rule Rule
     */
    @Auditable(parameters = {"nodeRef", "rule"})
    public Rule saveRule(NodeRef nodeRef, Rule rule);

    /**
     * 
     * @param nodeRef NodeRef
     * @param rule Rule
     * @param index int
     */
    @Auditable(parameters = {"nodeRef", "rule", "index"})
    public void saveRule(NodeRef nodeRef, Rule rule, int index);
    
    /**
     * 
     * @param nodeRef NodeRef
     * @param ruleNodeRef NodeRef
     * @param index int
     */
    @Auditable(parameters = {"nodeRef", "ruleNodeRef", "index"})
    public void setRulePosition(NodeRef nodeRef, NodeRef ruleNodeRef, int index);
    
    /**
     * 
     * @param nodeRef NodeRef
     * @param rule Rule
     * @param index int
     */
    @Auditable(parameters = {"nodeRef", "rule", "index"})
    public void setRulePosition(NodeRef nodeRef, Rule rule, int index);
    
    /**
     * Removes a rule from the given rule actionable node
     * 
     * @param nodeRef  the actionable node reference
     */
    @Auditable(parameters = {"nodeRef", "rule"})
    public void removeRule(NodeRef nodeRef, Rule rule);
    
    /**
     * Removes all the rules associated with an actionable node
     * 
     * @param nodeRef   the actionable node reference
     */
    @Auditable(parameters = {"nodeRef"})
    public void removeAllRules(NodeRef nodeRef);
    
    /**
     * Returns the owning node reference for a rule.  
     * 
     * @param rule  the rule
     * @return      the owning node reference
     */
    @Auditable(parameters = {"rule"})
    public NodeRef getOwningNodeRef(Rule rule);
    
    /**
     * Returns the owning node reference for an action.  Returns null for an unsaved action or one that is not 
     * parented by a rule.
     * 
     * NOTE: this method is temporary and will be removed in future versions.  It should only be used with good reason.
     * 
     * @param action    the action
     * @return          the owning node reference
     */
    @Auditable(parameters = {"action"})
    public NodeRef getOwningNodeRef(Action action);

    /**
     * Returns the owning node reference for a rule.
     *
     * @param ruleSet The rule set node.
     * @return the owning node reference
     */
    @Auditable (parameters = { "ruleSet" })
    @Experimental
    NodeRef getOwningNodeRef(NodeRef ruleSet);

    /**
     * Indicates whether the passed rule node reference is linked to another
     * rule node.
     * 
     * @param  nodeRef   rule node reference
     * @return boolean   true if linked, false otherwise
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean isLinkedToRuleNode(NodeRef nodeRef);
    
    /**
     * Get the node reference to the rule node which the rule node links to.  Returns null
     * if rules are not linked.
     * 
     * @param nodeRef   node reference of a rule node
     * @return NodeRef  reference to the 
     */
    @Auditable(parameters = {"nodeRef"})
    public NodeRef getLinkedToRuleNode(NodeRef nodeRef);
    
    /**
     * Get a list of the all the rule nodes that link to the passed rule node.  
     * Returns an empty list if none link.
     * 
     * @param nodeRef           node reference of a rule node
     * @return list of rule nodes that link to this passed rule node, empty if none
     */
    @Auditable(parameters = {"nodeRef"})
    public List<NodeRef> getLinkedFromRuleNodes(NodeRef nodeRef);

    /**
     * Get rule set node associated with folder
     *
     * @param folderNodeRef - folder node reference
     * @return node reference of a rule set
     */
    @Auditable(parameters = {"folderNodeRef"})
    @Experimental
    NodeRef getRuleSetNode(final NodeRef folderNodeRef);

    /**
     * Check if rule set is associated (owned/linked/inherited) with the given folder node.
     *
     * @param ruleSetNodeRef - node reference of a rule set
     * @param folderNodeRef - node reference of a folder
     * @return true if rule set is associated with folder
     */
    @Auditable(parameters = {"ruleSetNodeRef", "folderNodeRef"})
    @Experimental
    boolean isRuleSetAssociatedWithFolder(NodeRef ruleSetNodeRef, NodeRef folderNodeRef);

    /**
     * Check if rule's associated parent matches rule set node.
     *
     * @param ruleNodeRef - node reference of a rule
     * @param ruleSetNodeRef - node reference of a rule set
     * @return true if rule is associated with rule set
     */
    @Auditable(parameters = {"ruleNodeRef", "ruleSetNodeRef"})
    @Experimental
    boolean isRuleAssociatedWithRuleSet(final NodeRef ruleNodeRef, final NodeRef ruleSetNodeRef);

    /**
     * Check if other folders are linked to rule set.
     *
     * @param ruleSetNodeRef - node reference of a rule set
     * @return true if others folders are linked to rule set
     */
    @Auditable(parameters = {"ruleSetNodeRef"})
    @Experimental
    boolean isRuleSetShared(final NodeRef ruleSetNodeRef);
}
