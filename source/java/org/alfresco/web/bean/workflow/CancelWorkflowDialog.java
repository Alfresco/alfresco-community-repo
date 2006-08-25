package org.alfresco.web.bean.workflow;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Cancel Workflow" dialog
 * 
 * @author gavinc
 */
public class CancelWorkflowDialog extends BaseDialogBean
{
   protected String workflowInstanceId;
   protected WorkflowService workflowService;
   
   private static final Log logger = LogFactory.getLog(CancelWorkflowDialog.class);   

   // ------------------------------------------------------------------------------
   // Dialog implementation
 
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // make sure the workflow instance id has been passed 
      this.workflowInstanceId = this.parameters.get("workflow-instance-id");
      if (this.workflowInstanceId == null || this.workflowInstanceId.length() == 0)
      {
         throw new IllegalArgumentException("Cancel workflow dialog called without workflow instance id");
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Cancelling workflow with id: " + this.workflowInstanceId);
      
      // cancel the workflow
      this.workflowService.cancelWorkflow(this.workflowInstanceId);
      
      if (logger.isDebugEnabled())
         logger.debug("Cancelled workflow with id: " + this.workflowInstanceId);
      
      return outcome;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_cancel_workflow";
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * Returns the confirmation to display to the user before deleting the content.
    * 
    * @return The formatted message to display
    */
   public String getConfirmMessage()
   {
      String confirmMsg = Application.getMessage(FacesContext.getCurrentInstance(), 
               "cancel_workflow_confirm");
      
      return MessageFormat.format(confirmMsg, 
            new Object[] {this.parameters.get("workflow-instance-name")});
   }

   /**
    * Returns the workflow service instance
    * 
    * @return WorkflowService instance
    */
   public WorkflowService getWorkflowService()
   {
      return workflowService;
   }

   /**
    * Sets the workflow service to use
    * 
    * @param workflowService The WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
}
