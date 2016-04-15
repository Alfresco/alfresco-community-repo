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
