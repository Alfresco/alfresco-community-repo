/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
