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
import java.util.Iterator;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for advanced search
 * 
 * @author Gavin Cornwell
 */
public class AdvancedSearchElementReader implements ConfigElementReader
{
   public static final String ELEMENT_CONTENTTYPES = "content-types";
   public static final String ELEMENT_FOLDERTYPES = "folder-types";
   public static final String ELEMENT_TYPE = "type";
   public static final String ELEMENT_CUSTOMPROPS = "custom-properties";
   public static final String ELEMENT_METADATA = "meta-data";
   public static final String ATTRIBUTE_NAME = "name";
   public static final String ATTRIBUTE_TYPE = "type";
   public static final String ATTRIBUTE_PROPERTY = "property";
   public static final String ATTRIBUTE_ASPECT = "aspect";
   public static final String ATTRIBUTE_DISPLAYLABEL = "display-label-id";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      AdvancedSearchConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(AdvancedSearchConfigElement.CONFIG_ELEMENT_ID) == false)
         {
            throw new ConfigException("AdvancedSearchElementReader can only parse " +
                  AdvancedSearchConfigElement.CONFIG_ELEMENT_ID + " elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new AdvancedSearchConfigElement();
         
         // get the list of content types
         Element contentTypes = element.element(ELEMENT_CONTENTTYPES);
         if (contentTypes != null)
         {
            Iterator<Element> typesItr = contentTypes.elementIterator(ELEMENT_TYPE);
            List<String> types = new ArrayList<String>(5);
            while (typesItr.hasNext())
            {
               Element contentType = typesItr.next();
               String type = contentType.attributeValue(ATTRIBUTE_NAME);
               if (type != null)
               {
                  types.add(type);
               }
            }
            configElement.setContentTypes(types);
         }
         
         // get the list of folder types
         Element folderTypes = element.element(ELEMENT_FOLDERTYPES);
         if (folderTypes != null)
         {
            Iterator<Element> typesItr = folderTypes.elementIterator(ELEMENT_TYPE);
            List<String> types = new ArrayList<String>(5);
            while (typesItr.hasNext())
            {
               Element folderType = typesItr.next();
               String type = folderType.attributeValue(ATTRIBUTE_NAME);
               if (type != null)
               {
                  types.add(type);
               }
            }
            configElement.setFolderTypes(types);
         }
         
         // get the list of custom properties to display
         Element customProps = element.element(ELEMENT_CUSTOMPROPS);
         if (customProps != null)
         {
            Iterator<Element> propsItr = customProps.elementIterator(ELEMENT_METADATA);
            List<CustomProperty> props = new ArrayList<CustomProperty>(5);
            while (propsItr.hasNext())
            {
               Element propElement = propsItr.next();
               String type = propElement.attributeValue(ATTRIBUTE_TYPE);
               String aspect = propElement.attributeValue(ATTRIBUTE_ASPECT);
               String prop = propElement.attributeValue(ATTRIBUTE_PROPERTY);
               String labelId = propElement.attributeValue(ATTRIBUTE_DISPLAYLABEL);
               props.add(new AdvancedSearchConfigElement.CustomProperty(type, aspect, prop, labelId));
            }
            configElement.setCustomProperties(props);
         }
      }
      
      return configElement;
   }
}
