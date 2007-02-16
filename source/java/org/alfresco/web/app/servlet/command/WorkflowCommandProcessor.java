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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;

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
      out.print(this.command);
      out.print("' executed against node: ");
      out.println(this.targetRef.toString());
   }
}
