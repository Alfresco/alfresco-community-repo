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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;

/**
 * Task specific command processor implementation.
 * <p>
 * Responsible for executing workflow task operations.
 * 
 * @author David Caruana
 */
public final class TaskCommandProcessor implements CommandProcessor
{
   private String taskId;
   private String transition = null;
   private String command;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("end", EndTaskCommand.class);
   }

   
   /* (non-Javadoc)
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String command, Map<String, String> args, String[] urlElements)
   {
       if (urlElements.length == 0)
       {
          throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
       }
       taskId = urlElements[0];
       if (urlElements.length == 2)
       {
           transition = urlElements[1];
       }
       return true;
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#process(org.alfresco.service.ServiceRegistry, javax.servlet.http.HttpServletRequest, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String command)
   {
      Map<String, Object> properties = new HashMap<String, Object>(1, 1.0f);
      // all workflow commands use a "target" Node property as an argument
      properties.put(EndTaskCommand.PROP_TASK_ID, taskId);
      if (transition != null)
      {
          properties.put(EndTaskCommand.PROP_TRANSITION, transition);
      }
      Command cmd = CommandFactory.getInstance().createCommand(command);
      if (cmd == null)
      {
         throw new AlfrescoRuntimeException("Unregistered workflow command specified: " + command);
      }
      cmd.execute(serviceRegistry, properties);
      this.command = command;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#outputStatus(java.io.PrintWriter)
    */
   public void outputStatus(PrintWriter out)
   {
      out.print("Task command: '");
      out.print(command);
      out.print("' executed against task: ");
      out.println(taskId);
   }

}
