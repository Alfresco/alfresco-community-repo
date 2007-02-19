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
package org.alfresco.web.ui.common.component;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

/**
 * @author Kevin Roast
 */
public class UIModeList extends UICommand
{
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default constructor
    */
   public UIModeList()
   {
      setRendererType("org.alfresco.faces.ModeListRenderer");
   }
   
   
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
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.iconColumnWidth = (Integer)values[1];
      this.horizontal = (Boolean)values[2];
      this.disabled = (Boolean)values[3];
      this.label = (String)values[4];
      this.menu = (Boolean)values[5];
      this.menuImage = (String)values[6];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.iconColumnWidth;
      values[2] = this.horizontal;
      values[3] = this.disabled;
      values[4] = this.label;
      values[5] = this.menu;
      values[6] = this.menuImage;
      return (values);
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ModeListItemSelectedEvent)
      {
         // found an event for us, update the value for this component
         setValue( ((ModeListItemSelectedEvent)event).SelectedValue );
      }
      
      // default ActionEvent processing for a UICommand
      super.broadcast(event);
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed property accessors 
   
   /**
    * Get the horizontal rendering flag
    *
    * @return true for horizontal rendering, false otherwise
    */
   public boolean isHorizontal()
   {
      ValueBinding vb = getValueBinding("horizontal");
      if (vb != null)
      {
         this.horizontal = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.horizontal != null)
      {
         return this.horizontal.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Set true for horizontal rendering, false otherwise
    *
    * @param horizontal       the horizontal
    */
   public void setHorizontal(boolean horizontal)
   {
      this.horizontal = horizontal;
   }
   
   /**
    * Get the icon column width
    *
    * @return the icon column width
    */
   public int getIconColumnWidth()
   {
      ValueBinding vb = getValueBinding("iconColumnWidth");
      if (vb != null)
      {
         this.iconColumnWidth = (Integer)vb.getValue(getFacesContext());
      }
      
      if (this.iconColumnWidth != null)
      {
         return this.iconColumnWidth.intValue();
      }
      else
      {
         // return the default
         return 20;
      }
   }

   /**
    * Set the icon column width
    *
    * @param iconColumnWidth     the icon column width
    */
   public void setIconColumnWidth(int iconColumnWidth)
   {
      this.iconColumnWidth = Integer.valueOf(iconColumnWidth);
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
    * Returns the menu rendering flag
    * 
    * @return true if the menu rendering mode is to be used
    */
   public boolean isMenu()
   {
      ValueBinding vb = getValueBinding("menu");
      if (vb != null)
      {
         this.menu = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.menu != null)
      {
         return this.menu.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Sets whether the mode list is a menu
    * 
    * @param menu       the menu flag
    */
   public void setMenu(boolean menu)
   {
      this.menu = menu;
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
    * @return Returns the menu image.
    */
   public String getMenuImage()
   {
      ValueBinding vb = getValueBinding("menuImage");
      if (vb != null)
      {
         this.menuImage = (String)vb.getValue(getFacesContext());
      }
      
      return this.menuImage;
   }

   /**
    * @param menuImage The menu image to set.
    */
   public void setMenuImage(String menuImage)
   {
      this.menuImage = menuImage;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data 

   /** the icon column width */
   private Integer iconColumnWidth;

   /** true for horizontal rendering, false otherwise */
   private Boolean horizontal = null;
   
   /** disabled flag */
   private Boolean disabled = null;
   
   /** menu rendering flag */
   private Boolean menu = null;
   
   /** menu image to use */
   private String menuImage = null;
   
   /** the label */
   private String label;
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing a change in selection for a ModeList component.
    */
   public static class ModeListItemSelectedEvent extends ActionEvent
   {
      private static final long serialVersionUID = 3618135654274774322L;

      public ModeListItemSelectedEvent(UIComponent component, Object selectedValue)
      {
         super(component);
         SelectedValue = selectedValue;
      }
      
      public Object SelectedValue = null;
   }
}
