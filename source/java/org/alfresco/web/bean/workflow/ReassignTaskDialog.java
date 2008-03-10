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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Reassign Task" dialog
 * 
 * @author gavinc
 */
public class ReassignTaskDialog extends BaseReassignDialog
{
   private static final long serialVersionUID = 5804171557325189475L;

   protected String taskId;
   
   private static final Log logger = LogFactory.getLog(ReassignTaskDialog.class);   

   
   // ------------------------------------------------------------------------------
   // Dialog implementation
 
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.taskId = this.parameters.get("id");
      if (this.taskId == null || this.taskId.length() == 0)
      {
         throw new IllegalArgumentException("Reassign task dialog called without task id");
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Reassigning task with id: " + this.taskId);
      
      UIComponent picker = context.getViewRoot().findComponent("dialog:dialog-body:user-picker");
      
      if (picker != null && picker instanceof UIGenericPicker)
      {
         UIGenericPicker userPicker = (UIGenericPicker)picker;
         String[] user = userPicker.getSelectedResults();
         if (user != null && user.length > 0)
         {
            // create a map to hold the new owner property then update the task
            String userName = user[0];
            Map<QName, Serializable> params = new HashMap<QName, Serializable>(1);
            params.put(ContentModel.PROP_OWNER, userName);
            this.getWorkflowService().updateTask(this.taskId, params, null, null);
         }
         else
         {
            if (logger.isWarnEnabled())
               logger.warn("Failed to find selected user, reassign was unsuccessful");
         }
      }
      else
      {
         if (logger.isWarnEnabled())
            logger.warn("Failed to find user-picker component, reassign was unsuccessful");
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Reassigning task with id: " + this.taskId);
      
      return outcome;
   }
   
   @Override
   protected String getErrorMessageId()
   {
      return "error_reassign_task";
   }
}
