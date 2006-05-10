package org.alfresco.web.bean.rules;

import java.io.Serializable;
import java.text.MessageFormat;
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
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
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
   public static final String PROP_CONDITION_NAME = "conditionName";
   public static final String PROP_CONDITION_SUMMARY = "conditionSummary";
   public static final String PROP_CONDITION_NOT = "notcondition";
   
   protected RuleService ruleService;
   protected RulesBean rulesBean;
   
   private List<SelectItem> modelTypes;
   private List<SelectItem> mimeTypes;
   private List<SelectItem> types;
   private List<SelectItem> conditions;
   
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
   
   protected static final String CONDITION_PAGES_LOCATION = "/jsp/rules/";
   
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
      this.conditions = null;
      
      this.allConditionsProperties = new ArrayList<Map<String, Serializable>>();
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get hold of the space the rule will apply to and make sure
      // it is actionable
      Node currentSpace = this.browseBean.getActionSpace();
      
      // create the new rule
      Rule rule = this.ruleService.createRule(this.getType());

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
      
      return buildSummary(
            new String[] {bundle.getString("rule_type"), bundle.getString("name"), bundle.getString("description"),
                          bundle.getString("apply_to_sub_spaces"), bundle.getString("run_in_background"),
                          bundle.getString("conditions"), bundle.getString("actions")},
            new String[] {this.type, this.title, this.description, subSpacesYesNo, backgroundYesNo, 
                          conditionsSummary.toString(), actionsSummary.toString()});
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_rule";
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
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
            ConfigElement typesCfg = wizardCfg.getConfigElement("types");
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
      String viewId = calculateConditionViewId(this.condition);
      
      HashMap<String, Serializable> condProps = new HashMap<String, Serializable>(3);
      condProps.put(PROP_CONDITION_NAME, this.condition);
      this.currentConditionProperties = condProps;
      
      // determine whether the condition being added has any parameters
      ActionConditionDefinition conditionDef = this.actionService.
            getActionConditionDefinition(this.condition);
      if (conditionDef.hasParameterDefinitions())
      {
         // setup any defaults for the UI and override the viewId if necessary
         String overridenViewId = setupUIDefaultsForCondition(condProps);
         if (overridenViewId != null)
         {
            viewId = overridenViewId;
         }
      }
      else
      {
         // just add the condition to the list and use the title as the summary
         condProps.put(PROP_CONDITION_SUMMARY, conditionDef.getTitle());
         condProps.put(PROP_CONDITION_NOT, Boolean.FALSE);
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
      
      // refresh the wizard
      goToPage(context, calculateConditionViewId(this.condition));
   }
   
   /**
    * Adds the condition just setup by the user to the list of conditions for the rule
    */
   public void addCondition()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String summary = buildConditionSummary();
      
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
      
      // add all the conditions to the rule
      for (Map<String, Serializable> condParams : this.allConditionsProperties)
      {
         String conditionName = (String)condParams.get(PROP_CONDITION_NAME);
         this.condition = conditionName;
         this.currentConditionProperties = condParams;
         Map<String, Serializable> repoCondParams = buildConditionParams();
         
         // add the condition to the rule
         ActionCondition condition = this.actionService.
               createActionCondition(conditionName);
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
      
      return outcome;
   }
   
   /**
    * Sets up any default state required by the UI for collecting the 
    * condition settings. The view id to use for the condition UI can also
    * be overridden by returing the path to the relevant JSP.
    * 
    * @props The map of properties being used for the current condition
    * @return An optional overridden JSP to use for condition settings collection
    */
   protected String setupUIDefaultsForCondition(HashMap<String, Serializable> props)
   {
      // NOTE: none of the built in conditions have any defaults to setup
      
      return null;
   }
   
   /**
    * Builds the Map of properties for the given condition in the format the repo is expecting
    * 
    * @return The Map the repo is expecting
    */
   protected Map<String, Serializable> buildConditionParams()
   {
      Map<String, Serializable> repoParams = new HashMap<String, Serializable>();
      
      if (ComparePropertyValueEvaluator.NAME.equals(this.condition))
      {
         // add the text to compare
         String text = (String)this.currentConditionProperties.get(PROP_CONTAINS_TEXT);
         repoParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, text);
      }
      else if (InCategoryEvaluator.NAME.equals(this.condition))
      {
         // put the selected category in the condition params
         NodeRef nodeRef = (NodeRef)this.currentConditionProperties.get(PROP_CATEGORY);
         repoParams.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, nodeRef);
         
         // add the classifiable aspect
         repoParams.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, ContentModel.ASPECT_GEN_CLASSIFIABLE);
      }
      else if (IsSubTypeEvaluator.NAME.equals(this.condition))
      {
         // add the model type
         QName type = QName.createQName((String)this.currentConditionProperties.get(PROP_MODEL_TYPE));
         repoParams.put(IsSubTypeEvaluator.PARAM_TYPE, type);
      }
      else if (HasAspectEvaluator.NAME.equals(this.condition))
      {
         // add the aspect
         QName aspect = QName.createQName((String)this.currentConditionProperties.get(PROP_ASPECT));
         repoParams.put(HasAspectEvaluator.PARAM_ASPECT, aspect);
      }
      else if (CompareMimeTypeEvaluator.NAME.equals(this.condition))
      {
         // add the mimetype
         String mimeType = (String)this.currentConditionProperties.get(PROP_MIMETYPE);
         repoParams.put(CompareMimeTypeEvaluator.PARAM_VALUE, mimeType);
      }
      
      return repoParams;
   }
   
   /**
    * Returns a summary string for the current condition
    * 
    * @return The summary or null if a summary could not be built
    */
   protected String buildConditionSummary()
   {
      String summary = null;
      FacesContext context = FacesContext.getCurrentInstance();
      Boolean not = (Boolean)this.currentConditionProperties.get(PROP_CONDITION_NOT);
         
      if (ComparePropertyValueEvaluator.NAME.equals(this.condition))
      {
         String msgId = not.booleanValue() ? 
               "condition_compare_property_value_not" : "condition_compare_property_value";
         
         String text = (String)this.currentConditionProperties.get(PROP_CONTAINS_TEXT);
         
         summary = MessageFormat.format(Application.getMessage(context, msgId),
               new Object[] {text});
      }
      else if (InCategoryEvaluator.NAME.equals(this.condition))
      {
         String msgId = not.booleanValue() ? "condition_in_category_not" : "condition_in_category";
         
         String name = Repository.getNameForNode(this.nodeService, 
               (NodeRef)this.currentConditionProperties.get(PROP_CATEGORY));
         
         summary = MessageFormat.format(Application.getMessage(context, msgId),
               new Object[] {name});
      }
      else if (IsSubTypeEvaluator.NAME.equals(this.condition))
      {
         String msgId = not.booleanValue() ? "condition_is_subtype_not" : "condition_is_subtype";
         
         String label = null;
         String typeName = (String)this.currentConditionProperties.get(PROP_MODEL_TYPE);
         for (SelectItem item : this.getModelTypes())
         {
            if (item.getValue().equals(typeName))
            {
               label = item.getLabel();
               break;
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, msgId),
               new Object[] {label});
      }
      else if (HasAspectEvaluator.NAME.equals(this.condition))
      {
         String msgId = not.booleanValue() ? "condition_has_aspect_not" : "condition_has_aspect";
         
         String label = null;
         String aspectName = (String)this.currentConditionProperties.get(PROP_ASPECT);
         for (SelectItem item : this.getAspects())
         {
            if (item.getValue().equals(aspectName))
            {
               label = item.getLabel();
               break;
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, msgId),
               new Object[] {label});
      }
      else if (CompareMimeTypeEvaluator.NAME.equals(this.condition))
      {
         String msgId = not.booleanValue() ? "condition_compare_mime_type_not" : "condition_compare_mime_type";
         
         String label = null;
         String mimetype = (String)this.currentConditionProperties.get(PROP_MIMETYPE);
         for (SelectItem item : this.getMimeTypes())
         {
            if (item.getValue().equals(mimetype))
            {
               label = item.getLabel();
               break;
            }
         }
         
         summary = MessageFormat.format(Application.getMessage(context, msgId),
               new Object[] {label});
      }
      else
      {
         // as the default case (i.e. for conditions with no parameters) use the title
         ActionConditionDefinition conditionDef = this.actionService.
               getActionConditionDefinition(this.condition);
         summary = conditionDef.getTitle();
      }
      
      return summary;
   }
   
   /**
    * Calculates the viewId for the given condition id
    * 
    * @param conditionId The id of the condition to generate the view id for
    * @return The view id
    */
   protected String calculateConditionViewId(String conditionId)
   {
      return CONDITION_PAGES_LOCATION + conditionId + ".jsp";
   }
}
