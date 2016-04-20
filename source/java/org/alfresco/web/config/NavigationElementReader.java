package org.alfresco.web.config;

import java.util.Iterator;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for navigation overrides
 * 
 * @author gavinc
 */
public class NavigationElementReader implements ConfigElementReader
{
   public static final String ELEMENT_NAVIGATION = "navigation";
   public static final String ELEMENT_OVERRIDE = "override";
   public static final String ATTR_FROM_VIEWID = "from-view-id";
   public static final String ATTR_FROM_OUTCOME = "from-outcome";
   public static final String ATTR_TO_VIEWID = "to-view-id";
   public static final String ATTR_TO_OUTCOME = "to-outcome";
   
   /**
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      NavigationConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (ELEMENT_NAVIGATION.equals(name) == false)
         {
            throw new ConfigException("NavigationElementReader can only parse " +
                  ELEMENT_NAVIGATION + "elements, " + "the element passed was '" + 
                  name + "'");
         }
         
         configElement = new NavigationConfigElement();
         
         // go through the items to show
         Iterator<Element> items = element.elementIterator();
         while (items.hasNext())
         {
            Element item = items.next();
            
            // only process the override elements
            if (ELEMENT_OVERRIDE.equals(item.getName()))
            {
               String fromViewId = item.attributeValue(ATTR_FROM_VIEWID);
               String fromOutcome = item.attributeValue(ATTR_FROM_OUTCOME);
               String toViewId = item.attributeValue(ATTR_TO_VIEWID);
               String toOutcome = item.attributeValue(ATTR_TO_OUTCOME);
               
               configElement.addOverride(fromViewId, fromOutcome, toViewId, toOutcome);
            }
         }
      }
      
      return configElement;
   }
}
