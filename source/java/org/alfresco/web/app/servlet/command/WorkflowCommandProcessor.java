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
package org.alfresco.web.app.servlet.command;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.ui.common.Utils;

/**
 * Workflow specific command processor implementation.
 * <p>
 * Responsible for executing 'approve' and 'reject' workflow commands on a Node.
 * 
 * @author Kevin Roast
 */
public final class WorkflowCommandProcessor extends BaseNodeCommandProcessor
{
   private String command;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("approve", ApproveWorkflowCommand.class);
      CommandFactory.getInstance().registerCommand("reject", RejectWorkflowCommand.class);
   }

   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#process(org.alfresco.service.ServiceRegistry, javax.servlet.http.HttpServletRequest, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String command)
   {
      Map<String, Object> properties = new HashMap<String, Object>(1, 1.0f);
      // all workflow commands use a "target" Node property as an argument
      properties.put(ApproveWorkflowCommand.PROP_TARGET, this.targetRef);
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
      out.print("Workflow command: '");
      out.print(Utils.encode(this.command));
      out.print("' executed against node: ");
      out.println(Utils.encode(this.targetRef.toString()));
   }
}
