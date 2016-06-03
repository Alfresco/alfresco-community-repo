package org.alfresco.web.config;

import java.util.Iterator;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;
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
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
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
