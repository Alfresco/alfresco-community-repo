/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.rule;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * Action implementation to unlink the rules from one folder to another
 * 
 * @author Roy Wetherall
 */
public class UnlinkRules extends ActionExecuterAbstractBase
{
    /** Constants */
    public static final String NAME = "unlink-rules";
    
    /** Node service */
    private NodeService nodeService;
    
    /** Runtime rule service */
    private RuntimeRuleService ruleService;
    
    /**
     * Set rule service
     * 
     * @param ruleService   rule service
     */
    public void setRuleService(RuntimeRuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set node service
     * 
     * @param nodeService   node service
     */
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
        // Check that the actioned upon node has the rules aspect applied
        if (nodeService.hasAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES) == true)
        {                 
            // Get the rule node the actioned upon node is linked to
            NodeRef linkedToNode = ((RuleService)ruleService).getLinkedToRuleNode(actionedUponNodeRef);
            if (linkedToNode != null)
            {
                NodeRef ruleFolder = ruleService.getSavedRuleFolderAssoc(linkedToNode).getChildRef();
                nodeService.removeChild(actionedUponNodeRef, ruleFolder);
                nodeService.removeAspect(actionedUponNodeRef, RuleModel.ASPECT_RULES);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
