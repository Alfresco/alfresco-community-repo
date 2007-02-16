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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.ui.wcm.tag;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * @author Ariel Backenroth
 */
public class FormProcessorTag 
   extends BaseComponentTag
{
   private String formInstanceData = null;
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
