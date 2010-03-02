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
package org.alfresco.web.config;

import java.util.Iterator;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;
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
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
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
