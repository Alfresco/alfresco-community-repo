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
package org.alfresco.web.bean.workflow;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Cancel Workflow" dialog
 * 
 * @author gavinc
 */
public class CancelWorkflowDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -7875582893750792200L;
   
   transient private WorkflowInstance workflowInstance;
   transient private WorkflowService workflowService;
   
   private static final Log logger = LogFactory.getLog(CancelWorkflowDialog.class);   

   // ------------------------------------------------------------------------------
   // Dialog implementation
 
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // make sure the workflow instance id has been passed 
      String workflowInstanceId = this.parameters.get("workflow-instance-id");
      if (workflowInstanceId == null || workflowInstanceId.length() == 0)
      {
         throw new IllegalArgumentException("Cancel workflow dialog called without workflow instance id");
      }
      
      this.workflowInstance = getWorkflowService().getWorkflowById(workflowInstanceId);
      if (this.workflowInstance == null)
      {
         throw new IllegalArgumentException("Failed to find workflow instance for id: " + workflowInstanceId);
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Cancelling workflow with id: " + this.getWorkflowInstance().getId());
      
      WorkflowInstance instance = this.getWorkflowInstance();
      if(instance.isActive()) {
          // cancel the workflow
          this.getWorkflowService().cancelWorkflow(this.getWorkflowInstance().getId());
      }
      else
      {
          // delete the workflow
          this.getWorkflowService().deleteWorkflow(this.getWorkflowInstance().getId());
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Cancelled workflow with id: " + this.getWorkflowInstance().getId());
      
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
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "no");
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "yes");
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
      
      String workflowLabel = this.getWorkflowInstance().getDefinition().getTitle();
      if (this.getWorkflowInstance().getDescription() != null && this.getWorkflowInstance().getDescription().length() > 0)
      {
         workflowLabel = workflowLabel + " (" + this.getWorkflowInstance().getDescription() + ")";
      }
      
      return MessageFormat.format(confirmMsg, new Object[] {workflowLabel});
   }

   /**
    * Returns the workflow service instance
    * 
    * @return WorkflowService instance
    */
   public WorkflowService getWorkflowService()
   {
      if (workflowService == null)
      {
         workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
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
   
   protected WorkflowInstance getWorkflowInstance()
   {
      if (workflowInstance == null)
      {
         String workflowInstanceId = this.parameters.get("workflow-instance-id");
         if (workflowInstanceId == null || workflowInstanceId.length() == 0)
         {
            throw new IllegalArgumentException("Cancel workflow dialog called without workflow instance id");
         }

         this.workflowInstance = getWorkflowService().getWorkflowById(workflowInstanceId);
         if (this.workflowInstance == null)
         {
            throw new IllegalArgumentException("Failed to find workflow instance for id: " + workflowInstanceId);
         }
      }
      return workflowInstance;
   }

}
