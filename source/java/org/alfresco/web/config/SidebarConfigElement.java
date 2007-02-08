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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.util.ParameterCheck;

/**
 * Custom config element that represents the config data for the sidebar
 * 
 * @author gavinc
 */
public class SidebarConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "sidebar";
   
   private String defaultPlugin;
   private Map<String, SidebarPluginConfig> plugins = new LinkedHashMap<String, SidebarPluginConfig>(8, 10f);
   
   /**
    * Default constructor
    */
   public SidebarConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public SidebarConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the sidebar config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      SidebarConfigElement newElement = (SidebarConfigElement)configElement;
      SidebarConfigElement combinedElement = new SidebarConfigElement();
      
      // add all the plugins from this element
      for (SidebarPluginConfig plugin : this.getPlugins().values())
      {
         combinedElement.addPlugin(plugin);
      }
      
      // add all the plugins from the given element
      for (SidebarPluginConfig plugin : newElement.getPlugins().values())
      {
         combinedElement.addPlugin(plugin);
      }
      
      // work out the default plugin
      String newDefaultPlugin = newElement.getDefaultPlugin();
      if (newDefaultPlugin != null)
      {
         combinedElement.setDefaultPlugin(newDefaultPlugin);
      }
      else
      {
         combinedElement.setDefaultPlugin(this.getDefaultPlugin());
      }
      
      return combinedElement;
   }
   
   /**
    * Returns the named plugin
    * 
    * @param id The id of the plugin to retrieve
    * @return The SidebarPluginConfig object for the requested plugin or null if it doesn't exist
    */
   public SidebarPluginConfig getPlugin(String id)
   {
      return this.plugins.get(id);
   }
   
   /**
    * @return Returns a map of the plugins. A linked hash map is used internally to
    *         preserve ordering.
    */
   public Map<String, SidebarPluginConfig> getPlugins()
   {
      return this.plugins;
   }
   
   /**
    * @return The id of the default plugin, null if there isn't a default defined
    */
   public String getDefaultPlugin()
   {
      return this.defaultPlugin;
   }
   
   /**
    * Sets the plugin to use as the default
    * 
    * @param defaultPlugin Id of the default plugin
    */
   public void setDefaultPlugin(String defaultPlugin)
   {
      this.defaultPlugin = defaultPlugin;
   }
   
   /**
    * Adds a plugin
    * 
    * @param pluginConfig A pre-configured plugin config object
    */
   /*package*/ void addPlugin(SidebarPluginConfig pluginConfig)
   {
      this.plugins.put(pluginConfig.getId(), pluginConfig);
   }
   
   /**
    * Inner class representing the configuration of a sidebar plugin
    * 
    * @author gavinc
    */
   public static class SidebarPluginConfig
   {
      protected String id;
      protected String page;
      protected String actionsConfigId;
      protected String icon;
      protected String label;
      protected String labelId;
      protected String description;
      protected String descriptionId;
      
      public SidebarPluginConfig(String id, String page, 
            String label, String labelId,
            String description, String descriptionId,
            String actionsConfigId, String icon)
      {
         // check the mandatory parameters are present
         ParameterCheck.mandatoryString("id", id);
         ParameterCheck.mandatoryString("page", page);
         
         this.id = id;
         this.page = page;
         this.icon = icon;
         this.label = label;
         this.labelId = labelId;
         this.description = description;
         this.descriptionId = descriptionId;
         this.actionsConfigId = actionsConfigId;
      }
      
      public String getId()
      {
         return this.id;
      }
      
      public String getPage()
      {
         return this.page;
      }
      
      public String getIcon()
      {
         return this.icon;
      }
      
      public String getlabel()
      {
         return this.label;
      }
      
      public String getlabelId()
      {
         return this.labelId;
      }
      
      public String getDescription()
      {
         return this.description;
      }
      
      public String getDescriptionId()
      {
         return this.descriptionId;
      }
      
      public String getActionsConfigId()
      {
         return this.actionsConfigId;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (id=").append(this.id);
         buffer.append(" page=").append(this.page);
         buffer.append(" icon=").append(this.icon);
         buffer.append(" label=").append(this.label);
         buffer.append(" labelId=").append(this.labelId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" actions-config-id=").append(this.actionsConfigId).append(")");
         return buffer.toString();
      }
   }
}
