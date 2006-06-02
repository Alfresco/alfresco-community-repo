package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "simple-workflow" action.
 * 
 * @author gavinc
 */
public class SimpleWorkflowHandler extends BaseActionHandler
{
   public static final String PROP_APPROVE_STEP_NAME = "approveStepName";
   public static final String PROP_APPROVE_ACTION = "approveAction";
   public static final String PROP_APPROVE_FOLDER = "approveFolder";
   public static final String PROP_REJECT_STEP_PRESENT = "rejectStepPresent";
   public static final String PROP_REJECT_STEP_NAME = "rejectStepName";
   public static final String PROP_REJECT_ACTION = "rejectAction";
   public static final String PROP_REJECT_FOLDER = "rejectFolder";

   @Override
   public void setupUIDefaults(Map<String, Serializable> actionProps)
   {
      actionProps.put(PROP_APPROVE_ACTION, "move");
      actionProps.put(PROP_REJECT_STEP_PRESENT, "yes");
      actionProps.put(PROP_REJECT_ACTION, "move");
   }

   public String getJSPPath()
   {
      return getJSPPath(SimpleWorkflowActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the approve step name
      repoProps.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP,
            (String)actionProps.get(PROP_APPROVE_STEP_NAME));
      
      // add whether the approve step will copy or move the content
      boolean approveMove = true;
      String approveAction = (String)actionProps.get(PROP_APPROVE_ACTION);
      if (approveAction != null && approveAction.equals("copy"))
      {
         approveMove = false;
      }
      
      repoProps.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, Boolean.valueOf(approveMove));
      
      // add the destination folder of the content
      NodeRef approveDestNodeRef = null;
      Object approveDestNode = actionProps.get(PROP_APPROVE_FOLDER);
      if (approveDestNode instanceof NodeRef)
      {
         approveDestNodeRef = (NodeRef)approveDestNode;
      }
      else if (approveDestNode instanceof String)
      {
         approveDestNodeRef = new NodeRef((String)approveDestNode);
      }
      repoProps.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, approveDestNodeRef);
      
      // determine whether we have a reject step or not
      boolean requireReject = true;
      String rejectStepPresent = (String)actionProps.get(PROP_REJECT_STEP_PRESENT);
      if (rejectStepPresent != null && rejectStepPresent.equals("no"))
      {
         requireReject = false;
      }

      if (requireReject)
      {
         // add the reject step name
         repoProps.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP,
               (String)actionProps.get(PROP_REJECT_STEP_NAME));
      
         // add whether the reject step will copy or move the content
         boolean rejectMove = true;
         String rejectAction = (String)actionProps.get(PROP_REJECT_ACTION);
         if (rejectAction != null && rejectAction.equals("copy"))
         {
            rejectMove = false;
         }
         
         repoProps.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, Boolean.valueOf(rejectMove));
         
         // add the destination folder of the content
         NodeRef rejectDestNodeRef = null;
         Object rejectDestNode = actionProps.get(PROP_REJECT_FOLDER);
         if (rejectDestNode instanceof NodeRef)
         {
            rejectDestNodeRef = (NodeRef)rejectDestNode;
         }
         else if (rejectDestNode instanceof String)
         {
            rejectDestNodeRef = new NodeRef((String)rejectDestNode);
         }
         repoProps.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, rejectDestNodeRef);
      }
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      String approveStep = (String)repoProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP);
      Boolean approveMove = (Boolean)repoProps.get(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE);
      NodeRef approveFolderNode = (NodeRef)repoProps.get(
            SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER);
      
      String rejectStep = (String)repoProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP);
      Boolean rejectMove = (Boolean)repoProps.get(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE);
      NodeRef rejectFolderNode = (NodeRef)repoProps.get(
            SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER);
      
      actionProps.put(PROP_APPROVE_STEP_NAME, approveStep);
      actionProps.put(PROP_APPROVE_ACTION, approveMove ? "move" : "copy");
      actionProps.put(PROP_APPROVE_FOLDER, approveFolderNode);
      
      if (rejectStep == null && rejectMove == null && rejectFolderNode == null)
      {
         actionProps.put(PROP_REJECT_STEP_PRESENT, "no");
         actionProps.put(PROP_REJECT_ACTION, "move");
      }
      else
      {
         actionProps.put(PROP_REJECT_STEP_PRESENT, "yes");
         actionProps.put(PROP_REJECT_STEP_NAME, rejectStep);
         actionProps.put(PROP_REJECT_ACTION, rejectMove ? "move" : "copy");
         actionProps.put(PROP_REJECT_FOLDER, rejectFolderNode);
      }
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
      
      String approveStepName = (String)actionProps.get(PROP_APPROVE_STEP_NAME);
      String approveAction = (String)actionProps.get(PROP_APPROVE_ACTION);
      NodeRef approveFolder = (NodeRef)actionProps.get(PROP_APPROVE_FOLDER);
      String approveFolderName = Repository.getNameForNode(nodeService, approveFolder);
      String approveMsg = MessageFormat.format(
            Application.getMessage(context, "action_simple_workflow"), 
            new Object[] {Application.getMessage(context, approveAction), 
                          approveFolderName, approveStepName});
      
      String rejectMsg = null;
      String rejectStep = (String)actionProps.get(PROP_REJECT_STEP_PRESENT);
      if (rejectStep != null && "yes".equals(rejectStep))
      {
         String rejectStepName = (String)actionProps.get(PROP_REJECT_STEP_NAME);
         String rejectAction = (String)actionProps.get(PROP_REJECT_ACTION);
         NodeRef rejectFolder = (NodeRef)actionProps.get(PROP_REJECT_FOLDER);
         String rejectFolderName = Repository.getNameForNode(nodeService, rejectFolder);
         rejectMsg = MessageFormat.format(
               Application.getMessage(context, "action_simple_workflow"), 
               new Object[] {Application.getMessage(context, rejectAction),
                             rejectFolderName, rejectStepName});
      }
      
      StringBuilder builder = new StringBuilder(approveMsg);
      if (rejectMsg != null)
      {
         builder.append(" ");
         builder.append(rejectMsg);
      }
      
      return builder.toString();
   }
}
