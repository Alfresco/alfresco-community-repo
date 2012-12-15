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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.actions.IHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.rules.handlers.BaseConditionHandler;
import org.alfresco.web.bean.rules.handlers.CompositeConditionHandler;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;

/**
 * Bean implementation for the "Create Rule" wizard
 * 
 * @author gavinc
 */
public class CreateRuleWizard extends BaseActionWizard
{
   private static final long serialVersionUID = 6197875728665281192L;
   
   protected static final String PROP_CONDITION_NAME = "conditionName";
   protected static final String PROP_CONDITION_SUMMARY = "conditionSummary";

   private static final String RULE_OUTBOUND = "outbound";
   private static final String ACTION_CHECK_OUT = "check-out";

   transient private RuleService ruleService;
   protected RulesDialog rulesDialog;

   private List<SelectItem> modelTypes; //this is for subtype condition
   private List<SelectItem> mimeTypes; //for checking mime types condition
   private List<SelectItem> types;

   private List<SelectItem> conditions;

   protected Map<String, IHandler> conditionHandlers; //contains UI handlers, i.e. classes that know which JSP to to forward to 

   //This is where all the current condition properties go.  When addConditions is called, 
   //these are saved into allConditionsPropertiesList
   protected Map<String, Serializable> currentConditionProperties;

   transient protected DataModel allConditionsDataModel;

   //   protected List<Map<String, Serializable>> allConditionsProperties;
   //allConditionsProperties needs to be able to store both Map<String, Serializable> and List<Map<String, Serializable>> 
   //(for composite conditions)      
   protected List<Map<String, Serializable>> allConditionsPropertiesList;

   protected String title;
   protected String description;
   protected String type;
   protected String selectedCondition;
   protected boolean runInBackground;
   protected boolean applyToSubSpaces;
   protected boolean editingCondition;
   protected boolean ruleDisabled;
   
   private static final Log logger = LogFactory.getLog(CreateRuleWizard.class);

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.title = null;
      this.description = null;
      this.type = "inbound";
      this.selectedCondition = null;
      this.applyToSubSpaces = false;
      this.runInBackground = false;
      this.ruleDisabled = false;
      this.conditions = null;
      
      this.allConditionsPropertiesList = new ArrayList<Map<String, Serializable>>();
      
      initialiseConditionHandlers();
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("finishImpl called - saving rules");
      
      // get hold of the space the rule will apply to and make sure
      // it is actionable
      Node currentSpace = this.browseBean.getActionSpace();
      
      // create the new rule
      //Rule rule = this.ruleService.createRule(this.getType());
      Rule rule = new Rule();
      rule.setRuleType(this.getType());

      // setup the rule
      outcome = setupRule(context, rule, outcome);
      
      // Save the rule
      this.getRuleService().saveRule(currentSpace.getNodeRef(), rule);
      
      if (logger.isDebugEnabled())
         logger.debug("Added rule '" + this.title + "'");
      
      return outcome;
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      boolean disabled = true;
      int step = Application.getWizardManager().getCurrentStep();
      switch (step)
      {
      case 1:
      {
         disabled = (this.allConditionsDataModel == null || this.allConditionsDataModel.getRowCount() == 0);
         break;
      }
      case 2:
      {
         disabled = (this.allActionsDataModel == null || this.allActionsDataModel.getRowCount() == 0);
         break;
      }
      case 3:
      {
         disabled = (this.title == null || this.title.length() == 0);
         break;
      }
      }

      return disabled;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      if (this.allActionsDataModel != null && this.allActionsDataModel.getRowCount() > 0
            && this.allConditionsDataModel != null && this.allConditionsDataModel.getRowCount() > 0
            && this.title != null && this.title.length() > 0)
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      // create the summary using all the conditions
      StringBuilder conditionsSummary = new StringBuilder();
      for (Map<String, Serializable> props : this.allConditionsPropertiesList)
      {
         conditionsSummary.append(Utils.encode((String)props.get(PROP_CONDITION_SUMMARY)));
         conditionsSummary.append("<br>");
      }
      
      // create the summary using all the actions
      StringBuilder actionsSummary = new StringBuilder();
      for (Map<String, Serializable> props : this.allActionsProperties)
      {
         actionsSummary.append(Utils.encode((String)props.get(PROP_ACTION_SUMMARY)));
         actionsSummary.append("<br>");
      }
      
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      String backgroundYesNo = this.runInBackground ? bundle.getString("yes") : bundle.getString("no");
      String subSpacesYesNo = this.applyToSubSpaces ? bundle.getString("yes") : bundle.getString("no");
      String ruleDisabledYesNo = this.ruleDisabled ? bundle.getString("yes") : bundle.getString("no");

      return buildSummary(new String[]
      { bundle.getString("rule_type"), bundle.getString("name"), bundle.getString("description"),
            bundle.getString("apply_to_sub_spaces"), bundle.getString("run_in_background"),
            bundle.getString("rule_disabled"), bundle.getString("conditions"), bundle.getString("actions") },
            new String[]
            { this.type, Utils.encode(this.title), Utils.encode(this.description), subSpacesYesNo, backgroundYesNo,
                  ruleDisabledYesNo, conditionsSummary.toString(), actionsSummary.toString() });
   }

   @Override
   protected String getErrorMessageId()
   {
      return "error_rule";
   }

   protected CompositeAction getCompositeAction(Rule rule)
   {
      // Get the composite action
      Action ruleAction = rule.getAction();
      if (ruleAction == null)
      {
         throw new AlfrescoRuntimeException("Rule does not have associated action.");
      }
      else if ((ruleAction instanceof CompositeAction) == false)
      {
         throw new AlfrescoRuntimeException("Rules with non-composite actions are not currently supported by the UI");
      }
      return (CompositeAction) ruleAction;
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Determines whether the rule type drop down list should be enabled.
    * 
    * @return false as the rule type drop down should be enabled
    */
   public boolean getRuleTypeDisabled()
   {
      return false;
   }
   
   /**
    * Returns the properties for all the conditions as a JSF DataModel
    * 
    * @return JSF DataModel representing the condition properties
    */
   public DataModel getAllConditionsDataModel()
   {
      if (this.allConditionsDataModel == null)
      {
         this.allConditionsDataModel = new ListDataModel();
      }

      this.allConditionsDataModel.setWrappedData(this.allConditionsPropertiesList);

      return this.allConditionsDataModel;
   }

   /**
    * Returns a list of the types available in the repository
    * 
    * @return List of SelectItem objects
    */
   public List<SelectItem> getModelTypes()
   {
      if ((this.modelTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         ConfigService svc = Application.getConfigService(context);
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("subtypes");
            if (typesCfg != null)
            {
               this.modelTypes = new ArrayList<SelectItem>();
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));

                  // get the display label from config
                  String label = Utils.getDisplayLabel(context, child);

                  // if there wasn't a client based label try and get it from the dictionary
                  if (label == null)
                  {
                     TypeDefinition typeDef = this.getDictionaryService().getType(idQName);
                     if (typeDef != null)
                     {
                        label = typeDef.getTitle(this.getDictionaryService());
                     }
                     else
                     {
                        label = idQName.getLocalName();
                     }
                  }
                  
                  this.modelTypes.add(new SelectItem(idQName.toString(), label));
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.modelTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find 'subtypes' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
      
      return this.modelTypes;
   }
   
   /**
    * Returns a list of mime types in the system
    * 
    * @return List of mime types
    */
   public List<SelectItem> getMimeTypes()
   {
      if (this.mimeTypes == null)
      {
         this.mimeTypes = new ArrayList<SelectItem>(50);
           
         Map<String, String> mimeTypes = getMimetypeService().getDisplaysByMimetype();
         for (String mimeType : mimeTypes.keySet())
         {
            this.mimeTypes.add(new SelectItem(mimeType, mimeTypes.get(mimeType)));
         }
           
         // make sure the list is sorted by the values
         QuickSort sorter = new QuickSort(this.mimeTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
       
      return this.mimeTypes;
   }
   
   /**
    * @return Returns the list of selectable conditions
    */
   public List<SelectItem> getConditions()
   {
      if (this.conditions == null)
      {
         List<ActionConditionDefinition> ruleConditions = this.getActionService().getActionConditionDefinitions();
         this.conditions = new ArrayList<SelectItem>(ruleConditions.size());
         for (ActionConditionDefinition ruleConditionDef : ruleConditions)
         {
            // add to SelectItem list
            this.conditions.add(new SelectItem(ruleConditionDef.getName(), ruleConditionDef.getTitle()));
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.conditions, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the "Select a condition" entry at the beginning of the list
         this.conditions.add(0, new SelectItem("null", Application.getMessage(FacesContext.getCurrentInstance(),
               "select_a_condition")));
      }
      
      return this.conditions;
   }

   /**
    * @return Returns the types of rules that can be defined
    */
   public List<SelectItem> getTypes()
   {
      if (this.types == null)
      {
         List<RuleType> ruleTypes = this.getRuleService().getRuleTypes();
         this.types = new ArrayList<SelectItem>(ruleTypes.size());
         for (RuleType ruleType : ruleTypes)
         {
            this.types.add(new SelectItem(ruleType.getName(), ruleType.getDisplayLabel()));
         }
      }
      
      return shouldFilterTypes() ? filterTypes(this.types) : this.types;
   }
   
   private boolean shouldFilterTypes()
   {
      boolean filter = false;
      
      for (Map<String, Serializable> actionProperty: this.allActionsProperties)
      {
         if (actionProperty.get(PROP_ACTION_NAME).toString().equalsIgnoreCase(ACTION_CHECK_OUT))
         {
            filter = true;
            break;
         }
      }
      
      return filter;
   }
   
   private List<SelectItem> filterTypes(List<SelectItem> types)
   {
      List<SelectItem> filteredTypes = new ArrayList<SelectItem>(types);
      for (Iterator<SelectItem> iterator = filteredTypes.iterator(); iterator.hasNext();)
      {
         SelectItem selectItem = iterator.next();
         if (selectItem.getValue().toString().equalsIgnoreCase(RULE_OUTBOUND))
         {
            iterator.remove();
         }
      }
      
      return filteredTypes;
   }
   
   /**
    * @return Gets the condition settings 
    */
   public Map<String, Serializable> getConditionProperties()
   {
      return this.currentConditionProperties;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   } 

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return title;
   }
   
   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return Returns whether the rule should run in the background
    */
   public boolean getRunInBackground()
   {
      return this.runInBackground;
   }

   /**
    * @param runInBackground Sets whether the rule should run in the background
    */
   public void setRunInBackground(boolean runInBackground)
   {
      this.runInBackground = runInBackground;
   }

   /**
    * @return Returns whether the rule should be applied to sub spaces i.e. if it gets inherited
    */
   public boolean getApplyToSubSpaces()
   {
      return this.applyToSubSpaces;
   }

   /**
    * @param applyToSubSpaces Sets whether the rule will get applied to sub spaces
    */
   public void setApplyToSubSpaces(boolean applyToSubSpaces)
   {
      this.applyToSubSpaces = applyToSubSpaces;
   }

   /**
    * @return Returns whether the rule is disabled or not.
    */
   public boolean getRuleDisabled()
   {
      return this.ruleDisabled;
   }
   
   /**
    * @param ruleDisabled Sets whether the rule is disabled or not
    */
   public void setRuleDisabled(boolean ruleDisabled)
   {
      this.ruleDisabled = ruleDisabled;
   }
   
   /**
    * @return Returns the type.
    */
   public String getType()
   {
      return type;
   }

   /**
    * @param type The type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }
   
   /**
    * @return Returns the selected condition
    */
   public String getCondition()
   {
      return this.selectedCondition;
   }

   /**
    * @param condition Sets the selected condition
    */
   public void setCondition(String condition)
   {
      this.selectedCondition = condition;
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Displays the settings page for the current condition being added (if required)
    */
   public void promptForConditionValues()
   {
      // set the flag to show we are creating a new condition
      this.editingCondition = false;
      
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();
      String viewId = null;
      
      this.currentConditionProperties = new HashMap<String, Serializable>(3);
      this.currentConditionProperties.put(PROP_CONDITION_NAME, this.selectedCondition);
      this.currentConditionProperties.put(BaseConditionHandler.PROP_CONDITION_NOT, Boolean.FALSE);

      // get the handler for the condition, if there isn't one we presume it
      // is a no-parameter condition
      IHandler handler = this.conditionHandlers.get(this.selectedCondition);
      if (handler != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Found Handler for selected condition - '" + this.selectedCondition + "'");

         // setup any UI defaults the condition may have and get the location of
         // the JSP used to collect the parameters
         handler.setupUIDefaults(this.currentConditionProperties);
         viewId = handler.getJSPPath();
         if (logger.isDebugEnabled())
            logger.debug("Handler returned JSP page- '" + viewId + "'  Handler Type " + handler.getClass().toString());
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Did Not Find a handler for selected condition - '" + this.selectedCondition + "'");

         // just add the action to the list and use the title as the summary
         ActionConditionDefinition conditionDef = this.getActionService()
               .getActionConditionDefinition(this.selectedCondition);
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, conditionDef.getTitle());
         this.currentConditionProperties.put(BaseConditionHandler.PROP_CONDITION_NOT, Boolean.FALSE);
         // add the no params marker so we can disable the edit action
         this.currentConditionProperties.put(NO_PARAMS_MARKER, "no-params");
         this.allConditionsPropertiesList.add(this.currentConditionProperties);

         // come back to the same page we're on now as there are no params to collect
         viewId = this.returnViewId;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Currently creating '" + this.selectedCondition + "' condition");
      
      // go to the page to collect the settings
      goToPage(context, viewId);
   }
   
   /**
    * Sets up the context for editing existing condition values 
    */
   @SuppressWarnings("unchecked")
   public void editCondition()
   {
      Map conditionToEdit = (Map) this.allConditionsDataModel.getRowData();
      editCondition(conditionToEdit);
   }

   protected void editCondition(Map conditionToEdit)
   {
      // set the flag to show we are editing a condition
      this.editingCondition = true;

      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      this.selectedCondition = (String) conditionToEdit.get(PROP_CONDITION_NAME);
      this.currentConditionProperties = conditionToEdit;

      if (logger.isDebugEnabled())
         logger.debug("Editing Condition '" + selectedCondition + "'");

      // remember the page we're on
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();

      // go to the condition page (as there is an edit option visible,
      // there must be a handler for the condition so we don't check)
      goToPage(context, this.conditionHandlers.get(this.selectedCondition).getJSPPath());
   }

   /**
    * Adds the condition just setup by the user to the list of conditions for the rule
    */
   public void addCondition()
   {
      FacesContext context = FacesContext.getCurrentInstance();

      if (logger.isDebugEnabled())
         logger.debug("Adding Condition '" + selectedCondition + "'");

      IHandler handler = this.conditionHandlers.get(this.selectedCondition);

      // this is called from the actions page so there must be a handler
      // present so there's no need to check for null
      String summary = handler.generateSummary(context, this, this.currentConditionProperties);

      if (summary != null)
      {
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, summary);
      }
      if (logger.isDebugEnabled())
         logger.debug("Generated Summary - [" + summary + "] + selectedCondition " + this.selectedCondition);

      if (this.editingCondition == false)
      {
         this.allConditionsPropertiesList.add(this.currentConditionProperties);

      }

      // reset the action drop down
      this.selectedCondition = null;

      // refresh the wizard
      goToPage(context, this.returnViewId);
   }

   /**
    * Removes the requested condition from the list
    */
   public void removeCondition()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map conditionToRemove = (Map) this.allConditionsDataModel.getRowData();
      this.allConditionsPropertiesList.remove(conditionToRemove);
      
      // reset the action drop down
      this.selectedCondition = null;

      // refresh the wizard
      FacesContext context = FacesContext.getCurrentInstance();
      goToPage(context, context.getViewRoot().getViewId());
   }
   
   /**
    * Cancels the addition of the condition
    */
   public void cancelAddCondition()
   {
      if (this.editingCondition == false)
      {
         this.currentConditionProperties.clear();
      }
      
      // reset the action drop down
      this.selectedCondition = null;
      
      // refresh the wizard
      goToPage(FacesContext.getCurrentInstance(), this.returnViewId);
   }
   
   
   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * @param ruleService Sets the rule service to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   protected RuleService getRuleService()
   {
      if (ruleService == null)
      {
         ruleService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getRuleService();
      }
      return ruleService;
   }
   
   /**
    * Sets the rulesDialog instance to be used by the wizard in edit mode
    * 
    * @param rulesDialog The rulesDialog
    */
   public void setRulesDialog(RulesDialog RulesDialog)
   {
      this.rulesDialog = RulesDialog;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Sets up the given rule using the current state of the wizard
    * 
    * @param context FacesContext
    * @param rule The rule to setup
    * @param outcome The default outcome
    * @return The outcome
    */
   @SuppressWarnings("unchecked")
   protected String setupRule(FacesContext context, Rule rule, String outcome)
   {
      if (logger.isDebugEnabled())
         logger.debug("Saving Rules - setupRule");

      // setup the rule and add it to the space
      rule.setTitle(this.title);
      rule.setDescription(this.description);
      rule.applyToChildren(this.applyToSubSpaces);
      rule.setExecuteAsynchronously(this.runInBackground);
      rule.setRuleDisabled(this.ruleDisabled);
      
      CompositeAction compositeAction = this.getActionService().createCompositeAction();
      rule.setAction(compositeAction);
      int i = 1;
      // add all the conditions to the rule
      for (Object condParamsObj : this.allConditionsPropertiesList)
      {
         if (logger.isDebugEnabled())
            logger.debug("Saving Condition " + i++ + " of " + this.allConditionsPropertiesList.size());

         Map<String, Serializable> uiConditionParams = (Map<String, Serializable>) condParamsObj;

         ActionCondition condition = createCondition(uiConditionParams);

         if (condition instanceof CompositeActionCondition)
         {
            CompositeActionCondition compositeCondition = (CompositeActionCondition) condition;

            List<Map<String, Serializable>> subconditionProps = (List<Map<String, Serializable>>) uiConditionParams
                  .get(CompositeConditionHandler.PROP_COMPOSITE_CONDITION);
            int j = 1;
            compositeCondition.setORCondition(((Boolean)uiConditionParams.get(CompositeConditionHandler.PROP_CONDITION_OR)).booleanValue());
            compositeCondition.setInvertCondition((((Boolean)uiConditionParams.get(CompositeConditionHandler.PROP_CONDITION_NOT)).booleanValue()));
            
            for (Map<String, Serializable> props : subconditionProps)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Saving Composite Condition " + j++ + " of " + subconditionProps.size());
               
               compositeCondition.addActionCondition(createCondition(props));
            }
         }
         compositeAction.addActionCondition(condition);
      }
      
      // add all the actions to the rule
      for (Map<String, Serializable> actionParams : this.allActionsProperties)
      {
         // use the base class version of buildActionParams(), but for this
         // we need
         // to setup the currentActionProperties and action variables
         String actionName = (String) actionParams.get(PROP_ACTION_NAME);
         this.action = actionName;
         
         // get the action handler to prepare for the save
         Map<String, Serializable> repoActionParams = new HashMap<String, Serializable>();
         IHandler handler = this.actionHandlers.get(this.action);
         if (handler != null)
         {
            handler.prepareForSave(actionParams, repoActionParams);
         }
         
         // add the action to the rule
         Action action = this.getActionService().createAction(actionName);
         action.setParameterValues(repoActionParams);
         compositeAction.addAction(action);
      }
      
      return outcome;
   }

   private ActionCondition createCondition(Map<String, Serializable> uiConditionParams)
   {
      // get the condition handler to prepare for the save

      String conditionName = (String) uiConditionParams.get(PROP_CONDITION_NAME);

      Map<String, Serializable> repoCondParams = new HashMap<String, Serializable>();
      if (logger.isDebugEnabled())
      {
         logger.debug("\tSaving " + conditionName);
      }
      IHandler handler = this.conditionHandlers.get(conditionName);
      if (handler != null)
      {
         handler.prepareForSave(uiConditionParams, repoCondParams);
      }

      // add the condition to the rule
      ActionCondition condition = this.getActionService().createActionCondition(conditionName);
      condition.setParameterValues(repoCondParams);

      // specify whether the condition result should be inverted
      Boolean not = (Boolean) uiConditionParams.get(BaseConditionHandler.PROP_CONDITION_NOT);
      if (not == null)
      {
         logger.warn("Property missing NOT parameter value (currently null)");
         not = Boolean.TRUE;
      }
      condition.setInvertCondition(((Boolean) not).booleanValue());
      return condition;
   }
   
   /**
    * Initialises the condition handlers from the current configuration.
    */
   protected void initialiseConditionHandlers()
   {
      if ((this.conditionHandlers == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService svc = Application.getConfigService(FacesContext.getCurrentInstance());
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement conditionHandlerCfg = wizardCfg.getConfigElement("condition-handlers");
            if (conditionHandlerCfg != null)
            {
               this.conditionHandlers = new HashMap<String, IHandler>(20);
               
               // instantiate each handler and store in the map
               for (ConfigElement child : conditionHandlerCfg.getChildren())
               {
                  String conditionName = child.getAttribute("name");
                  String handlerClass = child.getAttribute("class");
                  
                  if (conditionName != null && conditionName.length() > 0 && handlerClass != null
                        && handlerClass.length() > 0)
                  {
                     try
                     {
                        @SuppressWarnings("unchecked")
                        Class klass = Class.forName(handlerClass);
                        IHandler handler = (IHandler) klass.newInstance();
                        this.conditionHandlers.put(conditionName, handler);
                     }
                     catch (Exception e)
                     {
                        throw new AlfrescoRuntimeException("Failed to setup condition handler for '" + conditionName
                              + "'", e);
                     }
                  }
               }
            }
            else
            {
               logger.warn("Could not find 'condition-handlers' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Action Wizards' configuration section");
         }
      }
   }
   
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      this.allConditionsDataModel = new ListDataModel();
      this.allConditionsDataModel.setWrappedData(this.allConditionsPropertiesList);
   }

}
