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
package org.alfresco.web.bean.workflow;

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.workflow.WorkflowInstance;
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
   protected WorkflowInstance workflowInstance;
   protected WorkflowService workflowService;
   
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
      
      this.workflowInstance = workflowService.getWorkflowById(workflowInstanceId);
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
         logger.debug("Cancelling workflow with id: " + this.workflowInstance.id);
      
      // cancel the workflow
      this.workflowService.cancelWorkflow(this.workflowInstance.id);
      
      if (logger.isDebugEnabled())
         logger.debug("Cancelled workflow with id: " + this.workflowInstance.id);
      
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
      
      String workflowLabel = this.workflowInstance.definition.title;
      if (this.workflowInstance.description != null && this.workflowInstance.description.length() > 0)
      {
         workflowLabel = workflowLabel + " (" + this.workflowInstance.description + ")";
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
