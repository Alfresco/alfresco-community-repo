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
package org.alfresco.web.ui.common.component.evaluator;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author kevinr
 * 
 * Evaluates to true if the value exactly matches the supplied string condition.
 */
public class StringEqualsEvaluator extends BaseEvaluator
{
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         result = getCondition().equals((String)getValue());
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Expected String value for evaluation: " + getValue());
      }
      
      return result;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.condition = (String)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.condition;
      return (values);
   }
   
   /**
    * Get the string condition to match value against
    * 
    * @return the string condition to match value against
    */
   public String getCondition()
   {
      ValueBinding vb = getValueBinding("condition");
      if (vb != null)
      {
         this.condition = (String)vb.getValue(getFacesContext());
      }
      
      return this.condition;
   }
   
   /**
    * Set the string condition to match value against
    * 
    * @param condition     string condition to match value against
    */
   public void setCondition(String condition)
   {
      this.condition = condition;
   }
   
   
   /** the string condition to match value against */
   private String condition = null;
}
