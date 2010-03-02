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
