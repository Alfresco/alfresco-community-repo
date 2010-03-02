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

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.xml.elementreader.GenericElementReader;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for WCM
 * 
 * @author gavinc
 */
public class WCMElementReader extends GenericElementReader
{
   public static final String ELEMENT_WCM = "wcm";
   
   /**
    * Default constructor
    */
   public WCMElementReader()
   {
      super(null);
   }
   
   /**
    * @see org.springframework.extensions.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      WCMConfigElement wcmConfigElement = null;
      
      if (element != null)
      {
         // create the config element object
         wcmConfigElement = new WCMConfigElement(ELEMENT_WCM);
         
         // we know there are no attributes or values for the root element
         // so just process the children
         processChildren(element, wcmConfigElement);
      }
      
      return wcmConfigElement;
   }
}
