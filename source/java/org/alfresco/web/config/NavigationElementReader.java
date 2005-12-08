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

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
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
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
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
