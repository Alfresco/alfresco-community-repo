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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for languages
 * 
 * @author Gavin Cornwell
 */
public class LanguagesConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "languages";
         
   private Map<String, String> localeMap = new HashMap<String, String>();
   private List<String> languages = new ArrayList<String>(8);
   
   /**
    * Default Constructor
    */
   public LanguagesConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public LanguagesConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   @Override
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the languages config via the generic interfaces is not supported");
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      LanguagesConfigElement existingElement = (LanguagesConfigElement)configElement;
      LanguagesConfigElement newElement = new LanguagesConfigElement();

      // add the languages from this config element
      for (String locale : this.languages)
      {
         newElement.addLanguage(locale, this.localeMap.get(locale));
      }
      
      // now add the languages from the one to be combined (but 
      // only if they are not already in the list)
      List<String> languages = existingElement.getLanguages();
      for (String locale : languages)
      {
         if (newElement.getLabelForLanguage(locale) == null)
         {
            String label = existingElement.getLabelForLanguage(locale);
            newElement.addLanguage(locale, label);
         }
      }
      
      return newElement;
   }

   /**
    * Add a language locale and display label to the list.
    * 
    * @param locale     Locale code
    * @param label      Display label
    */
   /*package*/ void addLanguage(String locale, String label)
   {
      this.localeMap.put(locale, label);
      this.languages.add(locale);
   }
   
   /**
    * @return List of supported language locale strings in config file order
    */
   public List<String> getLanguages()
   {
      return this.languages;
   }
   
   /**
    * @param locale     The locale string to lookup language label for
    * 
    * @return the language label for specified locale string, or null if not found
    */
   public String getLabelForLanguage(String locale)
   {
      return this.localeMap.get(locale);
   }
}
