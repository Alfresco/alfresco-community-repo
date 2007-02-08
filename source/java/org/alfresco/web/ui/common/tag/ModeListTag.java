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
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author kevinr
 */
public class ModeListTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ModeList";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.ModeListRenderer";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setStringProperty(component, "labelStyle", this.labelStyle);
      setStringProperty(component, "labelStyleClass", this.labelStyleClass);
      setStringProperty(component, "itemStyle", this.itemStyle);
      setStringProperty(component, "itemStyleClass", this.itemStyleClass);
      setStringProperty(component, "disabledStyle", this.disabledStyle);
      setStringProperty(component, "disabledStyleClass", this.disabledStyleClass);
      setStringProperty(component, "itemLinkStyle", this.itemLinkStyle);
      setStringProperty(component, "itemLinkStyleClass", this.itemLinkStyleClass);
      setStringProperty(component, "selectedStyle", this.selectedStyle);
      setStringProperty(component, "selectedStyleClass", this.selectedStyleClass);
      setStringProperty(component, "selectedLinkStyle", this.selectedLinkStyle);
      setStringProperty(component, "selectedLinkStyleClass", this.selectedLinkStyleClass);
      setStringProperty(component, "selectedImage", this.selectedImage);
      setIntProperty(component, "itemSpacing", this.itemSpacing);
      setIntProperty(component, "iconColumnWidth", this.iconColumnWidth);
      setIntProperty(component, "width", this.width);
      setStringProperty(component, "menuImage", this.menuImage);
      setBooleanProperty(component, "menu", this.menu);
      setBooleanProperty(component, "horizontal", this.horizontal);
      setBooleanProperty(component, "disabled", this.disabled);
      setStringProperty(component, "label", this.label);
      setStringProperty(component, "value", this.value);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.labelStyle = null;
      this.labelStyleClass = null;
      this.itemStyle = null;
      this.itemStyleClass = null;
      this.itemLinkStyle = null;
      this.itemLinkStyleClass = null;
      this.disabledStyle = null;
      this.disabledStyleClass = null;
      this.selectedStyle = null;
      this.selectedStyleClass = null;
      this.selectedLinkStyle = null;
      this.selectedLinkStyleClass = null;
      this.selectedImage = null;
      this.itemSpacing = null;
      this.iconColumnWidth = null;
      this.horizontal = null;
      this.width = null;
      this.label = null;
      this.action = null;
      this.actionListener = null;
      this.value = null;
      this.disabled = null;
   }

   /**
    * Set the itemSpacing
    *
    * @param itemSpacing     the itemSpacing
    */
   public void setItemSpacing(String itemSpacing)
   {
      this.itemSpacing = itemSpacing;
   }

   /**
    * Set the iconColumnWidth
    *
    * @param iconColumnWidth     the iconColumnWidth
    */
   public void setIconColumnWidth(String iconColumnWidth)
   {
      this.iconColumnWidth = iconColumnWidth;
   }

   /**
    * Set the label
    *
    * @param label     the label
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Set the action
    *
    * @param action     the action
    */
   public void setAction(String action)
   {
      this.action = action;
   }

   /**
    * Set the actionListener
    *
    * @param actionListener     the actionListener
    */
   public void setActionListener(String actionListener)
   {
      this.actionListener = actionListener;
   }

   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the width
    *
    * @param width     the width
    */
   public void setWidth(String width)
   {
      this.width = width;
   }
   
   /**
    * Set if this component is rendered horizontally
    *
    * @param horizontal     true if rendered horizontally, false to render vertically
    */
   public void setHorizontal(String horizontal)
   {
      this.horizontal = horizontal;
   }
   
   /**
    * Set the labelStyle
    *
    * @param labelStyle     the labelStyle
    */
   public void setLabelStyle(String labelStyle)
   {
      this.labelStyle = labelStyle;
   }

   /**
    * Set the labelStyleClass
    *
    * @param labelStyleClass     the labelStyleClass
    */
   public void setLabelStyleClass(String labelStyleClass)
   {
      this.labelStyleClass = labelStyleClass;
   }

   /**
    * Set the itemStyle
    *
    * @param itemStyle     the itemStyle
    */
   public void setItemStyle(String itemStyle)
   {
      this.itemStyle = itemStyle;
   }

   /**
    * Set the itemStyleClass
    *
    * @param itemStyleClass     the itemStyleClass
    */
   public void setItemStyleClass(String itemStyleClass)
   {
      this.itemStyleClass = itemStyleClass;
   }

   /**
    * Set the itemLinkStyle
    *
    * @param itemLinkStyle     the itemLinkStyle
    */
   public void setItemLinkStyle(String itemLinkStyle)
   {
      this.itemLinkStyle = itemLinkStyle;
   }

   /**
    * Set the itemLinkStyleClass
    *
    * @param itemLinkStyleClass     the itemLinkStyleClass
    */
   public void setItemLinkStyleClass(String itemLinkStyleClass)
   {
      this.itemLinkStyleClass = itemLinkStyleClass;
   }

   /**
    * Set the selectedStyle
    *
    * @param selectedStyle     the selectedStyle
    */
   public void setSelectedStyle(String selectedStyle)
   {
      this.selectedStyle = selectedStyle;
   }

   /**
    * Set the selectedStyleClass
    *
    * @param selectedStyleClass     the selectedStyleClass
    */
   public void setSelectedStyleClass(String selectedStyleClass)
   {
      this.selectedStyleClass = selectedStyleClass;
   }

   /**
    * Set the selectedLinkStyle
    *
    * @param selectedLinkStyle     the selectedLinkStyle
    */
   public void setSelectedLinkStyle(String selectedLinkStyle)
   {
      this.selectedLinkStyle = selectedLinkStyle;
   }

   /**
    * Set the selectedLinkStyleClass
    *
    * @param selectedLinkStyleClass     the selectedLinkStyleClass
    */
   public void setSelectedLinkStyleClass(String selectedLinkStyleClass)
   {
      this.selectedLinkStyleClass = selectedLinkStyleClass;
   }
   
   /**
    * Set the image to show instead of the ListItem icon when the item is selected
    * 
    * @param selectedImage     the selected image
    */
   public void setSelectedImage(String selectedImage)
   {
      this.selectedImage = selectedImage;
   }

   /**
    * Set the disabled flag
    * 
    * @param disabled true to disable all children
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }
   
   /**
    * Set the disabledStyle
    *
    * @param disabledStyle     the disabledStyle
    */
   public void setDisabledStyle(String disabledStyle)
   {
      this.disabledStyle = disabledStyle;
   }

   /**
    * Set the disabledStyleClass
    *
    * @param disabledStyleClass     the disabledStyleClass
    */
   public void setDisabledStyleClass(String disabledStyleClass)
   {
      this.disabledStyleClass = disabledStyleClass;
   }
   
   /**
    * Set the menu
    *
    * @param menu     the menu
    */
   public void setMenu(String menu)
   {
      this.menu = menu;
   }

   /**
    * Set the menuImage
    *
    * @param menuImage     the menuImage
    */
   public void setMenuImage(String menuImage)
   {
      this.menuImage = menuImage;
   }


   /** the menu */
   private String menu;

   /** the menuImage */
   private String menuImage;

   /** the disabledStyle */
   private String disabledStyle;

   /** the disabledStyleClass */
   private String disabledStyleClass;

   /** the selectedImage */
   private String selectedImage;

   /** the labelStyle */
   private String labelStyle;

   /** the labelStyleClass */
   private String labelStyleClass;

   /** the itemStyle */
   private String itemStyle;

   /** the itemStyleClass */
   private String itemStyleClass;

   /** the itemLinkStyle */
   private String itemLinkStyle;

   /** the itemLinkStyleClass */
   private String itemLinkStyleClass;

   /** the selectedStyle */
   private String selectedStyle;

   /** the selectedStyleClass */
   private String selectedStyleClass;

   /** the selectedLinkStyle */
   private String selectedLinkStyle;

   /** the selectedLinkStyleClass */
   private String selectedLinkStyleClass;

   /** true if rendered horizontally, false to render vertically */
   private String horizontal;

   /** the width */
   private String width;

   /** the itemSpacing */
   private String itemSpacing;

   /** the iconColumnWidth */
   private String iconColumnWidth;

   /** the label */
   private String label;

   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;

   /** the value */
   private String value;
   
   /** the disabled flag */
   private String disabled;
}
