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
 * Custom element reader to parse config for the sidebar
 * 
 * @author gavinc
 */
public class SidebarElementReader implements ConfigElementReader
{
   public static final String ELEMENT_SIDEBAR = "sidebar";
   public static final String ELEMENT_PLUGINS = "plugins";
   public static final String ELEMENT_PLUGIN = "plugin";
   public static final String ELEMENT_DEFAULT_PLUGIN = "default-plugin";
   public static final String ATTR_ID = "id";
   public static final String ATTR_LABEL = "label";
   public static final String ATTR_LABEL_ID = "label-id";
   public static final String ATTR_DESCRIPTION = "description";
   public static final String ATTR_DESCRIPTION_ID = "description-id";
   public static final String ATTR_PAGE = "page";
   public static final String ATTR_ICON = "icon";
   public static final String ATTR_ACTIONS_CONFIG_ID = "actions-config-id";

   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      SidebarConfigElement configElement = null;
      
      if (element != null)
      {
         String elementName = element.getName();
         if (elementName.equals(ELEMENT_SIDEBAR) == false)
         {
            throw new ConfigException("SidebarElementReader can only parse " +
                  ELEMENT_SIDEBAR + "elements, the element passed was '" + 
                  elementName + "'");
         }
         
         configElement = new SidebarConfigElement();
         
         // go through the plugins that make up the sidebar
         Element pluginsElem = element.element(ELEMENT_PLUGINS);
         if (pluginsElem != null)
         {
            Iterator<Element> plugins = pluginsElem.elementIterator(ELEMENT_PLUGIN);
            while (plugins.hasNext())
            {
               Element plugin = plugins.next();
               
               String id = plugin.attributeValue(ATTR_ID);
               String page = plugin.attributeValue(ATTR_PAGE);
               String label = plugin.attributeValue(ATTR_LABEL);
               String labelId = plugin.attributeValue(ATTR_LABEL_ID);
               String description = plugin.attributeValue(ATTR_DESCRIPTION);
               String descriptionId = plugin.attributeValue(ATTR_DESCRIPTION_ID);
               String actionsConfigId = plugin.attributeValue(ATTR_ACTIONS_CONFIG_ID);
               String icon = plugin.attributeValue(ATTR_ICON);
               
               SidebarConfigElement.SidebarPluginConfig cfg = 
                     new SidebarConfigElement.SidebarPluginConfig(id, page, 
                           label, labelId, description, descriptionId, 
                           actionsConfigId, icon);
               
               configElement.addPlugin(cfg);
            }
         }
         
         // see if a default plugin is specified
         Element defaultPlugin = element.element(ELEMENT_DEFAULT_PLUGIN);
         if (defaultPlugin != null)
         {
            configElement.setDefaultPlugin(defaultPlugin.getTextTrim());
         }
      }
      
      return configElement;
   }
}
