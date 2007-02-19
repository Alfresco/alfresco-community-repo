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
package org.alfresco.web.bean.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.web.bean.actions.IHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.rules.handlers.BaseConditionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Bean implementation for the "Edit Rule" wizard
 * 
 * @author gavinc
 */
public class EditRuleWizard extends CreateRuleWizard
{
   private static final Log logger = LogFactory.getLog(EditRuleWizard.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // get hold of the current rule details
      Rule rule = this.rulesBean.getCurrentRule();
      
      if (rule == null)
      {
         throw new AlfrescoRuntimeException("Failed to locate the current rule");
      }
      
      // populate the bean with current values 
      this.type = rule.getRuleTypes().get(0);
      this.title = rule.getTitle();
      this.description = rule.getDescription();
      this.applyToSubSpaces = rule.isAppliedToChildren();
      this.runInBackground = rule.getExecuteAsynchronously();
      this.ruleDisabled = rule.getRuleDisabled();
      
      FacesContext context = FacesContext.getCurrentInstance();
      
      // Get the composite action
      CompositeAction compositeAction = getCompositeAction(rule);
      
      // populate the conditions list with maps of properties representing each condition
      List<ActionCondition> conditions = compositeAction.getActionConditions();
      for (ActionCondition condition : conditions)
      {
         this.currentConditionProperties = new HashMap<String, Serializable>(3);
         this.condition = condition.getActionConditionDefinitionName();
         this.currentConditionProperties.put(PROP_CONDITION_NAME, this.condition);
         this.currentConditionProperties.put(BaseConditionHandler.PROP_CONDITION_NOT, 
               Boolean.valueOf(condition.getInvertCondition()));
         
         IHandler handler = this.conditionHandlers.get(this.condition);
         if (handler != null)
         {
            // use the handler to populate the properties and summary
            handler.prepareForEdit(this.currentConditionProperties, 
                  condition.getParameterValues());
            this.currentConditionProperties.put(PROP_CONDITION_SUMMARY,
                  handler.generateSummary(context, this, this.currentConditionProperties));
         }
         else
         {
            // there's no handler, so we presume it is a no-paramter
            // condition, use the condition title as the summary
            ActionConditionDefinition conditionDef = this.actionService.
                  getActionConditionDefinition(this.condition);
            this.currentConditionProperties.put(PROP_CONDITION_SUMMARY,
                  conditionDef.getTitle());
            // add the no params marker so we can disable the edit action
            this.currentConditionProperties.put(NO_PARAMS_MARKER, "no-params");
         }
         
         // add the populated currentConditionProperties to the list
         this.allConditionsProperties.add(this.currentConditionProperties);
      }
      
      // populate the actions list with maps of properties representing each action
      List<Action> actions = compositeAction.getActions();
      for (Action action : actions)
      {
         this.currentActionProperties = new HashMap<String, Serializable>(3);
         this.action = action.getActionDefinitionName();
         this.currentActionProperties.put(PROP_ACTION_NAME, this.action);
         
         IHandler handler = this.actionHandlers.get(this.action);
         if (handler != null)
         {
            // use the handler to populate the properties and summary
            handler.prepareForEdit(this.currentActionProperties, action.getParameterValues());
            this.currentActionProperties.put(PROP_ACTION_SUMMARY, 
                  handler.generateSummary(context, this, this.currentActionProperties));
         }
         else
         {
            // there's no handler, so we presume it is a no-paramter
            // action, use the action title as the summary
            ActionDefinition actionDef = this.actionService.getActionDefinition(this.action);
            this.currentActionProperties.put(PROP_ACTION_SUMMARY, actionDef.getTitle());
            // add the no params marker so we can disable the edit action
            this.currentActionProperties.put(NO_PARAMS_MARKER, "no-params");
         }
         
         // add the populated currentActionProperties to the list
         this.allActionsProperties.add(this.currentActionProperties);
      }
      
      // reset the current condition
      this.condition = null;
      
      // reset the current action
      this.action = null;
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get hold of the space the rule will apply to and make sure
      // it is actionable
      Node currentSpace = browseBean.getActionSpace();
      
      // get the existing rule
      Rule rule = this.rulesBean.getCurrentRule();
      
      // Get the composite action
      CompositeAction compositeAction = getCompositeAction(rule);
                  
      // remove all the conditions and actions from the current rule
      compositeAction.removeAllActionConditions();
      compositeAction.removeAllActions();
      
      // re-setup the rule
      outcome = setupRule(context, rule, outcome);
      
      // Save the rule
      this.ruleService.saveRule(currentSpace.getNodeRef(), rule);
      
      if (logger.isDebugEnabled())
         logger.debug("Updated rule '" + this.title + "'");
      
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Determines whether the rule type drop down list should be enabled.
    * 
    * @return true as the rule type drop down should be disabled
    */
   public boolean getRuleTypeDisabled()
   {
      return true;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods

}
