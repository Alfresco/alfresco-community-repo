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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ariel Backenroth
 */
public class UIFormProcessor extends SelfRenderingComponent
{
   private static final Log LOGGER = LogFactory.getLog(UIFormProcessor.class);
   
   
   private FormProcessor.InstanceData formInstanceData = null;
   
   private Form form = null;
   
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.FormProcessor";
   }
   
   public void restoreState(FacesContext context, Object state)
   {
      final Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.formInstanceData = (FormProcessor.InstanceData)values[1];
      this.form = (Form)values[2];
   }
   
   public Object saveState(FacesContext context)
   {
      final Object values[] = {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.formInstanceData,
         this.form
      };
      return values;
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(final FacesContext context) 
      throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      final ResponseWriter out = context.getResponseWriter();
      final Form form = this.getForm();
      final FormProcessor.InstanceData formInstanceData = this.getFormInstanceData();
      final FormProcessor tim = form.getFormProcessors().get(0);
      tim.process(formInstanceData, form, out);
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the instance data to render
    *
    * @return The instance data to render
    */
   public FormProcessor.InstanceData getFormInstanceData()
   {
      final ValueBinding vb = getValueBinding("formInstanceData");
      if (vb != null)
      {
         this.formInstanceData = (FormProcessor.InstanceData)vb.getValue(getFacesContext());
      }
      
      return this.formInstanceData;
   }
   
   /**
    * Sets the instance data to render
    *
    * @param formInstanceData The instance data to render
    */
   public void setFormInstanceData(final FormProcessor.InstanceData formInstanceData)
   {
      this.formInstanceData = formInstanceData;
   }

   /**
    * Returns the form
    *
    * @return The form
    */
   public Form getForm()
   {
      final ValueBinding vb = getValueBinding("form");
      if (vb != null)
      {
         this.form = (Form)vb.getValue(getFacesContext());
      }
      
      return this.form;
   }
   
   /**
    * Sets the form
    *
    * @param form The form
    */
   public void setForm(final Form form)
   {
      this.form = form;
   }
}
