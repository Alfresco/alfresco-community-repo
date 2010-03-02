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
package org.alfresco.web.ui.wcm.tag;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * @author Ariel Backenroth
 */
public class FormProcessorTag 
   extends BaseComponentTag
{
   private String formInstanceData = null;
   private String formInstanceDataName = null;
   private String form = null;
   private String formProcessorSession = null;

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
      if (this.formInstanceData != null)
      {
         assert isValueReference(this.formInstanceData);
         final ValueBinding vb = app.createValueBinding(this.formInstanceData);
         component.setValueBinding("formInstanceData", vb);
      }
      if (this.formInstanceDataName != null)
      {
         assert isValueReference(this.formInstanceDataName);
         final ValueBinding vb = app.createValueBinding(this.formInstanceDataName);
         component.setValueBinding("formInstanceDataName", vb);
      }
      if (this.form != null)
      {
         assert this.isValueReference(this.form);
         final ValueBinding vb = app.createValueBinding(this.form);
         component.setValueBinding("form", vb);
      }
      if (this.formProcessorSession != null)
      {
         assert this.isValueReference(this.formProcessorSession);
         final ValueBinding vb = app.createValueBinding(this.formProcessorSession);
         component.setValueBinding("formProcessorSession", vb);
      }
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.formInstanceData = null;
      this.formInstanceDataName = null;
      this.form = null;
      this.formProcessorSession = null;
   }
   
   /**
    * Set the instance data
    *
    * @param formInstanceData the instance data for the processor
    */
   public void setFormInstanceData(final String formInstanceData)
   {
      this.formInstanceData = formInstanceData;
   }

   /**
    * Set the form instance data name
    *
    * @param formInstanceDataName the form instance data name
    */
   public void setFormInstanceDataName(final String formInstanceDataName)
   {
      this.formInstanceDataName = formInstanceDataName;
   }

   /**
    * Sets the tempalte type
    *
    * @param form the tempalteType for the processor.
    */
   public void setForm(final String form)
   {
      this.form = form;
   }

   /**
    * Sets the form processor session
    *
    * @param formProcessorSession the binding for the form processor session
    */
   public void setFormProcessorSession(final String formProcessorSession)
   {
      this.formProcessorSession = formProcessorSession;
   }
}
