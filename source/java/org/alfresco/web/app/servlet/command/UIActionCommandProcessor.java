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

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.Utils;

/**
 * UI action command processor implementation.
 * <p>
 * Responsible for executing specific UI actions via a REST style URL interface.
 * <p>
 * The URL postfix for each specific command depends on the context that is required
 * for that command. For example, a command to launch the Create Web Content dialog may
 * require the current sandbox and the current web project as its context e.g.
 * <br>
 * http://server/alfresco/command/ui/createwebcontent?sandbox=website1&webproject=1234567890
 * 
 * @author Kevin Roast
 */
public class UIActionCommandProcessor implements ExtCommandProcessor
{
   public static final String PARAM_CONTAINER = "container";
   
   private ServletContext sc = null;
   private String command = null;
   private Map<String, String> args = null;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("createwebcontent", CreateWebContentCommand.class);
      CommandFactory.getInstance().registerCommand("editwebcontent", EditWebContentCommand.class);
      CommandFactory.getInstance().registerCommand("managetask", ManageTaskDialogCommand.class);
      CommandFactory.getInstance().registerCommand("editcontentprops", EditContentPropertiesCommand.class);
      CommandFactory.getInstance().registerCommand("userprofile", UserProfileDialogCommand.class);
   }
   
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String command, Map<String, String> args, String[] urlElements)
   {
      this.sc = sc;
      if (args.size() != 0)
      {
         this.args = new HashMap<String, String>(args);
      }
      return true;
   }
   
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String command)
   {
      // not implemented in ExtCommandProcessor!
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.ExtCommandProcessor#process(org.alfresco.service.ServiceRegistry, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, HttpServletResponse response, String command)
   {
      Map<String, Object> properties = new HashMap<String, Object>(this.args);
      
      properties.put(BaseUIActionCommand.PROP_SERVLETCONTEXT, this.sc);
      properties.put(BaseUIActionCommand.PROP_REQUEST, request);
      properties.put(BaseUIActionCommand.PROP_RESPONSE, response);
      
      // if the container parameter is present and equal to "plain" add the 
      // external container object to the session
      String container = request.getParameter(PARAM_CONTAINER);
      if (container != null && container.equalsIgnoreCase("plain"))
      {
         request.getSession().setAttribute(
                  AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.TRUE);
      }
      
      Command cmd = CommandFactory.getInstance().createCommand(command);
      if (cmd == null)
      {
         throw new AlfrescoRuntimeException("Unregistered UI Action command specified: " + command);
      }
      cmd.execute(serviceRegistry, properties);
      this.command = command;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#outputStatus(java.io.PrintWriter)
    */
   public void outputStatus(PrintWriter out)
   {
      out.print("UI Action command: '");
      out.print(Utils.encode(this.command));
      out.print("' executed with args: ");
      out.println(this.args != null ? (Utils.encode(this.args.toString())) : "");
   }
}
