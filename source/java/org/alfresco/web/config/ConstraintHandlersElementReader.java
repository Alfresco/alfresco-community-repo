/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
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
 * This class is a custom element reader to parse the config file for
 * &lt;constraint-handlers&gt; elements.
 * 
 * @author Neil McErlean.
 */
public class ConstraintHandlersElementReader implements ConfigElementReader
{
   public static final String ELEMENT_CONSTRAINT_HANDLERS = "constraint-handlers";
   public static final String ATTR_TYPE = "type";
   public static final String ATTR_VALIDATOR_HANDLER = "validation-handler";
   public static final String ATTR_MESSAGE = "message";
   public static final String ATTR_MESSAGE_ID = "message-id";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
	   ConstraintHandlersConfigElement result = null;
	   if (element == null)
	   {
		   return null;
	   }
	   
	   String name = element.getName();
	   if (!name.equals(ELEMENT_CONSTRAINT_HANDLERS))
	   {
		   throw new ConfigException(this.getClass().getName() + " can only parse " +
				   ELEMENT_CONSTRAINT_HANDLERS + " elements, the element passed was '" + name + "'");
	   }
	   
	   result = new ConstraintHandlersConfigElement();
	   
	   Iterator<Element> xmlNodes = element.elementIterator();
	   while (xmlNodes.hasNext())
	   {
		   Element nextNode = xmlNodes.next();
           String type = nextNode.attributeValue(ATTR_TYPE);
           String validationHandler = nextNode.attributeValue(ATTR_VALIDATOR_HANDLER);
           String message = nextNode.attributeValue(ATTR_MESSAGE);
           String messageId = nextNode.attributeValue(ATTR_MESSAGE_ID);
           
           result.addDataMapping(type, validationHandler, message, messageId);
	   }
	   
	   return result;
   }
}
