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
package org.alfresco.web.app.servlet.command;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;

/**
 * Script command processor implementation.
 * <p>
 * Responsible for executing 'execute' script commands on a Node.
 * 
 * @author Kevin Roast
 */
public final class ScriptCommandProcessor implements CommandProcessor
{
   private static final String ARG_SCRIPT_PATH  = "scriptPath";
   private static final String ARG_CONTEXT_PATH = "contextPath";
   
   private NodeRef scriptRef;
   private NodeRef docRef;
   private Object result;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("execute", ExecuteScriptCommand.class);
   }
   
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String command, Map<String, String> args, String[] urlElements)
   {
      boolean allowed = false;
      String scriptPath = args.get(ARG_SCRIPT_PATH);
      if (scriptPath != null)
      {
         // resolve path to a node
         this.scriptRef = BaseServlet.resolveNamePath(sc, scriptPath).NodeRef;
         
         // same for the document context path if specified
         String docPath = args.get(ARG_CONTEXT_PATH);
         if (docPath != null)
         {
            this.docRef = BaseServlet.resolveNamePath(sc, docPath).NodeRef;
         }
      }
      else
      {
         if (urlElements.length < 3)
         {
            throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
         }
         
         // get NodeRef to the node script to execute
         StoreRef storeRef = new StoreRef(urlElements[0], urlElements[1]);
         this.scriptRef = new NodeRef(storeRef, urlElements[2]);
         
         if (urlElements.length >= 6)
         {
            storeRef = new StoreRef(urlElements[3], urlElements[4]);
            this.docRef = new NodeRef(storeRef, urlElements[5]);
         }
      }
      
      // check we can access the nodes specified
      PermissionService ps = Repository.getServiceRegistry(sc).getPermissionService();
      allowed = (ps.hasPermission(this.scriptRef, PermissionService.READ) == AccessStatus.ALLOWED);
      if (this.docRef != null)
      {
         allowed &= (ps.hasPermission(this.docRef, PermissionService.READ) == AccessStatus.ALLOWED);
      }
      
      // check that the user has at least READ access on the node - else redirect to the login page
      return allowed;
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#process(org.alfresco.service.ServiceRegistry, javax.servlet.http.HttpServletRequest, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String command)
   {
      Map<String, Object> properties = new HashMap<String, Object>(4, 1.0f);
      
      properties.put(ExecuteScriptCommand.PROP_SCRIPT, this.scriptRef);
      properties.put(ExecuteScriptCommand.PROP_DOCUMENT, this.docRef);
      User user = Application.getCurrentUser(request.getSession());
      properties.put(ExecuteScriptCommand.PROP_USERPERSON, user.getPerson());
      
      // add URL arguments as a special Scriptable Map property called 'args' 
      Map<String, String> args = new ScriptableHashMap<String, String>();
      Enumeration names = request.getParameterNames();
      while (names.hasMoreElements())
      {
         String name = (String)names.nextElement();
         args.put(name, request.getParameter(name));
      }
      properties.put(ExecuteScriptCommand.PROP_ARGS, args);
      
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
