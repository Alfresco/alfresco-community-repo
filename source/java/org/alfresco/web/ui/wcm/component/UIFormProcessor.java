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
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
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
      this.form = (Form)values[2];
      this.formProcessorSession = (FormProcessor.Session)values[3];
   }
   
   public Object saveState(FacesContext context)
   {
      return new Object[] 
      {
         // standard component attributes are saved by the super class
         super.saveState(context),
         this.formInstanceData,
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
      try
      {
         if (fps != null && fps.getFormInstanceData().equals(fid))
         {
            LOGGER.debug("reusing form processor session " + fps);
            fp.process(this.formProcessorSession, out);
         }
         else
         {
            if (fps != null)
            {
               this.setFormProcessorSession(null);
            }
            LOGGER.debug("creating a new session for " + fid);
            this.setFormProcessorSession(fp.process(fid,
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
