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
 * Custom element reader to parse config for languages
 * 
 * @author Gavin Cornwell
 */
public class LanguagesElementReader implements ConfigElementReader
{
   public static final String ELEMENT_LANGUAGE = "language";
   public static final String ATTRIBUTE_LOCALE = "locale";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      LanguagesConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(LanguagesConfigElement.CONFIG_ELEMENT_ID) == false)
         {
            throw new ConfigException("LanguagesElementReader can only parse " +
                  LanguagesConfigElement.CONFIG_ELEMENT_ID + " elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new LanguagesConfigElement();
         
         Iterator<Element> langsItr = element.elementIterator(ELEMENT_LANGUAGE);
         while (langsItr.hasNext())
         {
            Element language = langsItr.next();
            String localeCode = language.attributeValue(ATTRIBUTE_LOCALE);
            String label = language.getTextTrim();
            
            if (localeCode != null && localeCode.length() != 0 &&
                label != null && label.length() != 0)
            {
               // store the language code against the display label
               configElement.addLanguage(localeCode, label);
            }
         }
      }
      
      return configElement;
   }
}
