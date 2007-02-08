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
      protected String errorMsgId = "error_dialog";
      protected boolean isOKButtonVisible = true;
      protected List<DialogButtonConfig> buttons;
      
      public DialogConfig(String name, String page, String bean,
                          String actionsConfigId, String icon, 
                          String title, String titleId,
                          String description, String descriptionId,
                          String errorMsgId, boolean isOKButtonVisible,
                          List<DialogButtonConfig> buttons)
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
         this.isOKButtonVisible = isOKButtonVisible;
         this.buttons = buttons;
         
         if (errorMsgId != null && errorMsgId.length() > 0)
         {
            this.errorMsgId = errorMsgId;
         }
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
      
      public String getErrorMessageId()
      {
         return this.errorMsgId;
      }
      
      public boolean isOKButtonVisible()
      {
         return this.isOKButtonVisible;
      }
      
      public List<DialogButtonConfig> getButtons()
      {
         return this.buttons;
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
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" errorMsgId=").append(this.errorMsgId);
         buffer.append(" isOKButtonVisible=").append(this.isOKButtonVisible);
         buffer.append(" buttons=").append(this.buttons).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Inner class representing the configuration for an additional
    * dialog button.
    * 
    * @author gavinc
    */
   public static class DialogButtonConfig
   {
      private String id;
      private String label;
      private String labelId;
      private String action;
      private String disabled;
      private String onclick;

      public DialogButtonConfig(String id, String label, String labelId, 
                                String action, String disabled, String onclick)
      {
         this.id = id;
         this.label = label;
         this.labelId = labelId;
         this.action = action;
         this.disabled = disabled;
         this.onclick = onclick;
         
         if ((this.label == null || this.label.length() == 0) && 
             (this.labelId == null || this.labelId.length() == 0))
         {
            throw new ConfigException("A dialog button needs to have a label or a label-id");
         }
         
         if (this.action == null || this.action.length() == 0)
         {
            throw new ConfigException("A dialog button requires an action");
         }
         else if (this.action.startsWith("#{") == false)
         {
            throw new ConfigException("The action for a dialog button must be a method binding expression, '" 
                  + this.action + "' is not!");
         }
      }

      public String getAction()
      {
         return action;
      }

      public String getDisabled()
      {
         return disabled;
      }

      public String getId()
      {
         return id;
      }

      public String getLabel()
      {
         return label;
      }

      public String getLabelId()
      {
         return labelId;
      }

      public String getOnclick()
      {
         return onclick;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (id=").append(this.id);
         buffer.append(" label=").append(this.label);
         buffer.append(" label-id=").append(this.labelId);
         buffer.append(" action=").append(this.action);
         buffer.append(" disabled=").append(this.disabled);
         buffer.append(" onclick=").append(this.onclick).append(")");
         return buffer.toString();
      }
   }
}
