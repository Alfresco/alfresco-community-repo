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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;

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
    public static final String PARAM_RUN_ALL_RULES_ON_CHILDREN = "run-all-rules-on-children";
	
	/**
	 * The node service
	 */
	private NodeService nodeService;
    
    /**
     * The rule service
     */
    private RuleService ruleService;
    
    /**
     * The runtime rule service
     */
    private RuntimeRuleService runtimeRuleService;
    
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
     * Set the runtime rule service
     * 
     * @param runtimeRuleService the runtime rule service
     */
    public void setRuntimeRuleService(RuntimeRuleService runtimeRuleService)
    {
       this.runtimeRuleService = runtimeRuleService;
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
            
            boolean runAllChildren = false;
            Boolean runAllChildrenValue = (Boolean)ruleAction.getParameterValue(PARAM_RUN_ALL_RULES_ON_CHILDREN);
            if (runAllChildrenValue != null)
            {
                runAllChildren = runAllChildrenValue.booleanValue();
            }
            
		    // Get the rules
            List<Rule> rules = ruleService.getRules(actionedUponNodeRef, includeInherited);
            if (rules != null && rules.isEmpty() == false)
            {
                // Get the child nodes for the actioned upon node
                List<ChildAssociationRef> children = nodeService.getChildAssocs(actionedUponNodeRef);
                for (ChildAssociationRef childAssoc : children)
                {
                    // Get the child node reference
                    NodeRef child = childAssoc.getChildRef();
                    
                    // Only execute rules on non-system folders
                    QName childType = nodeService.getType(child);
                    if (dictionaryService.isSubClass(childType, ContentModel.TYPE_SYSTEM_FOLDER) == false)
                    {
                        for (Rule rule : rules)
                        {       
                           // Only re-apply rules that are enabled
                           if (rule.getRuleDisabled() == false)
                           {
                               Action action = rule.getAction();
                               if (action != null)
                               {
                                  runtimeRuleService.addRulePendingExecution(actionedUponNodeRef, child, rule);
                               }
                           }
                        }
                        
                        // If the child is a folder and we have asked to run rules on children
                        if (runAllChildren == true &&
                            dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER) == true)
                        {
                            // Recurse with the child folder
                            executeImpl(ruleAction, child);
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
        paramList.add(new ParameterDefinitionImpl(PARAM_RUN_ALL_RULES_ON_CHILDREN, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_RUN_ALL_RULES_ON_CHILDREN)));
    }
}
