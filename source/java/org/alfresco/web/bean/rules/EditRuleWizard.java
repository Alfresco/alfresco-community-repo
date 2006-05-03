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

   @Override
   public void init()
   {
      super.init();
      
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
         populateAction(action.getParameterValues());
         
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
    * Populate the actionProperties member variable with correct props for the current action
    * using the supplied property map.
    * 
    * @param actionProps Map to retrieve props appropriate to the current action from
    */
   protected void populateAction(Map<String, Serializable> actionProps)
   {
      if (this.action.equals(AddFeaturesActionExecuter.NAME))
      {
         QName aspect = (QName)actionProps.get(AddFeaturesActionExecuter.PARAM_ASPECT_NAME);
         this.currentActionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (this.action.equals(RemoveFeaturesActionExecuter.NAME))
      {
          QName aspect = (QName)actionProps.get(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME);
          this.currentActionProperties.put(PROP_ASPECT, aspect.toString());
      }
      else if (this.action.equals(CopyActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(MoveActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(SimpleWorkflowActionExecuter.NAME))
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
      else if (this.action.equals(LinkCategoryActionExecuter.NAME))
      {
         NodeRef catNodeRef = (NodeRef)actionProps.get(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE);
         this.currentActionProperties.put(PROP_CATEGORY, catNodeRef);
      }
      else if (this.action.equals(CheckOutActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(CheckOutActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(CheckInActionExecuter.NAME))
      {
         String checkDesc = (String)actionProps.get(CheckInActionExecuter.PARAM_DESCRIPTION);
         this.currentActionProperties.put(PROP_CHECKIN_DESC, checkDesc);
         
         Boolean minorChange = (Boolean)actionProps.get(CheckInActionExecuter.PARAM_MINOR_CHANGE);
         this.currentActionProperties.put(PROP_CHECKIN_MINOR, minorChange);
      }
      else if (this.action.equals(TransformActionExecuter.NAME))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_TRANSFORMER, transformer);
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(ImageTransformActionExecuter.NAME))
      {
         String transformer = (String)actionProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
         this.currentActionProperties.put(PROP_IMAGE_TRANSFORMER, transformer);
         
         String options = (String)actionProps.get(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND);
         this.currentActionProperties.put(PROP_TRANSFORM_OPTIONS, options != null ? options : "");
         
         NodeRef destNodeRef = (NodeRef)actionProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(MailActionExecuter.NAME))
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
      else if (this.action.equals(ImporterActionExecuter.NAME))
      {
         NodeRef destNodeRef = (NodeRef)actionProps.get(ImporterActionExecuter.PARAM_DESTINATION_FOLDER);
         this.currentActionProperties.put(PROP_DESTINATION, destNodeRef);
      }
      else if (this.action.equals(SpecialiseTypeActionExecuter.NAME) == true)
      {
          QName specialiseType = (QName)actionProps.get(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME);
          this.currentActionProperties.put(PROP_OBJECT_TYPE, specialiseType.toString());
      }
      else if (this.action.equals(ScriptActionExecutor.NAME))
      {
          NodeRef scriptRef = (NodeRef)actionProps.get(ScriptActionExecutor.PARAM_SCRIPTREF);
          this.currentActionProperties.put(PROP_SCRIPT, scriptRef.getId());
      }
   }
}
