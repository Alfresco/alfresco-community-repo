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
package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.RulesBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Handler class used by the New Space Wizard 
 * 
 * @author gavinc
 */
public class NewRuleWizard extends BaseActionWizard
{
   // parameter names for actions
   public static final String PROP_ACTION_NAME = "actionName";
   public static final String PROP_ACTION_SUMMARY = "actionSummary";
   
   // parameter names for conditions
   public static final String PROP_CONDITION_NAME = "conditionName";
   public static final String PROP_CONDITION_SUMMARY = "conditionSummary";
   public static final String PROP_CONDITION_NOT = "notcondition";
   public static final String PROP_PROPERTY = "property";
   public static final String PROP_CONTAINS_TEXT = "containstext";
   public static final String PROP_MODEL_TYPE = "modeltype";
   public static final String PROP_MIMETYPE = "mimetype";
   public static final String PROP_MODEL_ASPECT = "modelaspect";
   public static final String PROP_TYPE_OR_ASPECT = "typeoraspect";
   
   private static Log logger = LogFactory.getLog(NewRuleWizard.class);
   
   private static final String ERROR = "error_rule";
   
   // TODO: retrieve these from the config service
   private static final String WIZARD_TITLE_ID = "new_rule_title";
   private static final String WIZARD_TITLE_EDIT_ID = "new_rule_title_edit";
   private static final String WIZARD_DESC_ID = "new_rule_desc";
   private static final String WIZARD_DESC_EDIT_ID = "new_rule_desc_edit";
   private static final String STEP1_TITLE_ID = "new_rule_step1_title";
   private static final String STEP2_TITLE_ID = "new_rule_step2_title";
   private static final String STEP3_TITLE_ID = "new_rule_step3_title";
   private static final String FINISH_INSTRUCTION_ID = "new_rule_finish_instruction";
   private static final String FINISH_INSTRUCTION_EDIT_ID = "new_rule_finish_instruction_edit";
   
   // new rule wizard specific properties
   private String title;
   private String description;
   private String type;
   private String condition;
   private boolean runInBackground;
   private boolean applyToSubSpaces;
   private boolean editingAction;
   private boolean editingCondition;

   private RuleService ruleService;
   private RulesBean rulesBean;
   
   private List<SelectItem> modelTypes;
   private List<SelectItem> mimeTypes;
   private List<SelectItem> types;
   private List<SelectItem> conditions;
   private List<SelectItem> typesAndAspects;
   private Map<String, String> conditionDescriptions;
   private Map<String, Serializable> currentConditionProperties;
   
   private List<Map<String, Serializable>> allActionsProperties;
   private List<Map<String, Serializable>> allConditionsProperties;
   
   private DataModel allActionsDataModel;
   private DataModel allConditionsDataModel;
   
   /**
    * Deals with the finish button being pressed
    * 
    * @return outcome
    */
   public String finish()
   {
      String outcome = FINISH_OUTCOME;
      
      UserTransaction tx = null;
   
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance());
         tx.begin();
         
         // get hold of the space the rule will apply to and make sure
         // it is actionable
         Node currentSpace = browseBean.getActionSpace();
         
         Rule rule = null;
         
         if (this.editMode)
         {
            // update the existing rule in the repository
            rule = this.rulesBean.getCurrentRule();
                        
            // remove all the conditions and actions from the current rule
            rule.removeAllActionConditions();
            rule.removeAllActions();
         }
         else
         {
            rule = this.ruleService.createRule(this.getType());
         }

         // setup the rule and add it to the space
         rule.setTitle(this.title);
         rule.setDescription(this.description);
         rule.applyToChildren(this.applyToSubSpaces);
         rule.setExecuteAsynchronously(this.runInBackground);
         
         // add all the conditions to the rule
         for (Map<String, Serializable> condParams : this.allConditionsProperties)
         {
            Map<String, Serializable> repoCondParams = buildConditionParams(condParams);
            
            // add the condition to the rule
            ActionCondition condition = this.actionService.createActionCondition(
                  (String)condParams.get(PROP_CONDITION_NAME));
            condition.setParameterValues(repoCondParams);
            
            // specify whether the condition result should be inverted
            Boolean not = (Boolean)condParams.get(PROP_CONDITION_NOT);
            condition.setInvertCondition(((Boolean)not).booleanValue());
            
            rule.addActionCondition(condition);
         }
         
         // add all the actions to the rule
         for (Map<String, Serializable> actionParams : this.allActionsProperties)
         {
            // use the base class version of buildActionParams(), but for this we need 
            // to setup the currentActionProperties and action variables
            String actionName = (String)actionParams.get(PROP_ACTION_NAME);
            this.action = actionName;
            this.currentActionProperties = actionParams;
            Map<String, Serializable> repoActionParams = buildActionParams();
            
            // add the action to the rule
            Action action = this.actionService.createAction(actionName);
            action.setParameterValues(repoActionParams);
            rule.addAction(action);
         }
         
         // Save the rule
         this.ruleService.saveRule(currentSpace.getNodeRef(), rule);
         
         if (logger.isDebugEnabled())
         {
            logger.debug(this.editMode ? "Updated" : "Added" + " rule '" + this.title + "'");
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Exception e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
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
    * Displays the settings page for the current condition being added (if required)
    * 
    * @return The outcome
    */
   public String promptForConditionValues()
   {
      String outcome = null;

      // set the flag to show we are creating a new condition
      this.editingCondition = false;
      
      if ("no-condition".equals(this.condition))
      {
         HashMap<String, Serializable> condProps = new HashMap<String, Serializable>(3);
         condProps.put(PROP_CONDITION_NAME, this.condition);
         condProps.put(PROP_CONDITION_SUMMARY, Application.getMessage(
               FacesContext.getCurrentInstance(), "condition_no_condition"));
         condProps.put(PROP_CONDITION_NOT, Boolean.FALSE);
         this.allConditionsProperties.add(condProps);
         
         // NOTE: we don't set an outcome to stay on the same page as there are 
         //       no settings related to 'no-condition'
         
         if (logger.isDebugEnabled())
            logger.debug("Add 'no-condition' condition to list");
      }
      else if (this.condition != null)
      {
         HashMap<String, Serializable> condProps = new HashMap<String, Serializable>(3);
         condProps.put(PROP_CONDITION_NAME, this.condition);
         this.currentConditionProperties = condProps;
         outcome = this.condition;
         
         if (logger.isDebugEnabled())
            logger.debug("Added '" + this.condition + "' condition to list");
      }
      
      // reset the selected condition drop down
      this.condition = null;
      
      return outcome;
   }
   
   /**
    * Sets up the context for editing existing condition values 
    * 
    * @return The outcome
    */
   public String editCondition()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map conditionToEdit = (Map)this.allConditionsDataModel.getRowData();
      this.condition = (String)conditionToEdit.get(PROP_CONDITION_NAME);
      this.currentConditionProperties = conditionToEdit;
      
      // set the flag to show we are editing a condition
      this.editingCondition = true;
      
      return this.condition;
   }
   
   /**
    * Adds the condition just setup by the user to the list of conditions for the rule
    * 
    * @return The outcome
    */
   public String addCondition()
   {
      String summary = buildConditionSummary(this.currentConditionProperties);
      
      if (summary != null)
      {
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, summary);
      }
      
      if (this.editingCondition)
      {
         this.condition = null;
      }
      else
      {
         this.allConditionsProperties.add(this.currentConditionProperties);
      }
      
      // re-display the conditions step
      return "condition";
   }
   
   /**
    * Removes the requested condition from the list
    * 
    * @return The outcome
    */
   public String removeCondition()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map conditionToRemove = (Map)this.allConditionsDataModel.getRowData();
      this.allConditionsProperties.remove(conditionToRemove);
      
      // reset the action drop down
      this.condition = null;
      
      // return no outcome to refresh page
      return null;
   }
   
   /**
    * Cancels the addition of the condition
    * 
    * @return The outcome
    */
   public String cancelAddCondition()
   {
      if (this.editingCondition)
      {
         this.condition = null;
      }
      else
      {
         this.currentConditionProperties.clear();
      }
      
      
      return "condition";
   }

   /**
    * Returns the properties for all the actions as a JSF DataModel
    * 
    * @return JSF DataModel representing the action properties
    */
   public DataModel getAllActionsDataModel()
   {
      if (this.allActionsDataModel == null)
      {
         this.allActionsDataModel = new ListDataModel();
      }
      
      this.allActionsDataModel.setWrappedData(this.allActionsProperties);
      
      return this.allActionsDataModel;
   }
   
   /**
    * Displays the settings page for the current action being added
    * 
    * @return The outcome
    */
   public String promptForActionValues()
   {
      // set the flag to show we are creating a new action
      this.editingAction = false;
      
      HashMap<String, Serializable> actionProps = new HashMap<String, Serializable>(3);
      actionProps.put(PROP_ACTION_NAME, this.action);
      this.currentActionProperties = actionProps;
      
      String outcome = this.action;
      
      if (SimpleWorkflowActionExecuter.NAME.equals(this.action))
      {
         this.currentActionProperties.put("approveAction", "move");
         this.currentActionProperties.put("rejectStepPresent", "yes");
         this.currentActionProperties.put("rejectAction", "move");
         
         if (logger.isDebugEnabled())
            logger.debug("Added '" + SimpleWorkflowActionExecuter.NAME + 
                  "' action to list");
      }
      else if (CheckInActionExecuter.NAME.equals(this.action))
      {
         this.currentActionProperties.put(PROP_CHECKIN_MINOR, new Boolean(true));
         
         if (logger.isDebugEnabled())
            logger.debug("Added '" + CheckInActionExecuter.NAME + 
                  "' action to list");
      }
      else if ("extract-metadata".equals(this.action))
      {
         // This one (currently) has no parameters, so just add it...
         actionProps.put(PROP_ACTION_SUMMARY, buildActionSummary(actionProps));
         this.allActionsProperties.add(actionProps);
         
         outcome = null;
         
         if (logger.isDebugEnabled())
            logger.debug("Added 'extract-metadata' action to list");
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Added '" + this.action + "' action to list");
      }
      
      // reset the selected action drop down
      this.action = null;
      
      return outcome;
   }
   
   /**
    * Sets up the context for editing existing action values 
    * 
    * @return The outcome
    */
   public String editAction()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map actionToEdit = (Map)this.allActionsDataModel.getRowData();
      this.action = (String)actionToEdit.get(PROP_ACTION_NAME);
      this.currentActionProperties = actionToEdit;
      
      // set the flag to show we are editing an action
      this.editingAction = true;
      
      return this.action;
   }
   
   /**
    * Adds the action just setup by the user to the list of actions for the rule
    * 
    * @return The outcome
    */
   public String addAction()
   {
      String summary = buildActionSummary(this.currentActionProperties);
      
      if (summary != null)
      {
         this.currentActionProperties.put(PROP_ACTION_SUMMARY, summary);
      }
      
      if (this.editingAction)
      {
         this.action = null;
      }
      else
      {
         this.allActionsProperties.add(this.currentActionProperties);
      }
      
      // re-display the actions step
      return "action";
   }
   
   /**
    * Removes the requested action from the list
    * 
    * @return The outcome
    */
   public String removeAction()
   {
      // use the built in JSF support for retrieving the object for the
      // row that was clicked by the user
      Map actionToRemove = (Map)this.allActionsDataModel.getRowData();
      this.allActionsProperties.remove(actionToRemove);
      
      // reset the action drop down
      this.action = null;
      
      // return no outcome to refresh page
      return null;
   }
   
   /**
    * Cancels the addition of the action
    * 
    * @return The outcome
    */
   public String cancelAddAction()
   {
      if (this.editingAction)
      {
         this.condition = null;
      }
      else
      {
         this.currentActionProperties.clear();
      }
      
      return "action";
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardDescription()
    */
   public String getWizardDescription()
   {
      if (this.editMode)
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_DESC_EDIT_ID);
      }
      else
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_DESC_ID);
      }
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardTitle()
    */
   public String getWizardTitle()
   {
      if (this.editMode)
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_TITLE_EDIT_ID);
      }
      else
      {
         return Application.getMessage(FacesContext.getCurrentInstance(), WIZARD_TITLE_ID);
      }
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepDescription()
    */
   public String getStepDescription()
   {
      String stepDesc = null;
      
      switch (this.currentStep)
      {
         case 4:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), SUMMARY_DESCRIPTION_ID);
            break;
         }
         default:
         {
            stepDesc = "";
         }
      }
      
      return stepDesc;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepTitle()
    */
   public String getStepTitle()
   {
      String stepTitle = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP1_TITLE_ID);
            break;
         }
         case 2:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_TITLE_ID);
            break;
         }
         case 3:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP3_TITLE_ID);
            break;
         }
         case 4:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), SUMMARY_TITLE_ID);
            break;
         }
         default:
         {
            stepTitle = "";
         }
      }
      
      return stepTitle;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 4:
         {
            if (this.editMode)
            {
               stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), FINISH_INSTRUCTION_EDIT_ID);
            }
            else
            {
               stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), FINISH_INSTRUCTION_ID);
            }
            break;
         }
         default:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), DEFAULT_INSTRUCTION_ID);
         }
      }
      
      return stepInstruction;
   }
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
      
      this.title = null;
      this.description = null;
      this.type = "inbound";
      this.condition = null;
      this.action = null;
      this.applyToSubSpaces = false;
      this.runInBackground = false;
      this.conditions = null;
      this.conditionDescriptions = null;
      
      this.allConditionsProperties = new ArrayList<Map<String, Serializable>>();
      this.allActionsProperties = new ArrayList<Map<String, Serializable>>();
   }
   
   /**
    * Sets the context of the rule up before performing the 
    * standard wizard editing steps
    *  
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#startWizardForEdit(javax.faces.event.ActionEvent)
    */
   public void startWizardForEdit(ActionEvent event)
   {
      // setup context for rule to be edited
      this.rulesBean.setupRuleAction(event);
      
      // perform the usual edit processing
      super.startWizardForEdit(event);
   }

   /**
    * Populates the values of the backing bean ready for editing the rule
    * 
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
    */
   public void populate()
   {
      // get hold of the current rule details
      Rule rule = this.rulesBean.getCurrentRule();
      
      if (rule == null)
      {
         throw new AlfrescoRuntimeException("Failed to locate the current rule");
      }
      
      // populate the bean with current values 
      this.type = rule.getRuleTypeName();
      this.title = rule.getTitle();
      this.description = rule.getDescription();
      this.applyToSubSpaces = rule.isAppliedToChildren();
      this.runInBackground = rule.getExecuteAsychronously();
      
      // populate the conditions list with maps of properties representing each condition
      List<ActionCondition> conditions = rule.getActionConditions();
      for (ActionCondition condition : conditions)
      {
         Map<String, Serializable> params = populateCondition(condition);
         this.allConditionsProperties.add(params);
      }
      
      List<Action> actions = rule.getActions();
      for (Action action : actions)
      {
         // use the base class version of populateActionFromProperties(), 
         // but for this we need to setup the currentActionProperties and 
         // action variables
         this.currentActionProperties = new HashMap<String, Serializable>(3);
         this.action = action.getActionDefinitionName();
         populateActionFromProperties(action.getParameterValues());
         
         // also add the name and summary 
         this.currentActionProperties.put(PROP_ACTION_NAME, this.action);
         // generate the summary
         this.currentActionProperties.put(PROP_ACTION_SUMMARY, 
               buildActionSummary(this.currentActionProperties));
         
         // add the populated currentActionProperties to the list
         this.allActionsProperties.add(this.currentActionProperties);
      }
      
      // reset the current action
      this.action = null;
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
      
      return buildSummary(
            new String[] {bundle.getString("rule_type"), bundle.getString("name"), bundle.getString("description"),
                          bundle.getString("apply_to_sub_spaces"), bundle.getString("run_in_background"),
                          bundle.getString("conditions"), bundle.getString("actions")},
            new String[] {this.type, this.title, this.description, subSpacesYesNo, backgroundYesNo, 
                          conditionsSummary.toString(), actionsSummary.toString()});
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#back()
    */
   public String back()
   {
      // reset the drop downs when back is clicked
      this.action = null;
      this.condition = null;
      
      return super.back();
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#next()
    */
   public String next()
   {
      // reset the drop downs when next is clicked
      this.action = null;
      this.condition = null;
      
      return super.next();
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

   /**
    * @param ruleService Sets the rule service to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   /**
    * @param mimetypeService Sets the mimetype service to use
    */
   public void setMimetypeService(MimetypeService mimetypeService)
   {
      this.mimetypeService = mimetypeService;
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

   /**
    * @return Returns the list of selectable actions
    */
   public List<SelectItem> getActions()
   {
      if (this.actions == null)
      {
         super.getActions();
         
         // add the "Select an action" entry at the beginning of the list
         this.actions.add(0, new SelectItem("null", 
               Application.getMessage(FacesContext.getCurrentInstance(), "select_an_action")));
      }
      
      return this.actions;
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
         ConfigService svc = (ConfigService)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(Application.BEAN_CONFIG_SERVICE);
         Config wizardCfg = svc.getConfig("Action Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("types");
            if (typesCfg != null)
            {
               FacesContext context = FacesContext.getCurrentInstance();
               this.modelTypes = new ArrayList<SelectItem>();
               for (ConfigElement child : typesCfg.getChildren())
               {
                  QName idQName = Repository.resolveToQName(child.getAttribute("name"));

                  // look for a client localized string
                  String label = null;
                  String msgId = child.getAttribute("displayLabelId");
                  if (msgId != null)
                  {
                     label = Application.getMessage(context, msgId);
                  }
                  
                  // if there wasn't an externalized string look for one in the config
                  if (label == null)
                  {
                     label = child.getAttribute("displayLabel");
                  }

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
               logger.warn("Could not find types configuration element");
            }
         }
         else
         {
            logger.warn("Could not find Action Wizards configuration section");
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
    * @return Returns a map of all the condition descriptions 
    */
   public Map<String, String> getConditionDescriptions()
   {
      if (this.conditionDescriptions == null)
      {
         List<ActionConditionDefinition> ruleConditions = this.actionService.getActionConditionDefinitions();
         this.conditionDescriptions = new HashMap<String, String>(ruleConditions.size());
         for (ActionConditionDefinition ruleConditionDef : ruleConditions)
         {
            this.conditionDescriptions.put(ruleConditionDef.getName(), 
                  ruleConditionDef.getDescription());
         }
      }
      
      return this.conditionDescriptions;
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
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#determineOutcomeForStep(int)
    */
   protected String determineOutcomeForStep(int step)
   {
      String outcome = null;
      
      switch(step)
      {
         case 1:
         {
            outcome = "details";
            break;
         }
         case 2:
         {
            outcome = "condition";
            break;
         }
         case 3:
         {
            outcome = "action";
            break;
         }
         case 4:
         {
            outcome = "summary";
            break;
         }
         default:
         {
            outcome = CANCEL_OUTCOME;
         }
      }
      
      return outcome;
   }
   
   /**
    * Builds the Map of properties for the given condition in the format the repo is expecting
    * 
    * @param params The Map of properties built from the UI
    * @return The Map the repo is expecting
    */
   protected Map<String, Serializable> buildConditionParams(Map<String, Serializable> params)
   {
      Map<String, Serializable> repoParams = new HashMap<String, Serializable>(params.size());
      
      String condName = (String)params.get(PROP_CONDITION_NAME);
      if (condName.equals(ComparePropertyValueEvaluator.NAME))
      {
         repoParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, params.get(PROP_CONTAINS_TEXT));
      }
      else if (condName.equals(InCategoryEvaluator.NAME))
      {
         // put the selected category in the condition params
         repoParams.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, params.get(PROP_CATEGORY));
         
         // add the classifiable aspect
         repoParams.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, ContentModel.ASPECT_GEN_CLASSIFIABLE);
      }
      else if (condName.equals(IsSubTypeEvaluator.NAME))
      {
         // add the model type
         repoParams.put(IsSubTypeEvaluator.PARAM_TYPE, QName.createQName((String)params.get(PROP_MODEL_TYPE)));
      }
      else if (condName.equals(HasAspectEvaluator.NAME))
      {
         // add the aspect
         repoParams.put(HasAspectEvaluator.PARAM_ASPECT, QName.createQName((String)params.get(PROP_ASPECT)));
      }
      else if (condName.equals(CompareMimeTypeEvaluator.NAME))
      {
          repoParams.put(CompareMimeTypeEvaluator.PARAM_VALUE, params.get(PROP_MIMETYPE));
      }
      
      return repoParams;
   }
   
   /**
    * Populates a Map of properties the wizard is expecting for the given condition
    * 
    * @param condition The condition to build the map for
    */
   protected Map<String, Serializable> populateCondition(ActionCondition condition)
   {
      // find out what the condition is called
      Map<String, Serializable> condProps = new HashMap<String, Serializable>(3);
      String name = condition.getActionConditionDefinitionName();
      condProps.put(PROP_CONDITION_NAME, name);
      
      // add the appropriate properties
      Map<String, Serializable> repoCondProps = condition.getParameterValues();
      if (name.equals(ComparePropertyValueEvaluator.NAME))
      {
         condProps.put(PROP_CONTAINS_TEXT, (String)repoCondProps.get(ComparePropertyValueEvaluator.PARAM_VALUE));
      }
      else if (name.equals(InCategoryEvaluator.NAME))
      {
         NodeRef catNodeRef = (NodeRef)repoCondProps.get(InCategoryEvaluator.PARAM_CATEGORY_VALUE);
         condProps.put(PROP_CATEGORY, catNodeRef);
      }
      else if (name.equals(IsSubTypeEvaluator.NAME))
      {
         condProps.put(PROP_MODEL_TYPE, ((QName)repoCondProps.get(IsSubTypeEvaluator.PARAM_TYPE)).toString());
      }
      else if (name.equals(HasAspectEvaluator.NAME))
      {
         condProps.put(PROP_ASPECT, ((QName)repoCondProps.get(HasAspectEvaluator.PARAM_ASPECT)).toString());
      }
      else if (name.equals(CompareMimeTypeEvaluator.NAME))
      {
          condProps.put(PROP_MIMETYPE, repoCondProps.get(CompareMimeTypeEvaluator.PARAM_VALUE));
      }
      
      // specify whether the condition result should be inverted
      condProps.put(PROP_CONDITION_NOT, Boolean.valueOf(condition.getInvertCondition()));
      
      // generate the summary 
      condProps.put(PROP_CONDITION_SUMMARY, buildConditionSummary(condProps));
         
      return condProps;
   }
   
   /**
    * Returns a summary string for the given condition parameters
    * 
    * @return The summary or null if a summary could not be built
    */
   protected String buildConditionSummary(Map<String, Serializable> props)
   {
      String summaryResult = null;
      
      String condName = (String)props.get(PROP_CONDITION_NAME);
      if (condName != null)
      {
         StringBuilder summary = new StringBuilder();
         
         String msgId = "condition_" + condName.replace('-', '_');
         
         // JSF is putting the boolean into the map as a Boolean object so we
         // need to handle that - adding a converter doesn't seem to help!
         Boolean not = (Boolean)props.get(PROP_CONDITION_NOT);
         if (not.booleanValue())
         {
            msgId = msgId + "_not";
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Looking up condition summary string: " + msgId);
         
         summary.append(Application.getMessage(FacesContext.getCurrentInstance(), msgId));
         summary.append(" ");
         
         // define a summary to be added for each condition
         if ("in-category".equals(condName))
         {
            String name = Repository.getNameForNode(this.nodeService, (NodeRef)props.get(PROP_CATEGORY));
            summary.append("'").append(name).append("'");
         }
         else if ("compare-property-value".equals(condName))
         {
            summary.append("'");
            summary.append(props.get(PROP_CONTAINS_TEXT));
            summary.append("'");
         }
         else if ("is-subtype".equals(condName))
         {
            // find the label used by looking through the SelectItem list
            String typeName = (String)props.get(PROP_MODEL_TYPE);
            for (SelectItem item : this.getModelTypes())
            {
               if (item.getValue().equals(typeName))
               {
                  summary.append("'").append(item.getLabel()).append("'");
                  break;
               }
            }
         }
         else if ("has-aspect".equals(condName))
         {
            // find the label used by looking through the SelectItem list
            String aspectName = (String)props.get(PROP_ASPECT);
            for (SelectItem item : this.getAspects())
            {
               if (item.getValue().equals(aspectName))
               {
                  summary.append("'").append(item.getLabel()).append("'");
                  break;
               }
            }
         }
         else if (CompareMimeTypeEvaluator.NAME.equals(condName))
         {
            String mimetype = (String)props.get(PROP_MIMETYPE);
            for (SelectItem item : this.getMimeTypes())
            {
               if (item.getValue().equals(mimetype))
               {
                  summary.append("'").append(item.getLabel()).append("'");
                  break;
               }
            }
         }
         
         summaryResult = summary.toString();
      }
      
      return summaryResult;
   }
   
   /**
    * Returns a summary string for the given action parameters
    * 
    * @return The summary or null if a summary could not be built
    */
   protected String buildActionSummary(Map<String, Serializable> props)
   {
      String summaryResult = null;
      
      String actionName = (String)this.currentActionProperties.get(PROP_ACTION_NAME);
      if (actionName != null)
      {
         StringBuilder summary = new StringBuilder();
         summary.append(Application.getMessage(FacesContext.getCurrentInstance(), 
               "action_" + actionName.replace('-', '_')));
         summary.append(" ");
         
         // define a summary to be added for each action
         if ("add-features".equals(actionName))
         {
            String aspect = (String)this.currentActionProperties.get(PROP_ASPECT);
            
            // find the label used by looking through the SelectItem list
            for (SelectItem item : this.getAspects())
            {
               if (item.getValue().equals(aspect))
               {
                  summary.append("'").append(item.getLabel()).append("'");
                  break;
               }
            }
         }
         else if ("simple-workflow".equals(actionName))
         {
            // just leave the summary as the title for now
            String approveStepName = (String)this.currentActionProperties.get(PROP_APPROVE_STEP_NAME);
            String approveAction = (String)this.currentActionProperties.get(PROP_APPROVE_ACTION);
            NodeRef approveFolder = (NodeRef)this.currentActionProperties.get(PROP_APPROVE_FOLDER);
            String approveFolderName = Repository.getNameForNode(this.nodeService, approveFolder);
            String approveMsg = MessageFormat.format(summary.toString(), 
                  new Object[] {Application.getMessage(FacesContext.getCurrentInstance(), approveAction), 
                                approveFolderName, approveStepName});
            
            String rejectStep = (String)this.currentActionProperties.get(PROP_REJECT_STEP_PRESENT);
            
            String rejectMsg = null;
            if (rejectStep != null && "yes".equals(rejectStep))
            {
               String rejectStepName = (String)this.currentActionProperties.get(PROP_REJECT_STEP_NAME);
               String rejectAction = (String)this.currentActionProperties.get(PROP_REJECT_ACTION);
               NodeRef rejectFolder = (NodeRef)this.currentActionProperties.get(PROP_REJECT_FOLDER);
               String rejectFolderName = Repository.getNameForNode(this.nodeService, rejectFolder);
               rejectMsg = MessageFormat.format(summary.toString(), 
                  new Object[] {Application.getMessage(FacesContext.getCurrentInstance(), rejectAction),
                                rejectFolderName, rejectStepName});
            }
            
            summary = new StringBuilder(approveMsg);
            if (rejectMsg != null)
            {
               summary.append(" ");
               summary.append(rejectMsg);
            }
         }
         else if ("link-category".equals(actionName))
         {
            NodeRef cat = (NodeRef)this.currentActionProperties.get(PROP_CATEGORY);
            String name = Repository.getNameForNode(this.nodeService, cat);
            summary.append("'").append(name).append("'");
         }
         else if ("transform".equals(actionName))
         {
            NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
            String name = Repository.getNameForNode(this.nodeService, space);
            String transformer = (String)this.currentActionProperties.get(PROP_TRANSFORMER);
            
            // find the label used by looking through the SelectItem list
            for (SelectItem item : this.getTransformers())
            {
               if (item.getValue().equals(transformer))
               {
                  transformer = item.getLabel();
                  break;
               }
            }
            
            // recreate the summary object as it contains parameters
            String msg = MessageFormat.format(summary.toString(), new Object[] {name, transformer});
            summary = new StringBuilder(msg);
         }
         else if ("transform-image".equals(actionName))
         {
            NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
            String name = Repository.getNameForNode(this.nodeService, space);
            String transformer = (String)this.currentActionProperties.get(PROP_IMAGE_TRANSFORMER);
            String option = (String)this.currentActionProperties.get(PROP_TRANSFORM_OPTIONS);
            
            // find the label used by looking through the SelectItem list
            for (SelectItem item : this.getImageTransformers())
            {
               if (item.getValue().equals(transformer))
               {
                  transformer = item.getLabel();
                  break;
               }
            }
            
            // recreate the summary object as it contains parameters
            String msg = MessageFormat.format(summary.toString(), new Object[] {name, transformer, option});
            summary = new StringBuilder(msg);
         }
         else if ("copy".equals(actionName) || "move".equals(actionName) || "check-out".equals(actionName))
         {
            NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
            String spaceName = Repository.getNameForNode(this.nodeService, space);
            summary.append("'").append(spaceName).append("'");
         }
         else if ("mail".equals(actionName))
         {
            String address = (String)this.currentActionProperties.get(PROP_TO);
            summary.append("'").append(address).append("'");
         }
         else if ("check-in".equals(actionName))
         {
            String comment = (String)this.currentActionProperties.get(PROP_CHECKIN_DESC);
            Boolean minorChange = (Boolean)this.currentActionProperties.get(PROP_CHECKIN_MINOR);
            String change = null;
            if (minorChange != null && minorChange.booleanValue())
            {
               change = Application.getMessage(FacesContext.getCurrentInstance(), "minor_change");
            }
            else
            {
               change = Application.getMessage(FacesContext.getCurrentInstance(), "major_change");
            }
            
            // recreate the summary object as it contains parameters
            String msg = MessageFormat.format(summary.toString(), new Object[] {change, comment});
            summary = new StringBuilder(msg);
         }
         else if ("import".equals(actionName))
         {
            NodeRef space = (NodeRef)this.currentActionProperties.get(PROP_DESTINATION);
            String spaceName = Repository.getNameForNode(this.nodeService, space);
            summary.append("'").append(spaceName).append("'");
         }
         else if (SpecialiseTypeActionExecuter.NAME.equals(actionName) == true)
         {
             String label = null;
             String objectType = (String)this.currentActionProperties.get(PROP_OBJECT_TYPE);
             for (SelectItem item  : getObjectTypes())
             {
                if (item.getValue().equals(objectType) == true)
                {
                    label = item.getLabel();
                    break;
                }
             }
             
             summary.append("'").append(label).append("'");
         }

         summaryResult = summary.toString();
      }
      
      return summaryResult;
   }
}
