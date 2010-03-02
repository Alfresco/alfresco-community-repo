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
package org.alfresco.web.bean.wcm;


/**
 * Configure Workflow sub-dialog for the Submit Dialog.
 * 
 * @author Kevin Roast
 */
public class SubmitConfigureWorkflowDialog extends FormWorkflowDialog
{
   private static final long serialVersionUID = 3828466420354771233L;
   
   protected SubmitDialog submitDialog;

   
   /**
    * @param submitDialog     The SubmitDialog to set.
    */
   public void setSubmitDialog(SubmitDialog submitDialog)
   {
      this.submitDialog = submitDialog;
   }


   /**
    * @return an object representing the workflow for the current action
    */
   @Override
   public WorkflowConfiguration getActionWorkflow()
   {
      return this.submitDialog.getActionWorkflow();
   }
}
