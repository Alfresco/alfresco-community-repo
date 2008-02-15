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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Dashboard config element.
 * 
 * @author Kevin Roast
 */
public class DashboardsConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "dashboards";
   
   private Map<String, LayoutDefinition> layoutDefs = new LinkedHashMap<String, LayoutDefinition>(4, 1.0f);
   private Map<String, DashletDefinition> dashletDefs = new LinkedHashMap<String, DashletDefinition>(8, 1.0f);
   private List<String> defaultDashlets = null;
   private boolean allowGuestConfig = false;
   
   /**
    * Default constructor
    */
   public DashboardsConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * @param name
    */
   public DashboardsConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the Dashboards config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      DashboardsConfigElement newElement = (DashboardsConfigElement)configElement;
      DashboardsConfigElement combinedElement = new DashboardsConfigElement();
      
      // put all into combined from this and then from new to override any already present
      combinedElement.dashletDefs.putAll(this.dashletDefs);
      combinedElement.dashletDefs.putAll(newElement.dashletDefs);
      
      combinedElement.layoutDefs.putAll(this.layoutDefs);
      combinedElement.layoutDefs.putAll(newElement.layoutDefs);
      
      if (newElement.allowGuestConfig != combinedElement.allowGuestConfig)
      {
         combinedElement.allowGuestConfig = newElement.allowGuestConfig;
      }
      
      // the default-dashlets list is completely replaced if config is overriden
      if (newElement.defaultDashlets != null)
      {
         combinedElement.defaultDashlets =
            (List<String>)((ArrayList<String>)newElement.defaultDashlets).clone();
      }
      else if (this.defaultDashlets != null)
      {
         combinedElement.defaultDashlets =
            (List<String>)((ArrayList<String>)this.defaultDashlets).clone();
      }
      
      return combinedElement;
   }
   
   /*package*/ void setAllowGuestConfig(boolean allow)
   {
      this.allowGuestConfig = allow;
   }
   
   public boolean getAllowGuestConfig()
   {
      return this.allowGuestConfig;
   }
   
   /*package*/ void addLayoutDefinition(LayoutDefinition def)
   {
      this.layoutDefs.put(def.Id, def);
   }
   
   public LayoutDefinition getLayoutDefinition(String id)
   {
      return this.layoutDefs.get(id);
   }
   
   /*package*/ void addDashletDefinition(DashletDefinition def)
   {
      this.dashletDefs.put(def.Id, def);
   }
   
   public DashletDefinition getDashletDefinition(String id)
   {
      return this.dashletDefs.get(id);
   }
   
   public Collection<LayoutDefinition> getLayouts()
   {
      return this.layoutDefs.values();
   }
   
   public Collection<DashletDefinition> getDashlets()
   {
      return this.dashletDefs.values();
   }
   
   /*package*/ void addDefaultDashlet(String id)
   {
      if (this.defaultDashlets == null)
      {
         this.defaultDashlets = new ArrayList<String>(2);
      }
      this.defaultDashlets.add(id);
   }
   
   public Collection<String> getDefaultDashlets()
   {
      return this.defaultDashlets;
   }
   
   /**
    * Structure class for the definition of a dashboard page layout 
    */
   public static class LayoutDefinition implements Serializable
   {
      private static final long serialVersionUID = -3014156293576142077L;
    
      LayoutDefinition(String id)
      {
         this.Id = id;
      }
      
      public String Id;
      public String Image;
      public int Columns;
      public int ColumnLength;
      public String Label;
      public String LabelId;
      public String Description;
      public String DescriptionId;
      public String JSPPage;
   }
   
   /**
    * Structure class for the definition of a dashboard dashlet component
    */
   public static class DashletDefinition implements Serializable
   {
      private static final long serialVersionUID = -5755903997700459631L;
      
      DashletDefinition(String id)
      {
         this.Id = id;
      }
      
      public String Id;
      public boolean AllowNarrow = true;
      public String Label;
      public String LabelId;
      public String Description;
      public String DescriptionId;
      public String JSPPage;
      public String ConfigJSPPage;
   }
}
