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
package org.alfresco.web.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * @author Kevin Roast
 */
public class CommandServletConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "command-servlet";
   
   private Map<String, Class> commandProcessors = new HashMap<String, Class>(4, 1.0f);
   
   /**
    * Default constructor
    */
   public CommandServletConfigElement()
   {
      super("command-servlet");
   }

   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public CommandServletConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the Command Servlet config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      CommandServletConfigElement existingElement = (CommandServletConfigElement)configElement;
      CommandServletConfigElement newElement = new CommandServletConfigElement();
      
      for (String name : commandProcessors.keySet())
      {
         newElement.addCommandProcessor(name, commandProcessors.get(name));
      }
      for (String name : existingElement.commandProcessors.keySet())
      {
         newElement.addCommandProcessor(name, existingElement.commandProcessors.get(name));
      }
      
      return newElement;
   }
   
   /*package*/ void addCommandProcessor(String name, String className)
   {
      try
      {
         Class clazz = Class.forName(className);
         commandProcessors.put(name, clazz);
      }
      catch (Throwable err)
      {
         throw new ConfigException("Unable to load command proccessor class: " +
               className + " due to " + err.getMessage());
      }
   }
   
   private void addCommandProcessor(String name, Class clazz)
   {
      commandProcessors.put(name, clazz);
   }
   
   public Class getCommandProcessor(String name)
   {
      return commandProcessors.get(name);
   }
}
