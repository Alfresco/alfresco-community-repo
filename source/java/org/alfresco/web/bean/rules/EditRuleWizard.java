package org.alfresco.web.bean.rules;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecutor;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.SpecialiseTypeActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
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
      this.type = rule.getRuleTypeName();
      this.title = rule.getTitle();
      this.description = rule.getDescription();
      this.applyToSubSpaces = rule.isAppliedToChildren();
      this.runInBackground = rule.getExecuteAsychronously();
      
      // populate the conditions list with maps of properties representing each condition
      List<ActionCondition> conditions = rule.getActionConditions();
      for (ActionCondition condition : conditions)
      {
         this.currentConditionProperties = new HashMap<String, Serializable>(3);
         this.condition = condition.getActionConditionDefinitionName();
         populateCondition(condition.getParameterValues());
         
         // add the name, summary and not condition flag
         this.currentConditionProperties.put(PROP_CONDITION_NAME, this.condition);
         this.currentConditionProperties.put(PROP_CONDITION_NOT, 
               Boolean.valueOf(condition.getInvertCondition()));
         this.currentConditionProperties.put(PROP_CONDITION_SUMMARY, buildConditionSummary());
      
         // add the populated currentConditionProperties to the list
         this.allConditionsProperties.add(this.currentConditionProperties);
      }
      
      // populate the actions list with maps of properties representing each action
      List<Action> actions = rule.getActions();
      for (Action action : actions)
      {
         this.currentActionProperties = new HashMap<String, Serializable>(3);
         this.action = action.getActionDefinitionName();
         populateAction(action.getParameterValues());
         
         // also add the name and summary 
         this.currentActionProperties.put(PROP_ACTION_NAME, this.action);
         this.currentActionProperties.put(PROP_ACTION_SUMMARY, buildActionSummary());
         
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
                  
      // remove all the conditions and actions from the current rule
      rule.removeAllActionConditions();
      rule.removeAllActions();
      
      // re-setup the rule
      outcome = setupRule(context, rule, outcome);
      
      // Save the rule
      this.ruleService.saveRule(currentSpace.getNodeRef(), rule);
      
      if (logger.isDebugEnabled())
         logger.debug("Updated rule '" + this.title + "'");
      
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Populates a Map of properties the wizard is expecting for the given condition
    * 
    * @param condition The condition to build the map for
    */
   protected void populateCondition(Map<String, Serializable> conditionProps)
   {
      if (ComparePropertyValueEvaluator.NAME.equals(this.condition))
      {
         String propValue = (String)conditionProps.get(ComparePropertyValueEvaluator.PARAM_VALUE);
         this.currentConditionProperties.put(PROP_CONTAINS_TEXT, propValue);
      }
      else if (InCategoryEvaluator.NAME.equals(this.condition))
      {
         NodeRef catNodeRef = (NodeRef)conditionProps.get(InCategoryEvaluator.PARAM_CATEGORY_VALUE);
         this.currentConditionProperties.put(PROP_CATEGORY, catNodeRef);
      }
      else if (IsSubTypeEvaluator.NAME.equals(this.condition))
      {
         QName type = (QName)conditionProps.get(IsSubTypeEvaluator.PARAM_TYPE);
         this.currentConditionProperties.put(PROP_MODEL_TYPE, type.toString());
      }
      else if (HasAspectEvaluator.NAME.equals(this.condition))
      {
         QName aspect = (QName)conditionProps.get(HasAspectEvaluator.PARAM_ASPECT);
         this.currentConditionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (CompareMimeTypeEvaluator.NAME.equals(this.condition))
      {
         String mimeType = (String)conditionProps.get(CompareMimeTypeEvaluator.PARAM_VALUE);
         this.currentConditionProperties.put(PROP_MIMETYPE, mimeType);
      }
   }
   
   /**
    * Populate the actionProperties member variable with correct props for the current action
    * using the supplied property map.
    * 
    * @param actionProps Map to retrieve props appropriate to the current action from
    */
   protected void populateAction(Map<String, Serializable> actionProps)
   {
      if (AddFeaturesActionExecuter.NAME.equals(this.action))
      {
         QName aspect = (QName)actionProps.get(AddFeaturesActionExecuter.PARAM_ASPECT_NAME);
         this.currentActionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (RemoveFeaturesActionExecuter.NAME.equals(this.action))
      {
          QName aspect = (QName)actionProps.get(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME);
          this.currentActionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (CopyActionExecuter.NAME.equals(this.action))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (MoveActionExecuter.NAME.equals(this.action))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (SimpleWorkflowActionExecuter.NAME.equals(this.action))
      {
         String approveStep = (String)actionProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP);
         Boolean approveMove = (Boolean)actionProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE);
         NodeRef approveFolderNode = (NodeRef)actionProps.get(
               SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER);
         
         String rejectStep = (String)actionProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP);
         Boolean rejectMove = (Boolean)actionProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE);
         NodeRef rejectFolderNode = (NodeRef)actionProps.get(
               SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER);
         
         this.currentActionProperties.put(PROP_APPROVE_STEP_NAME, approveStep);
         this.currentActionProperties.put(PROP_APPROVE_ACTION, approveMove ? "move" : "copy");
         this.currentActionProperties.put(PROP_APPROVE_FOLDER, approveFolderNode);
         
         if (rejectStep == null && rejectMove == null && rejectFolderNode == null)
         {
            this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "no");
         }
         else
         {
            this.currentActionProperties.put(PROP_REJECT_STEP_PRESENT, "yes");
            this.currentActionProperties.put(PROP_REJECT_STEP_NAME, rejectStep);
            this.currentActionProperties.put(PROP_REJECT_ACTION, rejectMove ? "move" : "copy");
            this.currentActionProperties.put(PROP_REJECT_FOLDER, rejectFolderNode);
         }
      }
      else if (LinkCategoryActionExecuter.NAME.equals(this.action))
      {
         NodeRef catNodeRef = (NodeRef)actionProps.get(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE);
         this.currentActionProperties.put(PROP_CATEGORY, catNodeRef);
      }
      else if (CheckOutActionExecuter.NAME.equals(this.action))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CheckOutActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (CheckInActionExecuter.NAME.equals(this.action))
      {
         String checkDesc = (String)actionProps.get(CheckInActionExecuter.PARAM_DESCRIPTION);
         this.currentActionProperties.put(PROP_CHECKIN_DESC, checkDesc);
         
         Boolean minorChange = (Boolean)actionProps.get(CheckInActionExecuter.PARAM_MINOR_CHANGE);
         this.currentActionProperties.put(PROP_CHECKIN_MINOR, minorChange);
      }
      else if (TransformActionExecuter.NAME.equals(this.action))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_TRANSFORMER, transformer);
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (ImageTransformActionExecuter.NAME.equals(this.action))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_IMAGE_TRANSFORMER, transformer);
         
         String options = (String)actionProps.get(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND);
         this.currentActionProperties.put(PROP_TRANSFORM_OPTIONS, options != null ? options : "");
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (MailActionExecuter.NAME.equals(this.action))
      {
         String subject = (String)actionProps.get(MailActionExecuter.PARAM_SUBJECT);
         this.currentActionProperties.put(PROP_SUBJECT, subject);
         
         String message = (String)actionProps.get(MailActionExecuter.PARAM_TEXT);
         this.currentActionProperties.put(PROP_MESSAGE, message);
         
         // handle single email or multiple authority recipients
         String to = (String)actionProps.get(MailActionExecuter.PARAM_TO);
         if (to != null)
         {
            this.currentActionProperties.put(PROP_TO, to);
         }
         else
         {
            List<String> recipients = (List<String>)actionProps.get(MailActionExecuter.PARAM_TO_MANY);
            if (recipients != null && recipients.size() != 0)
            {
               // rebuild the list of RecipientWrapper objects from the stored action
               for (String authority : recipients)
               {
                  this.emailRecipients.add(
                          new RecipientWrapper(displayLabelForAuthority(authority), authority));
               }
            }
         }
         
         NodeRef templateRef = (NodeRef)actionProps.get(MailActionExecuter.PARAM_TEMPLATE);
         if (templateRef != null)
         {
            this.currentActionProperties.put(PROP_TEMPLATE, templateRef.getId());
            this.usingTemplate = templateRef.getId();
         }
      }
      else if (ImporterActionExecuter.NAME.equals(this.action))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(ImporterActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (SpecialiseTypeActionExecuter.NAME.equals(this.action))
      {
          QName specialiseType = (QName)actionProps.get(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME);
          this.currentActionProperties.put(PROP_OBJECT_TYPE, specialiseType.toString());
      }
      else if (ScriptActionExecutor.NAME.equals(this.action))
      {
          NodeRef scriptRef = (NodeRef)actionProps.get(ScriptActionExecutor.PARAM_SCRIPTREF);
          this.currentActionProperties.put(PROP_SCRIPT, scriptRef.getId());
      }
   }
}
