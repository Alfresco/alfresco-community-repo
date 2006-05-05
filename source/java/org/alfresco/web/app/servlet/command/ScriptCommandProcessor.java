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

import javax.servlet.http.HttpSession;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.User;

/**
 * @author Kevin Roast
 */
public final class ScriptCommandProcessor implements CommandProcessor
{
   private NodeRef scriptRef;
   private NodeRef docRef;
   private Object result;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("execute", ExecuteScriptCommand.class);
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(org.alfresco.service.ServiceRegistry, java.lang.String, java.lang.String[])
    */
   public boolean validateArguments(ServiceRegistry serviceRegistry, String command, String[] args)
   {
      if (args.length < 3)
      {
         throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
      }
      
      // get NodeRef to the node script to execute
      StoreRef storeRef = new StoreRef(args[0], args[1]);
      this.scriptRef = new NodeRef(storeRef, args[2]);
      
      if (args.length >= 6)
      {
         storeRef = new StoreRef(args[3], args[4]);
         this.docRef = new NodeRef(storeRef, args[5]);
      }
      
      // check we can access the nodes specified
      PermissionService ps = serviceRegistry.getPermissionService();
      boolean allowed = (ps.hasPermission(this.scriptRef, PermissionService.READ) == AccessStatus.ALLOWED);
      if (this.docRef != null)
      {
         allowed &= (ps.hasPermission(this.docRef, PermissionService.READ) == AccessStatus.ALLOWED);
      }
      
      // check that the user has at least READ access on the node - else redirect to the login page
      return allowed;
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#process(org.alfresco.service.ServiceRegistry, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpSession session, String command)
   {
      Map<String, Object> properties = new HashMap<String, Object>(2, 1.0f);
      properties.put(ExecuteScriptCommand.PROP_SCRIPT, this.scriptRef);
      properties.put(ExecuteScriptCommand.PROP_DOCUMENT, this.docRef);
      User user = Application.getCurrentUser(session);
      properties.put(ExecuteScriptCommand.PROP_USERPERSON, user.getPerson());
      
      Command cmd = CommandFactory.getInstance().createCommand(command);
      if (cmd == null)
      {
         throw new AlfrescoRuntimeException("Unregistered script command specified: " + command);
      }
      this.result = cmd.execute(serviceRegistry, properties);
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#outputStatus(java.io.PrintWriter)
    */
   public void outputStatus(PrintWriter out)
   {
      out.write(this.result != null ? this.result.toString() : "Successfully executed script.");
   }
}
