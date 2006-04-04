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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Command Factory helper
 * 
 * @author Kevin Roast
 */
public final class CommandFactory
{
   private static Log logger = LogFactory.getLog(CommandFactory.class);
   
   private static CommandFactory instance = new CommandFactory();
   
   private static Map<String, Class> registry = new HashMap<String, Class>(16, 1.0f);
   
   /**
    * Private constructor - protect the singleton instance
    */
   private CommandFactory()
   {
   }
   
   /**
    * @return the singleton CommandFactory instance
    */
   public static CommandFactory getInstance()
   {
      return instance;
   }
   
   /**
    * Register a command name against an implementation
    * 
    * @param name       Unique name of the command
    * @param clazz      Class implementation of the command
    */
   public void registerCommand(String name, Class clazz)
   {
      registry.put(name, clazz);
   }
   
   /**
    * Create a command instance of the specified command name
    * 
    * @param name       Name of the command to create (must be registered)
    * 
    * @return the Command instance or null if not found
    */
   public Command createCommand(String name)
   {
      Command result = null;
      
      // lookup command by name in the registry
      Class clazz = registry.get(name);
      if (clazz != null)
      {
         try
         {
            Object obj = clazz.newInstance();
            if (obj instanceof Command)
            {
               result = (Command)obj;
            }
         }
         catch (Throwable err)
         {
            // return default if this occurs
            logger.warn("Unable to create workflow command instance '" + name +
                  "' with classname '" + clazz.getName() + "' due to error: " + err.getMessage());
         }
      }
      
      return result;
   }
}
