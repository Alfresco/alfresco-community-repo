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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
