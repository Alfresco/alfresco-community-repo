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
    * Immutable inner class representing the configuration of a single dialog.
    * 
    * @author gavinc
    */
   public static class DialogConfig
   {
      protected DialogAttributes attributes;
      
      public DialogConfig(DialogAttributes attrs)
      {
         // check the attributes object has been supplied
         ParameterCheck.mandatory("attrs", attrs);
         
         this.attributes = attrs;
      }
      
      public String getName()
      {
         return this.attributes.getName();
      }
      
      public String getPage()
      {
         return this.attributes.getPage();
      }
      
      public String getManagedBean()
      {
         return this.attributes.getManagedBean();
      }
      
      public String getTitle()
      {
         return this.attributes.getTitle();
      }
      
      public String getTitleId()
      {
         return this.attributes.getTitleId();
      }
      
      public String getSubTitle()
      {
         return this.attributes.getSubTitle();
      }
      
      public String getSubTitleId()
      {
         return this.attributes.getSubTitleId();
      }
      
      public String getDescription()
      {
         return this.attributes.getDescription();
      }
      
      public String getDescriptionId()
      {
         return this.attributes.getDescriptionId();
      }
      
      public String getIcon()
      {
         return this.attributes.getIcon();
      }
      
      public String getErrorMessageId()
      {
         return this.attributes.getErrorMessageId();
      }
      
      public boolean isOKButtonVisible()
      {
         return this.attributes.isOKButtonVisible();
      }
      
      public List<DialogButtonConfig> getButtons()
      {
         return this.attributes.getButtons();
      }
      
      public String getActionsConfigId()
      {
         return this.attributes.getActionsConfigId();
      }
      
      public boolean getActionsAsMenu()
      {
         return this.attributes.getActionsAsMenu();
      }
      
      public String getActionsMenuLabel()
      {
         return this.attributes.getActionsMenuLabel();
      }
      
      public String getActionsMenuLabelId()
      {
         return this.attributes.getActionsMenuLabelId();
      }
      
      public String getMoreActionsConfigId()
      {
         return this.attributes.getMoreActionsConfigId();
      }
      
      public String getMoreActionsMenuLabel()
      {
         return this.attributes.getMoreActionsMenuLabel();
      }
      
      public String getMoreActionsMenuLabelId()
      {
         return this.attributes.getMoreActionsMenuLabelId();
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (").append(this.attributes.toString()).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Immutable inner class representing the configuration for an additional
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
   
   /**
    * Object holding all the dialog attributes collected from config.
    * 
    * @author gavinc
    */
   public static class DialogAttributes
   {
      protected String name;
      protected String page;
      protected String managedBean;
      protected String icon;
      protected String title;
      protected String titleId;
      protected String subTitle;
      protected String subTitleId;
      protected String description;
      protected String descriptionId;
      protected String errorMsgId = "error_dialog";
      protected boolean isOKButtonVisible = true;
      protected List<DialogButtonConfig> buttons;
      protected String actionsConfigId;
      protected boolean actionsAsMenu = false;
      protected String actionsMenuLabel;
      protected String actionsMenuLabelId;
      protected String moreActionsConfigId;
      protected String moreActionsMenuLabel;
      protected String moreActionsMenuLabelId;
      
      // ----------------------------------------------------
      // Construction
      
      public DialogAttributes(String name, String page, String bean)
      {
         // check the mandatory parameters are present
         ParameterCheck.mandatoryString("name", name);
         ParameterCheck.mandatoryString("page", page);
         ParameterCheck.mandatoryString("managedBean", bean);
         
         this.name = name;
         this.page = page;
         this.managedBean = bean;
      }
      
      // ----------------------------------------------------
      // Setters

      public void setIcon(String icon)
      {
         this.icon = icon;
      }

      public void setTitle(String title)
      {
         this.title = title;
      }

      public void setTitleId(String titleId)
      {
         this.titleId = titleId;
      }

      public void setSubTitle(String subTitle)
      {
         this.subTitle = subTitle;
      }

      public void setSubTitleId(String subTitleId)
      {
         this.subTitleId = subTitleId;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      public void setDescriptionId(String descriptionId)
      {
         this.descriptionId = descriptionId;
      }
      
      public void setErrorMessageId(String errorMsgId)
      {
         if (errorMsgId != null && errorMsgId.length() > 0)
         {
            this.errorMsgId = errorMsgId;
         }
      }

      public void setOKButtonVisible(boolean isOKButtonVisible)
      {
         this.isOKButtonVisible = isOKButtonVisible;
      }

      public void setButtons(List<DialogButtonConfig> buttons)
      {
         this.buttons = buttons;
      }

      public void setActionsConfigId(String actionsConfigId)
      {
         this.actionsConfigId = actionsConfigId;
      }

      public void setActionsAsMenu(boolean actionsAsMenu)
      {
         this.actionsAsMenu = actionsAsMenu;
      }

      public void setActionsMenuLabel(String actionsMenuLabel)
      {
         this.actionsMenuLabel = actionsMenuLabel;
      }

      public void setActionsMenuLabelId(String actionsMenuLabelId)
      {
         this.actionsMenuLabelId = actionsMenuLabelId;
      }

      public void setMoreActionsConfigId(String moreActionsConfigId)
      {
         this.moreActionsConfigId = moreActionsConfigId;
      }

      public void setMoreActionsMenuLabel(String moreActionsMenuLabel)
      {
         this.moreActionsMenuLabel = moreActionsMenuLabel;
      }

      public void setMoreActionsMenuLabelId(String moreActionsMenuLabelId)
      {
         this.moreActionsMenuLabelId = moreActionsMenuLabelId;
      }

      // ----------------------------------------------------
      // Getters

      public String getName()
      {
         return this.name;
      }
      
      public String getPage()
      {
         return this.page;
      }
      
      public String getManagedBean()
      {
         return this.managedBean;
      }
      
      public String getDescription()
      {
         return this.description;
      }
      
      public String getDescriptionId()
      {
         return this.descriptionId;
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
      
      public String getSubTitle()
      {
         return this.subTitle;
      }
      
      public String getSubTitleId()
      {
         return this.subTitleId;
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
      
      public String getActionsConfigId()
      {
         return this.actionsConfigId;
      }
      
      public boolean getActionsAsMenu()
      {
         return this.actionsAsMenu;
      }
      
      public String getActionsMenuLabel()
      {
         return this.actionsMenuLabel;
      }
      
      public String getActionsMenuLabelId()
      {
         return this.actionsMenuLabelId;
      }
      
      public String getMoreActionsConfigId()
      {
         return this.moreActionsConfigId;
      }
      
      public String getMoreActionsMenuLabel()
      {
         return this.moreActionsMenuLabel;
      }
      
      public String getMoreActionsMenuLabelId()
      {
         return this.moreActionsMenuLabelId;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder();
         buffer.append("name=").append(this.name);
         buffer.append(" page=").append(this.page);
         buffer.append(" managedBean=").append(this.managedBean);
         buffer.append(" icon=").append(this.icon);
         buffer.append(" title=").append(this.title);
         buffer.append(" titleId=").append(this.titleId);
         buffer.append(" subTitle=").append(this.subTitle);
         buffer.append(" subTitleId=").append(this.subTitleId);
         buffer.append(" description=").append(this.description);
         buffer.append(" descriptionId=").append(this.descriptionId);
         buffer.append(" errorMsgId=").append(this.errorMsgId);
         buffer.append(" isOKButtonVisible=").append(this.isOKButtonVisible);
         buffer.append(" actionsConfigId=").append(this.actionsConfigId);
         buffer.append(" actionsAsMenu=").append(this.actionsAsMenu);
         buffer.append(" actionsMenuLabel=").append(this.actionsMenuLabel);
         buffer.append(" actionsMenuLabelId=").append(this.actionsMenuLabelId);
         buffer.append(" moreActionsConfigId=").append(this.moreActionsConfigId);
         buffer.append(" moreActionsMenuLabel=").append(this.moreActionsMenuLabel);
         buffer.append(" moreActionsMenuLabelId=").append(this.moreActionsMenuLabelId);
         buffer.append(" buttons=").append(this.buttons);
         return buffer.toString();
      }
   }
}
