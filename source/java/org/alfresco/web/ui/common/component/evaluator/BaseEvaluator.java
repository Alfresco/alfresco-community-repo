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

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.log4j.Logger;

import org.alfresco.web.ui.common.component.SelfRenderingComponent;

/**
 * @author kevinr
 */
public abstract class BaseEvaluator extends SelfRenderingComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public final String getFamily()
   {
      return "org.alfresco.faces.evaluators";
   }

   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public final boolean getRendersChildren()
   {
      return !evaluate();
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public final void encodeBegin(FacesContext context) throws IOException
   {
      // no output for this component
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public final void encodeChildren(FacesContext context) throws IOException
   {
      // if this is called, then the evaluate returned false which means
      // the child components show not be allowed to render themselves
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
    */
   public final void encodeEnd(FacesContext context) throws IOException
   {
      // no output for this component
   }
   
   /**
    * Get the value for this component to be evaluated against
    *
    * @return the value for this component to be evaluated against
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
    * Set the value for this component to be evaluated against
    *
    * @param value     the value for this component to be evaluated against
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      return (values);
   }
   
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public abstract boolean evaluate();
   
   
   protected static Logger s_logger = Logger.getLogger(BaseEvaluator.class);
   
   /** the value to be evaluated against */
   private Object value;
}
