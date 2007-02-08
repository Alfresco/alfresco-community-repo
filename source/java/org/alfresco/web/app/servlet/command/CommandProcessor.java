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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.service.ServiceRegistry;

/**
 * This interfaces defines the contract and lifecycle of a Servlet Command Processor.
 * <p>
 * A command processor is defined as a class capable of executing a set of related Command
 * objects. It performs the bulk of the work for the command servlet. The processor impl
 * is responsible for validating that the command can be processed (given the supplied remaining
 * URL arguments from the servlet) and processing the command. It is also responsible for
 * supply an output status page on successfuly execution of the command.
 * <p>
 * The arguments passed to a Command Processor are the remaining URL elements from the command
 * servlet URL after removing the web-app name, servlet name and command processor name.  
 * 
 * @author Kevin Roast
 */
public interface CommandProcessor
{
   /**
    * Pass and validate URL arguments for the command processor. Validate if the command can be
    * executed given the arguments supplied. Generally at this post a Command Processor will
    * convert the supplied arguments to the objects it expects, and also check any permissions
    * that are required by the current user to execute the command.
    * 
    * @param sc               ServletContext, can be used to retrieve ServiceRegistry instance
    *                         from the Repository bean.
    * @param command          Name of the command the arguments are for
    * @param args             Map of URL args passed to the command servlet
    * @param urlElements      String[] of the remaining URL arguments to the command servlet
    * 
    * @return true if the command can be executed by the current user given the supplied args.
    */
   public boolean validateArguments(ServletContext sc, String command, Map<String, String> args, String[] urlElements);
   
   /**
    * Process the supplied command name. It is the responsibility of the Command Processor
    * to lookup the specified command name using the CommandFactory registry. For that reason
    * it also has the responsiblity to initially register commands it is responsible for so
    * they can be constructed later. If the supplied command is unknown to it then an
    * exception should be thrown to indicate this.
    *  
    * @param serviceRegistry  ServiceRegistry
    * @param request          HttpServletRequest
    * @param command          Name of the command to construct and execute
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String command);
   
   /**
    * Output a simple status message to the supplied PrintWriter.
    * It can be assumed that the process() method was successful if this method is called.
    * 
    * @param out              PrintWriter
    */
   public void outputStatus(PrintWriter out);
}
