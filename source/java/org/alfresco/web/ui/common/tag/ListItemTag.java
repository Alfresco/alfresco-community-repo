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
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

/**
 * @author kevinr
 */
public class ListItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ListItem";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // this component is rendered by its parent container
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "tooltip", this.tooltip);
      setStringProperty(component, "label", this.label);
      setStringProperty(component, "description", this.description);
      setStringProperty(component, "image", this.image);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.tooltip = null;
      this.label = null;
      this.description = null;
      this.image = null;
      this.value = null;
      this.disabled = null;
   }
   
   /**
    * Set the tooltip
    *
    * @param tooltip     the tooltip
    */
   public void setTooltip(String tooltip)
   {
      this.tooltip = tooltip;
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
    * Set the description
    *
    * @param description     the description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Set the image
    *
    * @param image     the image
    */
   public void setImage(String image)
   {
      this.image = image;
   }

   /**
    * Set the value to be selected initially 
    *
    * @param value     the value to be selected initially
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the disabled flag
    * 
    * @param disabled true to set this item as disabled
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }

   /** the tooltip */
   private String tooltip;

   /** the label */
   private String label;

   /** the image */
   private String image;

   /** the value to be selected initially */
   private String value;
   
   /** the disabled flag */
   private String disabled;
   
   /** the description */
   private String description;
}
