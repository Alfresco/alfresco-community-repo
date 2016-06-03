package org.alfresco.web.app.servlet.command;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.alfresco.web.ui.common.Utils;

/**
 * Task specific command processor implementation.
 * <p>
 * Responsible for executing workflow task operations.
 * 
 * @author David Caruana
 */
public final class TaskCommandProcessor implements CommandProcessor
{
   private String taskId;
   private String transition = null;
   private String command;
   
   static
   {
      // add our commands to the command registry
      CommandFactory.getInstance().registerCommand("end", EndTaskCommand.class);
   }

   
   /* (non-Javadoc)
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String cmd, Map<String, String> args, String[] urlElements)
   {
       if (urlElements.length == 0)
       {
          throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
       }
       taskId = urlElements[0];
       if (urlElements.length == 2)
       {
           transition = urlElements[1];
       }
       return WorkflowUtil.isTaskEditable(taskId, sc);
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#process(org.alfresco.service.ServiceRegistry, javax.servlet.http.HttpServletRequest, java.lang.String)
    */
   public void process(ServiceRegistry serviceRegistry, HttpServletRequest request, String commandName)
   {
      Map<String, Object> properties = new HashMap<String, Object>(1, 1.0f);
      // all workflow commands use a "target" Node property as an argument
      properties.put(EndTaskCommand.PROP_TASK_ID, taskId);
      if (transition != null)
      {
          properties.put(EndTaskCommand.PROP_TRANSITION, transition);
      }
      Command cmd = CommandFactory.getInstance().createCommand(commandName);
      if (cmd == null)
      {
         throw new AlfrescoRuntimeException("Unregistered workflow command specified: " + commandName);
      }
      cmd.execute(serviceRegistry, properties);
      this.command = commandName;
   }

   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#outputStatus(java.io.PrintWriter)
    */
   public void outputStatus(PrintWriter out)
   {
      out.print("Task command: '");
      out.print(Utils.encode(command));
      out.print("' executed against task: ");
      out.println(Utils.encode(taskId));
   }

}
