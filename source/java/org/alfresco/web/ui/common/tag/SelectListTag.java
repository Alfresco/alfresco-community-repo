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
}
