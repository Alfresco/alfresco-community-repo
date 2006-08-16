package org.alfresco.web.bean.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.TransientNode;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Manage WorkItem" dialog.
 * 
 * @author gavinc
 */
public class ManageWorkItemDialog extends BaseDialogBean
{
   protected WorkflowService workflowService;
   protected Node workItemNode;
   protected WorkflowTask workItem;
   protected WorkflowTransition[] transitions;

   protected static final String ID_PREFIX = "transition_";
   protected static final String CLIENT_ID_PREFIX = "dialog:" + ID_PREFIX;
   
   private static final Log logger = LogFactory.getLog(ManageWorkItemDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      String taskId = this.parameters.get("id");
      this.workItem = this.workflowService.getTaskById(taskId);
      
      if (this.workItem != null)
      {
         // setup a transient node to represent the work item we're managing
         WorkflowTaskDefinition taskDef = this.workItem.definition;
         this.workItemNode = new TransientNode(taskDef.metadata.getName(),
                  "task_" + System.currentTimeMillis(), this.workItem.properties);
               
         if (logger.isDebugEnabled())
            logger.debug("Created node for work item: " + this.workItemNode);
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Saving work item with params: " + this.workItemNode.getProperties());
      
      // prepare the edited parameters for saving
      Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.workItemNode);
      
      // update the task with the updated parameters
      this.workflowService.updateTask(this.workItem.id, params, null, null);
      
      return outcome;
   }

   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      List<DialogButtonConfig> buttons = null;

      if (this.workItem != null)
      {
         // get the transitions available from this work item and 
         // show them in the dialog as additional buttons
         this.transitions = this.workItem.path.node.transitions;

         if (this.transitions != null)
         {
            buttons = new ArrayList<DialogButtonConfig>(this.transitions.length);
            
            for (WorkflowTransition trans : this.transitions)
            {
               buttons.add(new DialogButtonConfig(ID_PREFIX + trans, trans.title, null,
                     "#{DialogManager.bean.transition}", "false", null));
            }
         }
      }
      
      return buttons;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "save");
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   // ------------------------------------------------------------------------------
   // Event handlers

   @SuppressWarnings("unused")
   public String transition()
   {
      String outcome = getDefaultFinishOutcome();
      
      // to find out which transition button was pressed we need
      // to look for the button's id in the request parameters,
      // the first non-null result is the button that was pressed.
      FacesContext context = FacesContext.getCurrentInstance();
      Map reqParams = context.getExternalContext().getRequestParameterMap();
      
      String selectedTransition = null;
      for (WorkflowTransition trans : this.transitions)
      {
         Object result = reqParams.get(CLIENT_ID_PREFIX + trans);
         if (result != null)
         {
            // this was the button that was pressed
            selectedTransition = trans.id;
            break;
         }
      }
      
      if (selectedTransition != null)
      {
         UserTransaction tx = null;
      
         try
         {
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // prepare the edited parameters for saving
            Map<QName, Serializable> params = WorkflowBean.prepareWorkItemParams(this.workItemNode);
      
            // update the task with the updated parameters
            this.workflowService.updateTask(this.workItem.id, params, null, null);
         
            // signal the selected transition to the workflow task
            this.workflowService.endTask(this.workItem.id, selectedTransition);
            
            // commit the changes
            tx.commit();
            
            if (logger.isDebugEnabled())
               logger.debug("Ended work item with transition: " + selectedTransition);
         }
         catch (Throwable e)
         {
            // reset the flag so we can re-attempt the operation
            isFinished = false;
            
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(formatErrorMessage(e));
            outcome = this.getErrorOutcome(e);
         }
      }
      
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns the Node representing the work item
    * 
    * @return The node
    */
   public Node getWorkItemNode()
   {
      return this.workItemNode;
   }
   
   /**
    * Sets the workflow service to use
    * 
    * @param workflowService
    *           WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
}
