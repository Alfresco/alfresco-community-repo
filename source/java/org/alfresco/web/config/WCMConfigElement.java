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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.element.GenericConfigElement;

/**
 * Custom config element that represents the config data for WCM
 * 
 * @author gavinc
 */
public class WCMConfigElement extends GenericConfigElement
{
   private static final long serialVersionUID = -4906603037550877971L;
   
   protected Map<String, GenericConfigElement> childrenMap;
   
   public static final String CONFIG_ELEMENT_ID = "wcm";
   
   /**
    * Default constructor
    */
   public WCMConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public WCMConfigElement(String name)
   {
      super(name);
      
      this.childrenMap = new HashMap<String, GenericConfigElement>(8);
   }

   @Override
   public ConfigElement combine(ConfigElement configElement)
   {
      WCMConfigElement combined = new WCMConfigElement(this.name);
      WCMConfigElement toCombineElement = (WCMConfigElement)configElement;
      
      // work out which child element to add
      Map<String, GenericConfigElement> toCombineKids = toCombineElement.getChildrenAsMap();
      List<ConfigElement> kids = this.getChildren();
      if (kids != null)
      {
          for (ConfigElement child : kids)
          {
             String childName = child.getName();
             if (toCombineKids.containsKey(childName))
             {
                // check for the 'xforms' child element
                if (childName.equals("xforms"))
                {
                   // add the widgets from the 'to combine' element to
                   // this one and then add to the new combined element
                   for (ConfigElement widget : toCombineKids.get("xforms").getChildren())
                   {
                      ((GenericConfigElement)child).addChild(widget);
                   }
                   
                   // add the current child to the combined one
                   combined.addChild(child);
                }
                else
                {
                   // use the overridden child element
                   combined.addChild(toCombineKids.get(childName));
                }
             }
             else
             {
                // the current child has not be overridden so
                // just add the current child
                combined.addChild(child);
             }
          }
      }
      
      // make sure any children only present in the 'to combine' element
      // are added
      kids = toCombineElement.getChildren();
      if (kids != null)
      {
         Map<String, GenericConfigElement> combinedKids = combined.getChildrenAsMap();
         for (ConfigElement child : kids)
         {
            if (!combinedKids.containsKey(child.getName()))
            {
               combined.addChild(child);
            }
         }
      }
      
      
      return combined;
   }

   @Override
   public void addChild(ConfigElement configElement)
   {
      super.addChild(configElement);
      
      // also add the child element to our map
      this.childrenMap.put(configElement.getName(), (GenericConfigElement)configElement);
   }
   
   /**
    * Returns the children in a Map
    * 
    * @return Child elements as a Map
    */
   public Map<String, GenericConfigElement> getChildrenAsMap()
   {
      return this.childrenMap;
   }
}
