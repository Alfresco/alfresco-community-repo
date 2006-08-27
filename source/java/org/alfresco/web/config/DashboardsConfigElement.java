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
      
      return combinedElement;
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
   
   /**
    * Structure class for the definition of a dashboard page layout 
    */
   public static class LayoutDefinition
   {
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
   public static class DashletDefinition
   {
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
