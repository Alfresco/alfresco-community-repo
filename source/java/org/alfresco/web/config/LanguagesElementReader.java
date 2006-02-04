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
