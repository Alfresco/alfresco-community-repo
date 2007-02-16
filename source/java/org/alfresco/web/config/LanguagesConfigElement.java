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
