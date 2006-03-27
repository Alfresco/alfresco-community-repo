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
 * Custom element reader to parse config for dialogs
 * 
 * @author gavinc
 */
public class DialogsElementReader implements ConfigElementReader
{
   public static final String ELEMENT_DIALOGS = "dialogs";
   public static final String ELEMENT_DIALOG = "dialog";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_PAGE = "page";
   public static final String ATTR_MANAGED_BEAN = "managed-bean";
   public static final String ATTR_ICON = "icon";
   public static final String ATTR_TITLE = "title";
   public static final String ATTR_TITLE_ID = "title-id";
   public static final String ATTR_DESCRIPTION = "description";
   public static final String ATTR_DESCRIPTION_ID = "description-id";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      DialogsConfigElement configElement = null;
      
      if (element != null)
      {
         String elementName = element.getName();
         if (elementName.equals(ELEMENT_DIALOGS) == false)
         {
            throw new ConfigException("DialogsElementReader can only parse " +
                  ELEMENT_DIALOGS + "elements, the element passed was '" + 
                  elementName + "'");
         }
         
         configElement = new DialogsConfigElement();
         
         // go through the items to show
         Iterator<Element> items = element.elementIterator(ELEMENT_DIALOG);
         while (items.hasNext())
         {
            Element item = items.next();
            
            String name = item.attributeValue(ATTR_NAME);
            String page = item.attributeValue(ATTR_PAGE);
            String bean = item.attributeValue(ATTR_MANAGED_BEAN);
            String icon = item.attributeValue(ATTR_ICON);
            String title = item.attributeValue(ATTR_TITLE);
            String titleId = item.attributeValue(ATTR_TITLE_ID);
            String description = item.attributeValue(ATTR_DESCRIPTION);
            String descriptionId = item.attributeValue(ATTR_DESCRIPTION_ID);
            
            DialogsConfigElement.DialogConfig cfg = new DialogsConfigElement.DialogConfig(
                  name, page, bean, icon, title, titleId, description, descriptionId);
            
            configElement.addDialog(cfg);
         }
      }
      
      return configElement;
   }

}
