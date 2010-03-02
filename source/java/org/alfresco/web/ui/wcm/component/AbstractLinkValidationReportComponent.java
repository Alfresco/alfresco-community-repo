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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.web.bean.wcm.LinkValidationState;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * Base class for all the link validation report JSF components.
 * 
 * @author gavinc
 */
public abstract class AbstractLinkValidationReportComponent extends SelfRenderingComponent
{
   protected LinkValidationState state;
   
   // ------------------------------------------------------------------------------
   // Component implementation

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.state = (LinkValidationState)values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.state;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      // the child components are rendered explicitly during the encodeBegin()
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return The LinkValidationState object holding the report information
    */
   public LinkValidationState getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.state = (LinkValidationState)vb.getValue(getFacesContext());
      }
      
      return this.state;
   }
   
   /**
    * @param value The LinkValidationState object to get the summary info from
    */
   public void setValue(LinkValidationState value)
   {
      this.state = value;
   }
}
