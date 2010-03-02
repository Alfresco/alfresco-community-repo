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

import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;

/**
 * Bean implementation for the "View Completed Task" dialog.
 * 
 * @author gavinc
 */
public class ViewCompletedTaskDialog extends ManageTaskDialog
{
   // ------------------------------------------------------------------------------
   // Dialog implementation

   private static final long serialVersionUID = 1568710712589201055L;

   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // nothing to do as the finish button is not shown and the dialog is read only
      
      return outcome;
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   @Override
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      return null;
   }
   
   @Override
   public String getContainerTitle()
   {
      String titleStart = Application.getMessage(FacesContext.getCurrentInstance(), "view_completed_task_title");
         
      return titleStart + ": " + this.getWorkflowTask().title;
   }
}
