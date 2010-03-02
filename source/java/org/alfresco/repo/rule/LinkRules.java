/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

import java.util.List;

import javax.swing.text.html.parser.ContentModel;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Action implementation to link the rules from one folder to another
 * 
 * @author Roy Wetherall
 */
public class LinkRules extends ActionExecuterAbstractBase
{
    public static final String NAME = "link-rules";
    public static final String PARAM_LINK_FROM_NODE = "link_from_node";
    
    private NodeService nodeService;
    
    private RuntimeRuleService ruleService;
    
    public void setRuleService(RuntimeRuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true)
        {
            // The actioned upon node is the rule folder we are interested in
               // this should not already have rules associated with it
            if (nodeService.hasAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES) == true)
            {
                throw new AlfrescoRuntimeException("The link to node already has rules.");
            }
            
            // Link to folder is passed as a parameter
              // this should have rules already specified
            NodeRef linkedFromNodeRef = (NodeRef)action.getParameterValue(PARAM_LINK_FROM_NODE);
            if (nodeService.hasAspect(linkedFromNodeRef, RuleModel.ASPECT_RULES) == false)
            {
                throw new AlfrescoRuntimeException("The link from node has no rules to link.");
            }
            
            // Create the destination folder as a secondary child of the first
            NodeRef ruleSetNodeRef = ruleService.getSavedRuleFolderAssoc(linkedFromNodeRef).getChildRef();
            nodeService.addChild(actionedUponNodeRef, ruleSetNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
            nodeService.addAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES, null);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(
                new ParameterDefinitionImpl(PARAM_LINK_FROM_NODE,
                DataTypeDefinition.NODE_REF, 
                true,
                getParamDisplayLabel(PARAM_LINK_FROM_NODE)));
    }
}
