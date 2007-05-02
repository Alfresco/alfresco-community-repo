/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;

/**
 * This interfaces defines the contract and lifecycle of a Servlet Command Processor.
 * <p>
 * The ExtCommandProcessor adds an overloaded process() method to allow the
 * HttpServletResponse to be passed.
 * 
 * @author Kevin Roast
 */
public interface ExtCommandProcessor extends CommandProcessor
{
   /**
    * Process the supplied command name. It is the responsibility of the Command Processor
    * to lookup the specified command name using the CommandFactory registry. For that reason
    * it also has the responsiblity to initially register commands it is responsible for so
    * they can be constructed later. If the supplied command is unknown to it then an
    * exception should be thrown to indicate this.
    *  
    * @param serviceRegistry  ServiceRegistry
    * @param request          HttpServletRequest
    * @param response         HttpServletResponse
    * @param command          Name of the command to construct and execute
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, HttpServletResponse response, String command);
}
