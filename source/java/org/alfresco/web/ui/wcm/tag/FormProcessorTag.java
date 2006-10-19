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
package org.alfresco.web.ui.wcm.tag;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * @author Ariel Backenroth
 */
public class FormProcessorTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.FormProcessor";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(final UIComponent component)
   {
      super.setProperties(component);
      final Application app = this.getFacesContext().getApplication();
      if (this.instanceData != null)
      {
         assert isValueReference(this.instanceData);
         final ValueBinding vb = app.createValueBinding(this.instanceData);
         component.setValueBinding("formInstanceData", vb);
      }
      if (this.templateType != null)
      {
         assert this.isValueReference(this.templateType);
         final ValueBinding vb = app.createValueBinding(this.templateType);
         component.setValueBinding("form", vb);
      }
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.instanceData = null;
      this.templateType = null;
   }
   
   /**
    * Set the instance data
    *
    * @param instanceData the instance data for the processor
    */
   public void setFormInstanceData(final String instanceData)
   {
      this.instanceData = instanceData;
   }

   /**
    * Sets the tempalte type
    *
    * @param templateType the tempalteType for the processor.
    */
   public void setForm(final String templateType)
   {
      this.templateType = templateType;
   }
   
   private String instanceData;
   private String templateType;
}
