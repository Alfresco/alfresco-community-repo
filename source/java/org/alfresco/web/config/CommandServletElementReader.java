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

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

/**
 * @author Kevin Roast
 */
public class CommandServletElementReader implements ConfigElementReader
{
   public static final String ELEMENT_COMMANDPROCESSOR = "command-processor";
   public static final String ATTRIBUTE_NAME = "name";
   public static final String ATTRIBUTE_CLASS = "class";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      CommandServletConfigElement configElement = new CommandServletConfigElement();
      
      if (element != null)
      {
         if (CommandServletConfigElement.CONFIG_ELEMENT_ID.equals(element.getName()) == false)
         {
            throw new ConfigException("CommandServletElementReader can only parse config elements of type 'command-servlet'");
         }
         
         Iterator<Element> itr = element.elementIterator(ELEMENT_COMMANDPROCESSOR);
         while (itr.hasNext())
         {
            Element procElement = itr.next();
            
            String name = procElement.attributeValue(ATTRIBUTE_NAME);
            String className = procElement.attributeValue(ATTRIBUTE_CLASS);
            
            if (name == null || name.length() == 0)
            {
               throw new ConfigException("'name' attribute is mandatory for command processor config element.");
            }
            if (className == null || className.length() == 0)
            {
               throw new ConfigException("'class' attribute is mandatory for command processor config element.");
            }
            
            configElement.addCommandProcessor(name, className);
         }
      }
      
      return configElement;
   }
}
