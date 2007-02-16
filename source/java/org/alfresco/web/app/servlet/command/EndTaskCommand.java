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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * End Task command implementation
 * 
 * @author David Caruana
 */
public final class EndTaskCommand implements Command
{
   public static final String PROP_TASK_ID = "taskId";
   public static final String PROP_TRANSITION = "transition";
   
   private static final String[] PROPERTIES = new String[] {PROP_TASK_ID, PROP_TRANSITION};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#getPropertyNames()
    */
   public String[] getPropertyNames()
   {
      return PROPERTIES;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      String taskId = (String)properties.get(PROP_TASK_ID);
      if (taskId == null)
      {
         throw new IllegalArgumentException("Unable to execute EndTaskCommand - mandatory parameter not supplied: " + PROP_TASK_ID);
      }
      String transition = (String)properties.get(PROP_TRANSITION);

      // end task
      WorkflowService workflowService = serviceRegistry.getWorkflowService();
      return workflowService.endTask(taskId, transition);
   }
}
