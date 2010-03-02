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
 * Custom element reader to parse config for property sheets
 * 
 * @author gavinc
 */
public class PropertySheetElementReader implements ConfigElementReader
{
   public static final String ELEMENT_PROPERTY_SHEET = "property-sheet";
   public static final String ELEMENT_SHOW_PROPERTY = "show-property";
   public static final String ELEMENT_SHOW_ASSOC = "show-association";
   public static final String ELEMENT_SHOW_CHILD_ASSOC = "show-child-association";
   public static final String ELEMENT_SEPARATOR = "separator";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_DISPLAY_LABEL = "display-label";
   public static final String ATTR_DISPLAY_LABEL_ID = "display-label-id";
   public static final String ATTR_READ_ONLY = "read-only";
   public static final String ATTR_CONVERTER = "converter";
   public static final String ATTR_SHOW_IN_EDIT_MODE = "show-in-edit-mode";
   public static final String ATTR_SHOW_IN_VIEW_MODE = "show-in-view-mode";
   public static final String ATTR_COMPONENT_GENERATOR = "component-generator";
   public static final String ATTR_IGNORE_IF_MISSING = "ignore-if-missing";
   
   /**
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      PropertySheetConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ELEMENT_PROPERTY_SHEET) == false)
         {
            throw new ConfigException("PropertySheetElementReader can only parse " +
                  ELEMENT_PROPERTY_SHEET + "elements, " + "the element passed was '" + 
                  name + "'");
         }
         
         configElement = new PropertySheetConfigElement();
         
         // go through the items to show
         Iterator<Element> items = element.elementIterator();
         while (items.hasNext())
         {
            Element item = items.next();
            String propName = item.attributeValue(ATTR_NAME);
            String label = item.attributeValue(ATTR_DISPLAY_LABEL);
            String labelId = item.attributeValue(ATTR_DISPLAY_LABEL_ID);
            String readOnly = item.attributeValue(ATTR_READ_ONLY);
            String converter = item.attributeValue(ATTR_CONVERTER);
            String inEdit = item.attributeValue(ATTR_SHOW_IN_EDIT_MODE);
            String inView = item.attributeValue(ATTR_SHOW_IN_VIEW_MODE);
            String compGenerator = item.attributeValue(ATTR_COMPONENT_GENERATOR);
            
            if (ELEMENT_SHOW_PROPERTY.equals(item.getName()))
            {
               String ignoreIfMissing = item.attributeValue(ATTR_IGNORE_IF_MISSING);
               
               // add the property to show to the custom config element
               configElement.addProperty(propName, label, labelId, readOnly, converter, 
                     inView, inEdit, compGenerator, ignoreIfMissing);
            }
            else if (ELEMENT_SHOW_ASSOC.equals(item.getName()))
            {
               configElement.addAssociation(propName, label, labelId, readOnly, converter, 
                     inView, inEdit, compGenerator);
            }
            else if (ELEMENT_SHOW_CHILD_ASSOC.equals(item.getName()))
            {
               configElement.addChildAssociation(propName, label, labelId, readOnly, converter, 
                     inView, inEdit, compGenerator);
            }
            else if (ELEMENT_SEPARATOR.equals(item.getName()))
            {
               configElement.addSeparator(propName, label, labelId, inView, inEdit, compGenerator);
            }
         }
      }
      
      return configElement;
   }

}
