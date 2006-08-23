/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
