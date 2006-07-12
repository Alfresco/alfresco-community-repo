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
package org.alfresco.service.cmr.rule;

import java.util.List;

import org.alfresco.service.Auditable;
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
     * Indicates wether the rules for a given node are enabled or not.  If the 
     * rules are not enabled then they will not be executed.
     * 
     * @param nodeRef       the node reference
     * @return              true if the rules are enabled, false otherwise
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public boolean rulesEnabled(NodeRef nodeRef);
    
    /**
     * Disables the rules for a given node reference.  When the rules are disabled they
     * will not execute.
     * 
     * @param nodeRef  the node reference
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void disableRules(NodeRef nodeRef);
    
    /**
     * Enables the rules for a given node reference.  When the rules are enabled they
     * will execute as usual.  By default all rules are enabled.
     * 
     * @param nodeRef   the node reference
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void enableRules(NodeRef nodeRef);
    
    /**
     * Disables a rule, preventing it from being fired.
     * 
     * @param rule  the rule to disable
     */
    @Auditable(parameters = {"rule"})
    public void disableRule(Rule rule);
    
    /**
     * Enables a rule previously disabled.
     * 
     * @param rule  the rule to enable
     */
    @Auditable(parameters = {"rule"})
    public void enableRule(Rule rule);

    /**
     * Indicates whether the node in question has any rules associated with it.
     * 
     * @param nodeRef 	the node reference
     * @return 			true if the node has rules associated, false otherwise
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public List<Rule> getRules(NodeRef nodeRef);

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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "includeInhertied"})
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
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "includeInhertiedRuleType", "ruleTypeName"})
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInhertiedRuleType, String ruleTypeName);

    /**
     * Count the number of rules associated with an actionable node.
     *  
     * @param nodeRef                   the node reference
     * @return                          a list of the rules associated with the node
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public int countRules(NodeRef nodeRef);
    
    /**
     * Get the rule given its id.
     * 
     * @param nodeRef the node reference
     * @param ruleId the rule id
     * @return the rule corresponding ot the id
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "ruleId"})
    public Rule getRule(NodeRef nodeRef, String ruleId);
	
	/**
     * Helper method to create a new rule.
     * <p>
     * Call add rule once the details of the rule have been specified in order
     * to associate the rule with a node reference.
     * 
     * @param ruleTypeName	the name of the rule type
     * @return 				the created rule
     */
    @Auditable(parameters = {"ruleTypeName"})
    public Rule createRule(String ruleTypeName);

    /**
     * Saves the details of the rule to the specified node reference.
     * <p>	
     * If the rule is already associated with the node, the details are updated
     * with those specified.
     * 
     * @param nodeRef
     * @param rule
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "rule"})
    public void saveRule(NodeRef nodeRef, Rule rule);
        
    /**
     * Removes a rule from the given rule actionable node
     * 
     * @param nodeRef  the actionable node reference
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "rule"})
    public void removeRule(NodeRef nodeRef, Rule rule);
    
    /**
     * Removes all the rules associated with an actionable node
     * 
     * @param nodeRef   the actionable node reference
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void removeAllRules(NodeRef nodeRef);
}
