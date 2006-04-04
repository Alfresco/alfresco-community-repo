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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.util.ParameterCheck;

/**
 * Custom config element that represents the config data for a property sheet
 * 
 * @author gavinc
 */
public class DialogsConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "dialogs";
   
   private Map<String, DialogConfig> dialogs = new LinkedHashMap<String, DialogConfig>(8, 10f);
   
   /**
    * Default constructor
    */
   public DialogsConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public DialogsConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the dialogs config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      DialogsConfigElement combined = new DialogsConfigElement();
      
      // add all the dialogs from this element
      for (DialogConfig dialog : this.getDialogs().values())
      {
         combined.addDialog(dialog);
      }
      
      // add all the dialogs from the given element
      for (DialogConfig dialog : ((DialogsConfigElement)configElement).getDialogs().values())
      {
         combined.addDialog(dialog);
      }
      
      return combined;
   }
   
   /**
    * Returns the named dialog
    * 
    * @param name The name of the dialog to retrieve
    * @return The DialogConfig object for the requested dialog or null if it doesn't exist
    */
   public DialogConfig getDialog(String name)
   {
      return this.dialogs.get(name);
   }
   
   /**
    * @return Returns a map of the dialogs. A linked hash map is used internally to
    *         preserve ordering.
    */
   public Map<String, DialogConfig> getDialogs()
   {
      return this.dialogs;
   }
   
   /**
    * Adds a dialog
    * 
    * @param dialogConfig A pre-configured dialog config object
    */
   /*package*/ void addDialog(DialogConfig dialogConfig)
   {
      this.dialogs.put(dialogConfig.getName(), dialogConfig);
   }
   
   /**
    * Inner class representing the configuration of a single dialog
    * 
    * @author gavinc
    */
   public static class DialogConfig
   {
      protected String name;
      protected String page;
      protected String managedBean;
      protected String actionsConfigId;
      protected String icon;
      protected String title;
      protected String titleId;
      protected String description;
      protected String descriptionId;
      
      public DialogConfig(String name, String page, String bean,
                          String actionsConfigId, String icon, 
                          String title, String titleId,
                          String description, String descriptionId)
      {
         // check the mandatory parameters are present
         ParameterCheck.mandatoryString("name", name);
         ParameterCheck.mandatoryString("page", page);
         ParameterCheck.mandatoryString("managedBean", bean);
         
         this.name = name;
         this.page = page;
         this.managedBean = bean;
         this.actionsConfigId = actionsConfigId;
         this.icon = icon;
         this.title = title;
         this.titleId = titleId;
         this.description = description;
         this.descriptionId = descriptionId;
      }
      
      public String getDescription()
      {
         return this.description;
      }
      
      public String getDescriptionId()
      {
         return this.descriptionId;
      }
      
      public String getManagedBean()
      {
         return this.managedBean;
      }
      
      public String getActionsConfigId()
      {
         return this.actionsConfigId;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public String getPage()
      {
         return this.page;
      }
      
      public String getIcon()
      {
         return this.icon;
      }
      
      public String getTitle()
      {
         return this.title;
      }
      
      public String getTitleId()
      {
         return this.titleId;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" page=").append(this.page);
         buffer.append(" managed-bean=").append(this.managedBean);
         buffer.append(" actions-config-id=").append(this.actionsConfigId);
         buffer.append(" icon=").append(this.icon);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId).append(")");
         return buffer.toString();
      }
   }
}
