package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;

/**
 * Simple servlet command pattern interface.
 * 
 * @author Kevin Roast
 */
public interface Command
{
   /**
    * Execute the command
    * 
    * @param serviceRegistry     The ServiceRegistry instance
    * @param properties          Bag of named properties for the command
    * 
    * @return return value from the command if any
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties);
   
   /**
    * @return the names of the properties required for this command
    */
   public String[] getPropertyNames();
}
