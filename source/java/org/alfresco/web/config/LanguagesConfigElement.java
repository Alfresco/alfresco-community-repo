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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.element.ConfigElementAdapter;

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
    * @see org.springframework.extensions.config.element.ConfigElementAdapter#getChildren()
    */
   @Override
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the languages config via the generic interfaces is not supported");
   }

   /**
    * @see org.springframework.extensions.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      LanguagesConfigElement newElement = (LanguagesConfigElement)configElement;
      LanguagesConfigElement combinedElement = new LanguagesConfigElement();

      // add the languages from this config element
      for (String locale : this.languages)
      {
         combinedElement.addLanguage(locale, this.localeMap.get(locale));
      }
      
      // now add the languages from the one to be combined (but 
      // only if they are not already in the list)
      List<String> languages = newElement.getLanguages();
      for (String locale : languages)
      {
         if (combinedElement.getLabelForLanguage(locale) == null)
         {
            String label = newElement.getLabelForLanguage(locale);
            combinedElement.addLanguage(locale, label);
         }
      }
      
      return combinedElement;
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
