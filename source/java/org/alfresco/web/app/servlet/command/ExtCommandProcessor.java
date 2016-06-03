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
