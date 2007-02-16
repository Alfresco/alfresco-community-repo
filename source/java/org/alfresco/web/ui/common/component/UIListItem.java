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
package org.alfresco.web.ui.common.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author kevinr
 */
public class UIListItem extends SelfRenderingComponent
{
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Controls";
   }

   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      values[0] = super.saveState(context);
      values[1] = this.value;
      values[2] = this.disabled;
      values[3] = this.label;
      values[4] = this.description;
      values[5] = this.tooltip;
      values[6] = this.image;
      return ((Object) (values));
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      super.restoreState(context, values[0]);
      this.value = values[1];
      this.disabled = (Boolean)values[2];
      this.label = (String)values[3];
      this.description = (String)values[4];
      this.tooltip = (String)values[5];
      this.image = (String)values[6];
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * Get the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @return the value
    */
   public Object getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = vb.getValue(getFacesContext());
      }
      
      return this.value;
   }

   /**
    * Set the value - the value is used in a equals() match against the current value in the
    * parent ModeList component to set the selected item.
    *
    * @param value     the value
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
   
   /**
    * Returns the disabled flag
    * 
    * @return true if the mode list is disabled
    */
   public boolean isDisabled()
   {
      ValueBinding vb = getValueBinding("disabled");
      if (vb != null)
      {
         this.disabled = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.disabled != null)
      {
         return this.disabled.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Sets whether the mode list is disabled
    * 
    * @param disabled   the disabled flag
    */
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
   
   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      ValueBinding vb = getValueBinding("label");
      if (vb != null)
      {
         this.label = (String)vb.getValue(getFacesContext());
      }
      
      return this.label;
   }

   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      ValueBinding vb = getValueBinding("description");
      if (vb != null)
      {
         this.description = (String)vb.getValue(getFacesContext());
      }
      
      return this.description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return Returns the image.
    */
   public String getImage()
   {
      ValueBinding vb = getValueBinding("image");
      if (vb != null)
      {
         this.image = (String)vb.getValue(getFacesContext());
      }
      
      return this.image;
   }

   /**
    * @param image The image to set.
    */
   public void setImage(String image)
   {
      this.image = image;
   }

   /**
    * @return Returns the tooltip.
    */
   public String getTooltip()
   {
      ValueBinding vb = getValueBinding("tooltip");
      if (vb != null)
      {
         this.tooltip = (String)vb.getValue(getFacesContext());
      }
      
      return this.tooltip;
   }

   /**
    * @param tooltip The tooltip to set.
    */
   public void setTooltip(String tooltip)
   {
      this.tooltip = tooltip;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   /** the component value */
   private Object value = null;
   
   /** disabled flag */
   private Boolean disabled = null;
   
   /** the tooltip */
   private String tooltip;

   /** the label */
   private String label;
   
   /** the description */
   private String description;
   
   /** the image */
   private String image;
}
