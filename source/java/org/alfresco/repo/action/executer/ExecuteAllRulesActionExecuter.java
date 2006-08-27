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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * This action executes all rules present on the actioned upon node 
 * 
 * @author Roy Wetherall
 */
public class ExecuteAllRulesActionExecuter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
	public static final String NAME = "execute-all-rules";
    public static final String PARAM_EXECUTE_INHERITED_RULES = "execute-inherited-rules";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
    
    /**
     * The rule service
     */
    private RuleService ruleService;
    
    /**
     * The action service
     */
    private ActionService actionService;
    
    /** The dictionary Service */
    private DictionaryService dictionaryService;
	
    /**
     * Set the node service
     * 
     * @param nodeService  the node service
     */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
    
    /**
     * Set the rule service
     * 
     * @param ruleService   the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set the action service
     * 
     * @param actionService     the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Sets the dictionary service
     * 
     * @param dictionaryService     the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
		if (this.nodeService.exists(actionedUponNodeRef) == true)
		{
            // Get the parameter value
            boolean includeInherited = false;
            Boolean includeInheritedValue = (Boolean)ruleAction.getParameterValue(PARAM_EXECUTE_INHERITED_RULES);
            if (includeInheritedValue != null)
            {
                includeInherited = includeInheritedValue.booleanValue();
            }
            
		    // Get the rules
            List<Rule> rules = this.ruleService.getRules(actionedUponNodeRef, includeInherited);
            if (rules != null && rules.isEmpty() == false)
            {
                // Get the child nodes for the actioned upon node
                List<ChildAssociationRef> children = this.nodeService.getChildAssocs(actionedUponNodeRef);
                for (ChildAssociationRef childAssoc : children)
                {
                    // Get the child node reference
                    NodeRef child = childAssoc.getChildRef();
                    
                    // Only execute rules on non-system folders
                    if (this.dictionaryService.isSubClass(this.nodeService.getType(child), ContentModel.TYPE_SYSTEM_FOLDER) == false)
                    {
                        for (Rule rule : rules)
                        {                     
                            Action action = rule.getAction();
                            if (action != null)
                            {
                                this.actionService.executeAction(action, child);
                            }
                        }
                    }
                }
            }
		}
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_EXECUTE_INHERITED_RULES, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_EXECUTE_INHERITED_RULES)));
    }
}
