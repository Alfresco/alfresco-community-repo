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
package org.alfresco.web.ui.common.component.data;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author Kevin Roast
 */
public class UIColumn extends UIComponentBase
{
   // ------------------------------------------------------------------------------
   // Component implementation 
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Data";
   }
   
   /**
    * Return the UI Component to be used as the header for this column
    * 
    * @return UIComponent
    */
   public UIComponent getHeader()
   {
      return getFacet("header");
   }
   
   /**
    * Return the UI Component to be used as the footer for this column
    * 
    * @return UIComponent
    */
   public UIComponent getFooter()
   {
      return getFacet("footer");
   }
   
   /**
    * Return the UI Component to be used as the large icon for this column
    * 
    * @return UIComponent
    */
   public UIComponent getLargeIcon()
   {
      return getFacet("large-icon");
   }
   
   /**
    * Return the UI Component to be used as the small icon for this column
    * 
    * @return UIComponent
    */
   public UIComponent getSmallIcon()
   {
      return getFacet("small-icon");
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.primary = ((Boolean)values[1]).booleanValue();
      this.actions = ((Boolean)values[2]).booleanValue();
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = (this.primary ? Boolean.TRUE : Boolean.FALSE);
      values[2] = (this.actions ? Boolean.TRUE : Boolean.FALSE);
      return (values);
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return true if this is the primary column
    */
   public boolean getPrimary()
   {
      ValueBinding vb = getValueBinding("primary");
      if (vb != null)
      {
         this.primary = (Boolean)vb.getValue(getFacesContext());
      }
      return this.primary;
   }
   
   /**
    * @param primary    True if this is the primary column, false otherwise
    */
   public void setPrimary(boolean primary)
   {
      this.primary = primary;
   }
   
   /**
    * @return true if this is the column containing actions for the current row
    */
   public boolean getActions()
   {
      ValueBinding vb = getValueBinding("actions");
      if (vb != null)
      {
         this.actions = (Boolean)vb.getValue(getFacesContext());
      }
      return this.actions;
   }
   
   /**
    * @param actions    True if this is the column containing actions for the current row
    */
   public void setActions(boolean actions)
   {
      this.actions = actions;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data 
   
   private boolean primary = false;
   private boolean actions = false;
}
