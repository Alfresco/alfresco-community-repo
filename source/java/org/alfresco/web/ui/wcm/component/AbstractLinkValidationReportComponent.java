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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.ui.wcm.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.web.bean.wcm.LinkValidationState;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;

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
