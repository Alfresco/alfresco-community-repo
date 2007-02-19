/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
