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
package org.alfresco.web.bean.rules;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.web.bean.actions.IHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.rules.handlers.BaseConditionHandler;
import org.alfresco.web.bean.rules.handlers.CompositeConditionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Edit Rule" wizard
 * 
 * @author gavinc
 */
public class EditRuleWizard extends CreateCompositeRuleWizard
{
   private static final long serialVersionUID = -7222762769396254445L;
   
   private static final Log logger = LogFactory.getLog(EditRuleWizard.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation

   /*  Loads up conditions and actions from the repository
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // get hold of the current rule details
      Rule rule = this.rulesDialog.getCurrentRule();
      
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

      populateConditions(context, compositeAction);

      populateActions(context, compositeAction);

      // reset the current condition
      this.selectedCondition = null;

      // reset the current action
      this.action = null;
   }

   protected void populateActions(FacesContext context, CompositeAction compositeAction)
   {
      // populate the actions list with maps of properties representing each action
      List<Action> actions = compositeAction.getActions();
      for (Action action : actions)
      {
         this.currentActionProperties = new HashMap<String, Serializable>(3);
         this.currentEmailRecipientsDataModel = null;
         this.action = action.getActionDefinitionName();
         this.currentActionProperties.put(PROP_ACTION_NAME, this.action);
         
         IHandler handler = this.actionHandlers.get(this.action);
         if (handler != null)
         {
            // use the handler to populate the properties and summary
            handler.prepareForEdit(this.currentActionProperties, action.getParameterValues());
            this.currentActionProperties.put(PROP_ACTION_SUMMARY, handler.generateSummary(context, this,
                  this.currentActionProperties));
         }
         else
         {
            // there's no handler, so we presume it is a no-parameter
            // action, use the action title as the summary
            ActionDefinition actionDef = this.getActionService().getActionDefinition(this.action);
            this.currentActionProperties.put(PROP_ACTION_SUMMARY, actionDef.getTitle());
            // add the no params marker so we can disable the edit action
            this.currentActionProperties.put(NO_PARAMS_MARKER, "no-params");
         }

         // add the populated currentActionProperties to the list
         this.allActionsProperties.add(this.currentActionProperties);
      }
   }

   protected void populateConditions(FacesContext context, CompositeAction compositeAction)
   {
      // populate the conditions list with maps of properties representing each condition
      List<ActionCondition> conditions = compositeAction.getActionConditions();
      for (ActionCondition toplevel_condition : conditions)
      {
         this.selectedCondition = toplevel_condition.getActionConditionDefinitionName();
         this.currentConditionProperties = new HashMap<String, Serializable>();
            
         if (logger.isDebugEnabled())
            logger.debug("Preparing for Edit Condition " + this.selectedCondition);

         if (toplevel_condition instanceof CompositeActionCondition)
         {
            if (logger.isDebugEnabled())
               logger.debug("\tDetected CompositeCondition");

            CompositeActionCondition compositeCondition = (CompositeActionCondition) toplevel_condition;
            this.currentCompositeConditionPropertiesList = new ArrayList<Map<String, Serializable>>();
            
            //TODO:  add OR property

            for (ActionCondition subcondition : compositeCondition.getActionConditions())
            {
               if (logger.isDebugEnabled())
                  logger.debug("\tSetting ... SubConditions " + subcondition.getActionConditionDefinitionName());

               this.selectedCondition = subcondition.getActionConditionDefinitionName();
               Map<String, Serializable> subConditionProperties = new HashMap<String, Serializable>();
               populateProperties(context, subcondition, subConditionProperties);
               this.currentCompositeConditionPropertiesList.add(subConditionProperties);
            }
            
            this.selectedCondition = CompositeConditionHandler.NAME;

            this.currentConditionProperties.put(CompositeConditionHandler.PROP_COMPOSITE_CONDITION,
                  (Serializable) this.currentCompositeConditionPropertiesList);

            populateProperties(context, compositeCondition, currentConditionProperties);            
            
         } else 
            populateProperties(context, toplevel_condition, this.currentConditionProperties);

         // add the populated currentConditionProperties to the list
         this.allConditionsPropertiesList.add(this.currentConditionProperties);

         printConditionState();

      }
   }


   protected void populateProperties(FacesContext context, ActionCondition condition,
         Map<String, Serializable> uiConditionProperties)
   {
      uiConditionProperties.put(PROP_CONDITION_NAME, this.selectedCondition);
      uiConditionProperties.put(BaseConditionHandler.PROP_CONDITION_NOT, Boolean.valueOf(condition.getInvertCondition()));

      IHandler handler = this.conditionHandlers.get(this.selectedCondition);
      if (handler != null)
      {
         // use the handler to populate the properties and summary
         handler.prepareForEdit(uiConditionProperties, condition.getParameterValues());
         uiConditionProperties.put(PROP_CONDITION_SUMMARY, handler
               .generateSummary(context, this, uiConditionProperties));
      } else
      {
         // there's no handler, so we presume it is a no-parameter
         // condition, use the condition title as the summary
         ActionConditionDefinition conditionDef = this.getActionService().getActionConditionDefinition(this.selectedCondition);
         uiConditionProperties.put(PROP_CONDITION_SUMMARY, conditionDef.getTitle());
         // add the no params marker so we can disable the edit action
         uiConditionProperties.put(NO_PARAMS_MARKER, "no-params");
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get hold of the space the rule will apply to and make sure
      // it is actionable
      Node currentSpace = browseBean.getActionSpace();
      
      // get the existing rule
      Rule rule = this.rulesDialog.getCurrentRule();
      
      // Get the composite action
      CompositeAction compositeAction = getCompositeAction(rule);
                  
      // remove all the conditions and actions from the current rule
      compositeAction.removeAllActionConditions();
      compositeAction.removeAllActions();
      
      // re-setup the rule
      outcome = setupRule(context, rule, outcome);
      
      // Save the rule
      this.getRuleService().saveRule(currentSpace.getNodeRef(), rule);
      
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
