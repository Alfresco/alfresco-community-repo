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
package org.alfresco.web.ui.repo.component.shelf;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;

/**
 * @author Kevin Roast
 */
public class UIShelfGroup extends SelfRenderingComponent
{
   // ------------------------------------------------------------------------------
   // Component Impl 

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Shelf";
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.expanded = ((Boolean)values[1]).booleanValue();
      this.label = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = (this.expanded ? Boolean.TRUE : Boolean.FALSE);
      values[2] = this.label;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      // output each shelf group component in turn
      out.write("<table cellspacing=0 cellpadding=0 border=0 width=100%>");
      for (Iterator i=this.getChildren().iterator(); i.hasNext(); /**/)
      {
         UIComponent child = (UIComponent)i.next();
         if (child instanceof UIShelfItem)
         {
            // render child items
            out.write("<tr><td>");
            Utils.encodeRecursive(context, child);
            out.write("</tr></td>");
         }
      }
      out.write("</table>");
   }

   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 
   
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
    * Returns whether the component show allow rendering of its child components.
    */
   public boolean isExpanded()
   {
      ValueBinding vb = getValueBinding("expanded");
      if (vb != null)
      {
         Boolean expanded = (Boolean)vb.getValue(getFacesContext());
         if (expanded != null)
         {
            this.expanded = expanded.booleanValue();
         }
      }
      
      return this.expanded;
   }
   
   /**
    * Sets whether the group is expanded
    */
   public void setExpanded(boolean expanded)
   {
      this.expanded = expanded;
   }
   
   
   private String label = null;
   private boolean expanded = false;
}
