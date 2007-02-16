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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.common.tag.data;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;


/**
 * @author kevinr
 */
public class ColumnTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.RichListColumn";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is renderer by the parent
      return null;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.primary = null;
      this.actions = null;
      this.width = null;
      this.style = null;
      this.styleClass = null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setBooleanProperty(component, "primary", this.primary);
      setBooleanProperty(component, "actions", this.actions);
      setStringProperty(component, "width", this.width);
      setStringProperty(component, "style", this.style);
      setStringProperty(component, "styleClass", this.styleClass);
   }
   
   
   // ------------------------------------------------------------------------------
   // Tag properties

   /**
    * Set if this is the primary column
    *
    * @param primary     the primary if "true", otherwise false
    */
   public void setPrimary(String primary)
   {
      this.primary = primary;
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
    * Set the style
    *
    * @param style     the style
    */
   public void setStyle(String style)
   {
      this.style = style;
   }

   /**
    * Set the styleClass
    *
    * @param styleClass     the styleClass
    */
   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }
   
   /**
    * Set if this is the actions column
    *
    * @param actions     the actions if "true", otherwise false
    */
   public void setActions(String actions)
   {
      this.actions = actions;
   }


   /** the actions */
   private String actions;
   
   /** the style */
   private String style;

   /** the styleClass */
   private String styleClass;

   /** the width */
   private String width;

   /** the primary */
   private String primary;
}
