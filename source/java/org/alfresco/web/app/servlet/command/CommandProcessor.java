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

import javax.servlet.http.HttpSession;

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
    * @param serviceRegistry  ServiceRegistry instance
    * @param command          Name of the command the arguments are for
    * @param args             String[] of the remaining URL arguments to the command servlet.
    * 
    * @return true if the command can be executed by the current user given the supplied args.
    */
   public boolean validateArguments(ServiceRegistry serviceRegistry, String command, String[] args);
   
   /**
    * Process the supplied command name. It is the responsibility of the Command Processor
    * to lookup the specified command name using the CommandFactory registry. For that reason
    * it also has the responsiblity to initially register commands it is responsible for so
    * they can be constructed later. If the supplied command is unknown to it then an
    * exception should be thrown to indicate this.
    *  
    * @param serviceRegistry  ServiceRegistry
    * @param session          HttpSession
    * @param command          Name of the command to construct and execute
    */
   public void process(ServiceRegistry serviceRegistry, HttpSession session, String command);
   
   /**
    * Output a simple status message to the supplied PrintWriter.
    * It can be assumed that the process() method was successful if this method is called.
    * 
    * @param out              PrintWriter
    */
   public void outputStatus(PrintWriter out);
}
