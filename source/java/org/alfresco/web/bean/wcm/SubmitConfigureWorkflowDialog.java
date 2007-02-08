/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean.wcm;


/**
 * Configure Workflow sub-dialog for the Submit Dialog.
 * 
 * @author Kevin Roast
 */
public class SubmitConfigureWorkflowDialog extends FormWorkflowDialog
{
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
