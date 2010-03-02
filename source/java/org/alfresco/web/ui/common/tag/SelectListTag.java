/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

/**
 * @author Kevin Roast
 */
public class SelectListTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.SelectList";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setBooleanProperty(component, "multiSelect", this.multiSelect);
      setBooleanProperty(component, "activeSelect", this.activeSelect);
      setStringStaticProperty(component, "var", this.var);
      setStringProperty(component, "itemStyle", this.itemStyle);
      setStringProperty(component, "itemStyleClass", this.itemStyleClass);
      setStringProperty(component, "value", this.value);
      setStringProperty(component, "onchange", this.onchange);
      setBooleanProperty(component, "escapeItemLabel", this.escapeItemLabel);
      setBooleanProperty(component, "escapeItemDescription", this.escapeItemDescription);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.multiSelect = null;
      this.activeSelect = null;
      this.var = null;
      this.itemStyle = null;
      this.itemStyleClass = null;
      this.value = null;
      this.onchange = null;
      this.escapeItemLabel = null;
      this.escapeItemDescription = null;
   }

   /**
    * Set the multi-select mode 
    *
    * @param multiSelect     the multi-select mode 
    */
   public void setMultiSelect(String multiSelect)
   {
      this.multiSelect = multiSelect;
   }
   
   /**
    * Set the active selection mode
    *
    * @param activeSelect     the active selection mode
    */
   public void setActiveSelect(String activeSelect)
   {
      this.activeSelect = activeSelect;
   }
   
   /**
    * Set the variable name for row item context
    *
    * @param var     the variable name for row item context
    */
   public void setVar(String var)
   {
      this.var = var;
   }
   
   /**
    * Set the item Style
    *
    * @param itemStyle     the item Style
    */
   public void setItemStyle(String itemStyle)
   {
      this.itemStyle = itemStyle;
   }

   /**
    * Set the item Style Class
    *
    * @param itemStyleClass     the item Style Class
    */
   public void setItemStyleClass(String itemStyleClass)
   {
      this.itemStyleClass = itemStyleClass;
   }
   
   /**
    * Set the selected value
    *
    * @param value     the selected value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Set the onchange handler
    *
    * @param value the onchange handler.
    */
   public void setOnchange(final String onchange)
   {
      this.onchange = onchange;
   }
   
   /**
    * Set the escapeItemLabel flag
    *
    * @param escapeItemLabel true to escape the items labels
    */
   public void setEscapeItemLabel(String escapeItemLabel)
   {
      this.escapeItemLabel = escapeItemLabel;
   }
   
   /**
    * Set the escapeItemDescription flag
    *
    * @param escapeItemDescription true to escape the items descriptions
    */
   public void setEscapeItemDescription(String escapeItemDescription)
   {
      this.escapeItemDescription = escapeItemDescription;
   }

   /** the selected value */
   private String value;

   /** the itemStyle */
   private String itemStyle;

   /** the itemStyleClass */
   private String itemStyleClass;

   /** the multi-select mode */
   private String multiSelect;

   /** the active selection mode */
   private String activeSelect;
   
   /** the variable name for row item context */
   private String var;

   /** the event handler for a change in selection */
   private String onchange;
   
   /** the escape mode for item's labels */
   private String escapeItemLabel;
   
   /** the escape mode for item's descriptions */
   private String escapeItemDescription;
}
