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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.CompositeAction;
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
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Create Rule" wizard
 * 
 * @author gavinc
 */
public class CreateRuleWizard extends BaseActionWizard
{
   protected static final String PROP_CONDITION_NAME = "conditionName";
   protected static final String PROP_CONDITION_SUMMARY = "conditionSummary";
   
   protected RuleService ruleService;
   protected RulesBean rulesBean;
   
   private List<SelectItem> modelTypes;
   private List<SelectItem> mimeTypes;
   private List<SelectItem> types;
   private List<SelectItem> conditions;
   
   protected Map<String, IHandler> conditionHandlers;
   protected Map<String, Serializable> currentConditionProperties;
   protected List<Map<String, Serializable>> allConditionsProperties;

   protected DataModel allConditionsDataModel;
   
   protected String title;
   protected String description;
   protected String type;
   protected String condition;
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
      this.condition = null;
      this.applyToSubSpaces = false;
      this.runInBackground = false;
      this.ruleDisabled = false;
      this.conditions = null;
      
      this.allConditionsProperties = new ArrayList<Map<String, Serializable>>();
      
      initialiseConditionHandlers();
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
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
      this.ruleService.saveRule(currentSpace.getNodeRef(), rule);
      
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
      switch(step)
      {
         case 1:
         {
            disabled = (this.allConditionsDataModel == null || 
                        this.allConditionsDataModel.getRowCount() == 0);
            break;
         }
         case 2:
         {
            disabled = (this.allActionsDataModel == null || 
                        this.allActionsDataModel.getRowCount() == 0);
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
      if (this.allActionsDataModel != null && 
          this.allActionsDataModel.getRowCount() > 0 &&
          this.allConditionsDataModel != null && 
          this.allConditionsDataModel.getRowCount() > 0 &&
          this.title != null && this.title.length() > 0)
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
      for (Map<String, Serializable> props : this.allConditionsProperties)
      {
         conditionsSummary.append(props.get(PROP_CONDITION_SUMMARY));
         conditionsSummary.append("<br/>");
      }
      
      // create the summary using all the actions
      StringBuilder actionsSummary = new StringBuilder();
      for (Map<String, Serializable> props : this.allActionsProperties)
      {
         actionsSummary.append(props.get(PROP_ACTION_SUMMARY));
         actionsSummary.append("<br/>");
      }
      
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      String backgroundYesNo = this.runInBackground ? bundle.getString("yes") : bundle.getString("no");
      String subSpacesYesNo = this.applyToSubSpaces ? bundle.getString("yes") : bundle.getString("no");
      String ruleDisabledYesNo = this.ruleDisabled ? bundle.getString("yes") : bundle.getString("no");
      
      return buildSummary(
            new String[] {bundle.getString("rule_type"), bundle.getString("name"), bundle.getString("description"),
                          bundle.getString("apply_to_sub_spaces"), bundle.getString("run_in_background"), bundle.getString("rule_disabled"),
                          bundle.getString("conditions"), bundle.getString("actions")},
            new String[] {this.type, this.title, this.description, subSpacesYesNo, backgroundYesNo, ruleDisabledYesNo,
                          conditionsSummary.toString(), actionsSummary.toString()});
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
       return (CompositeAction)ruleAction;
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
      
      this.allConditionsDataModel.setWrappedData(this.allConditionsProperties);
      
      return this.allConditionsDataModel;
   }
   
   /**
    * Returns a list of the types available in the repository
    * 
    * @return List of SelectItem objects
    */
   public List<SelectItem> getModelTypes()
   {
      if (this.modelTypes == null)
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
                     TypeDefinition typeDef = this.dictionaryService.getType(idQName);
                     if (typeDef != null)
                     {
                        label = typeDef.getTitle();
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
           
           Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
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
         List<ActionConditionDefinition> ruleConditions = this.actionService.getActionConditionDefinitions();
         this.conditions = new ArrayList<SelectItem>(ruleConditions.size());
         for (ActionConditionDefinition ruleConditionDef : ruleConditions)
         {
            // add to SelectItem list
            this.conditions.add(new SelectItem(ruleConditionDef.getName(), 
                  ruleConditionDef.getTitle()));
         }
         
         // make sure the list is sorted by the label
         QuickSort sorter = new QuickSort(this.conditions, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         // add the "Select a condition" entry at the beginning of the list
         this.conditions.add(0, new SelectItem("null", 
               Application.getMessage(FacesContext.getCurrentInstance(), "select_a_condition")));
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
         List<RuleType> ruleTypes = this.ruleService.getRuleTypes();
         this.types = new ArrayList<SelectItem>(ruleTypes.size());
         for (RuleType ruleType : ruleTypes)
         {
            this.types.add(new SelectItem(ruleType.getName(), ruleType.getDisplayLabel()));
         }
      }
      
      return this.types;
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
      return this.condition;
   }

   /**
    * @param condition Sets the selected condition
    */
   public void setCondition(String condition)
   {
      this.condition = condition;
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
      
      HashMap<String, Serializable> condProps = new HashMap<String, Serializable>(3);
      condProps.put(PROP_CONDITION_NAME, this.condition);
      this.currentConditionProperties = condProps;
      
      // get the handler for the condition, if there isn't one we presume it
      // is a no-parameter condition
      IHandler handler = this.conditionHandlers.get(this.condition);
      if (handler != null)
      {
         // setup any UI defaults the condition may have and get the location of
         // the JSP used to collect the parameters
         handler.setupUIDefaults(condProps);
         viewId = handler.getJSPPath();
      }
      else
      {
         // just add the action to the list and use the title as the summary
         ActionConditionDefinition conditionDef = this.actionService.
               getActionConditionDefinition(this.condition);
         condProps.put(PROP_CONDITION_SUMMARY, conditionDef.getTitle());
         condProps.put(BaseConditionHandler.PROP_CONDITION_NOT, Boolean.FALSE);
         // add the no params marker so we can disable the edit action
         condProps.put(NO_PARAMS_MARKER, "no-params");
         this.allConditionsProperties.add(condProps);
         
         // come back to the same page we're on now as there are no params to collect
         viewId = this.returnViewId;
      }
      
      if (logger.isDebugEnabled())
            logger.debug("Added '" + this.condition + "' condition to list");
      
      // go to the page to collect the settings
      goToPage(context, viewId);
   }
   
   /**
    * Sets up the context for editing existing condition values 
    */
   @SuppressWarnings("unchecked")
   public void editCondition()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map conditionToEdit = (Map)this.allConditionsDataModel.getRowData();
      this.condition = (String)conditionToEdit.get(PROP_CONDITION_NAME);
      this.currentConditionProperties = conditionToEdit;
      
      // set the flag to show we are editing a condition
      this.editingCondition = true;
      
      // remember the page we're on
      FacesContext context = FacesContext.getCurrentInstance();
      this.returnViewId = context.getViewRoot().getViewId();
      
      // go to the condition page (as there is an edit option visible,
      // there must be a handler for the condition so we don't check)
      goToPage(context, this.conditionHandlers.get(this.condition).getJSPPath());
   }
   
   /**
    * Adds the condition just setup by the user to the list of conditions for the rule
    */
   public void addCondition()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      // this is called from the actions page so there must be a handler
      // present so there's no need to check for null
      String summary = this.conditionHandlers.get(this.condition).generateSummary(
            context, this, this.currentConditionProperties);
      
      if (summary != null)
      {
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, summary);
      }
      
      if (this.editingCondition == false)
      {
         this.allConditionsProperties.add(this.currentConditionProperties);
      }
      
      // reset the action drop down
      this.condition = null;
      
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
      Map conditionToRemove = (Map)this.allConditionsDataModel.getRowData();
      this.allConditionsProperties.remove(conditionToRemove);
      
      // reset the action drop down
      this.condition = null;
      
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
      this.condition = null;
      
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
   
   /**
    * Sets the RulesBean instance to be used by the wizard in edit mode
    * 
    * @param rulesBean The RulesBean
    */
   public void setRulesBean(RulesBean rulesBean)
   {
      this.rulesBean = rulesBean;
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
   protected String setupRule(FacesContext context, Rule rule, String outcome)
   {
      // setup the rule and add it to the space
      rule.setTitle(this.title);
      rule.setDescription(this.description);
      rule.applyToChildren(this.applyToSubSpaces);
      rule.setExecuteAsynchronously(this.runInBackground);
      rule.setRuleDisabled(this.ruleDisabled);
      
      CompositeAction compositeAction = this.actionService.createCompositeAction();
      rule.setAction(compositeAction);
      
      // add all the conditions to the rule
      for (Map<String, Serializable> condParams : this.allConditionsProperties)
      {
         String conditionName = (String)condParams.get(PROP_CONDITION_NAME);
         this.condition = conditionName;
         
         // get the condition handler to prepare for the save
         Map<String, Serializable> repoCondParams = new HashMap<String, Serializable>();
         IHandler handler = this.conditionHandlers.get(this.condition);
         if (handler != null)
         {
            handler.prepareForSave(condParams, repoCondParams);
         }
         
         // add the condition to the rule
         ActionCondition condition = this.actionService.
               createActionCondition(conditionName);
         condition.setParameterValues(repoCondParams);
         
         // specify whether the condition result should be inverted
         Boolean not = (Boolean)condParams.get(BaseConditionHandler.PROP_CONDITION_NOT);
         condition.setInvertCondition(((Boolean)not).booleanValue());
         
         compositeAction.addActionCondition(condition);
      }
      
      // add all the actions to the rule
      for (Map<String, Serializable> actionParams : this.allActionsProperties)
      {
         // use the base class version of buildActionParams(), but for this we need 
         // to setup the currentActionProperties and action variables
         String actionName = (String)actionParams.get(PROP_ACTION_NAME);
         this.action = actionName;
         
         // get the action handler to prepare for the save
         Map<String, Serializable> repoActionParams = new HashMap<String, Serializable>();
         IHandler handler = this.actionHandlers.get(this.action);
         if (handler != null)
         {
            handler.prepareForSave(actionParams, repoActionParams);
         }
         
         // add the action to the rule
         Action action = this.actionService.createAction(actionName);
         action.setParameterValues(repoActionParams);
         compositeAction.addAction(action);
      }
      
      return outcome;
   }
   
   /**
    * Initialises the condition handlers from the current configuration.
    */
   protected void initialiseConditionHandlers()
   {
      if (this.conditionHandlers == null)
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
                  
                  if (conditionName != null && conditionName.length() > 0 &&
                      handlerClass != null && handlerClass.length() > 0)
                  {
                     try
                     {
                        Class klass = Class.forName(handlerClass);
                        IHandler handler = (IHandler)klass.newInstance();
                        this.conditionHandlers.put(conditionName, handler);
                     }
                     catch (Exception e)
                     {
                        throw new AlfrescoRuntimeException("Failed to setup condition handler for '" + 
                              conditionName + "'", e);
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
}
