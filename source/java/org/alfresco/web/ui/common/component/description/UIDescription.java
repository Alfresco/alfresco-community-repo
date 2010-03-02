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
package org.alfresco.web.ui.common.component.description;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * Description component that outputs a dynamic description
 * 
 * @author gavinc
 */
public class UIDescription extends SelfRenderingComponent implements Serializable
{
   private static final long serialVersionUID = -2319791691993957792L;
   
   private String controlValue;
   private String text;

   /**
    * @return The control value the description is for
    */
   public String getControlValue()
   {
      if (this.controlValue == null)
      {
         ValueBinding vb = getValueBinding("controlValue");
         if (vb != null)
         {
            this.controlValue = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.controlValue;
   }

   /**
    * @param controlValue Sets the control value this description is for
    */
   public void setControlValue(String controlValue)
   {
      this.controlValue = controlValue;
   }

   /**
    * @return Returns the description text
    */
   public String getText()
   {
      if (this.text == null)
      {
         ValueBinding vb = getValueBinding("text");
         if (vb != null)
         {
            this.text = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.text;
   }

   /**
    * @param text Sets the description text 
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Description";
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.controlValue = (String)values[1];
      this.text = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.controlValue;
      values[2] = this.text;
      return (values);
   }

   /**
    * @see javax.faces.component.UIComponent#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return false;
   }
}
