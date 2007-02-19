/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;

/**
 * Patch to apply the model changes made when decoupling actions from rules.
 * 
 * @author Roy Wetherall
 */
public class ActionRuleDecouplingPatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.actionRuleDecouplingPatch.result";
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        // Get a reference to the spaces store
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        
        // Get all the node's of type rule in the store
        int updateCount = 0;
        ResultSet resultSet = this.searchService.query(storeRef, "lucene", "TYPE:\"" + RuleModel.TYPE_RULE + "\"");
        for (NodeRef origRuleNodeRef : resultSet.getNodeRefs())
        {
            // Check that this rule need updated
            if (!this.nodeService.exists(origRuleNodeRef))
            {
                continue;
            }
            Map<QName, Serializable> origProperties = this.nodeService.getProperties(origRuleNodeRef);
            if (origProperties.containsKey(RuleModel.PROP_EXECUTE_ASYNC) == false)
            {
                // 1) Change the type of the rule to be a composite action
                this.nodeService.setType(origRuleNodeRef, ActionModel.TYPE_COMPOSITE_ACTION);
                
                // 2) Create a new rule node
                ChildAssociationRef parentRef = this.nodeService.getPrimaryParent(origRuleNodeRef);
                NodeRef newRuleNodeRef = this.nodeService.createNode(
                        parentRef.getParentRef(),
                        parentRef.getTypeQName(),
                        parentRef.getQName(),
                        RuleModel.TYPE_RULE).getChildRef();
                
                // 3) Move the origional rule under the new rule
                this.nodeService.moveNode(
                        origRuleNodeRef,
                        newRuleNodeRef,
                        RuleModel.ASSOC_ACTION,
                        RuleModel.ASSOC_ACTION);
                
                // 4) Move the various properties from the origional, onto the new rule
                Map<QName, Serializable> newProperties = this.nodeService.getProperties(newRuleNodeRef);
                
                // Set the rule type, execute async and applyToChildren properties on the rule
                Serializable ruleType = origProperties.get(RuleModel.PROP_RULE_TYPE);
                origProperties.remove(RuleModel.PROP_RULE_TYPE);
                newProperties.put(RuleModel.PROP_RULE_TYPE, ruleType);
                Serializable executeAsync = origProperties.get(ActionModel.PROP_EXECUTE_ASYNCHRONOUSLY);
                origProperties.remove(ActionModel.PROP_EXECUTE_ASYNCHRONOUSLY);
                newProperties.put(RuleModel.PROP_EXECUTE_ASYNC, executeAsync);
                Serializable applyToChildren = origProperties.get(RuleModel.PROP_APPLY_TO_CHILDREN);
                origProperties.remove(RuleModel.PROP_APPLY_TO_CHILDREN);
                newProperties.put(RuleModel.PROP_APPLY_TO_CHILDREN, applyToChildren);                
                origProperties.remove(QName.createQName(RuleModel.RULE_MODEL_URI, "owningNodeRef"));
                
                // Move the action and description values from the composite action onto the rule
                Serializable title = origProperties.get(ActionModel.PROP_ACTION_TITLE);
                origProperties.remove(ActionModel.PROP_ACTION_TITLE);
                Serializable description = origProperties.get(ActionModel.PROP_ACTION_DESCRIPTION);
                origProperties.remove(ActionModel.PROP_ACTION_DESCRIPTION);
                newProperties.put(ContentModel.PROP_TITLE, title);
                newProperties.put(ContentModel.PROP_DESCRIPTION, description);
                
                // Set the updated property values
                this.nodeService.setProperties(origRuleNodeRef, origProperties);
                this.nodeService.setProperties(newRuleNodeRef, newProperties);
                
                // Increment the update count
                updateCount++;
            }
        }
        
        // Done
        String msg = I18NUtil.getMessage(MSG_RESULT, updateCount);
        return msg;
    }

}
