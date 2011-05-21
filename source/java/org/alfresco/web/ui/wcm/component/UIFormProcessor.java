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
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * @author Ariel Backenroth
 */
public class UIFormProcessor 
   extends SelfRenderingComponent
{
   private static final Log LOGGER = LogFactory.getLog(UIFormProcessor.class);

   private Document formInstanceData = null;
   private String formInstanceDataName = null;
   private Form form = null;
   private FormProcessor.Session formProcessorSession = null;
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.FormProcessor";
   }
   
   public void restoreState(final FacesContext context, final Object state)
   {
      final Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.formInstanceData = (Document)values[1];
      this.formInstanceDataName = (String)values[2];
      this.form = (Form)values[3];
      this.formProcessorSession = (FormProcessor.Session)values[4];
   }
   
   public Object saveState(FacesContext context)
   {
      return new Object[] 
      {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.formInstanceData,
         this.formInstanceDataName,
         this.form,
         this.formProcessorSession
      };
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(final FacesContext context) 
      throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      
      final ResponseWriter out = context.getResponseWriter();
      final Form form = this.getForm();
      final FormProcessor fp = form.getFormProcessors().get(0);
      final FormProcessor.Session fps = this.getFormProcessorSession();
      final Document fid = this.getFormInstanceData();
      final String formInstanceDataName = this.getFormInstanceDataName();
      try
      {
         if (fps != null && 
             fps.getForm().equals(form) && 
             fps.getFormInstanceData().equals(fid) &&
             fps.getFormInstanceDataName().equals(formInstanceDataName))
         {
            LOGGER.debug("reusing form processor session " + fps);
            fp.process(this.formProcessorSession, out);
         }
         else
         {
            if (fps != null)
            {
               this.setFormProcessorSession(null);
               LOGGER.debug("clearing form instance data " + fid);
               fid.removeChild(fid.getDocumentElement());
            }
            LOGGER.debug("creating a new session for " + fid);
            this.setFormProcessorSession(fp.process(fid,
                                                    formInstanceDataName,
                                                    form,
                                                    out));
         }
      }
      catch (Throwable t)
      {
         Utils.addErrorMessage(t.getMessage(), t);
         out.write(t.toString());
      }
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the instance data to render
    *
    * @return The instance data to render
    */
   public Document getFormInstanceData()
   {
      final ValueBinding vb = getValueBinding("formInstanceData");
      if (vb != null)
      {
         this.formInstanceData = (Document)vb.getValue(getFacesContext());
      }
      return this.formInstanceData;
   }
   
   /**
    * Sets the instance data to render
    *
    * @param formInstanceData The instance data to render
    */
   public void setFormInstanceData(final Document formInstanceData)
   {
      this.formInstanceData = formInstanceData;
   }

   /**
    * Returns the form instance data name
    *
    * @return The form instance data name
    */
   public String getFormInstanceDataName()
   {
      final ValueBinding vb = this.getValueBinding("formInstanceDataName");
      if (vb != null)
      {
         this.formInstanceDataName = (String)vb.getValue(getFacesContext());
      }
      return this.formInstanceDataName;
   }
   
   /**
    * Sets the form instance data name
    *
    * @param formInstanceDataName The form instance data name.
    */
   public void setForm(final String formInstanceDataName)
   {
      this.formInstanceDataName = formInstanceDataName;
   }

   /**
    * Returns the form
    *
    * @return The form
    */
   public Form getForm()
   {
      final ValueBinding vb = this.getValueBinding("form");
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

   /**
    * Returns the form processor session
    *
    * @return the form processor session
    */
   public FormProcessor.Session getFormProcessorSession()
   {
      final ValueBinding vb = this.getValueBinding("formProcessorSession");
      if (vb != null)
      {
         this.formProcessorSession = (FormProcessor.Session)
            vb.getValue(getFacesContext());
      }
      LOGGER.debug("getFormProcessorSession() = " + this.formProcessorSession);      
      return this.formProcessorSession;
   }

   /**
    * Sets the form processor session
    *
    * @param formProcessorSession the form processor session.
    */
   public void setFormProcessorSession(final FormProcessor.Session formProcessorSession)
   {
      if (formProcessorSession == null && this.formProcessorSession != null)
      {
         LOGGER.debug("destroying old session " + this.formProcessorSession);
         this.formProcessorSession.destroy();
      }
      this.formProcessorSession = formProcessorSession;
      final ValueBinding vb = this.getValueBinding("formProcessorSession");
      if (vb != null)
      {
         vb.setValue(getFacesContext(), formProcessorSession);
      }
   }
}
