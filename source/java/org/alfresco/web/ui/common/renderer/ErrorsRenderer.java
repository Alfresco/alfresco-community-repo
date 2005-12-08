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
package org.alfresco.web.ui.common.renderer;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;

/**
 * Renderer that displays any errors that occurred in the previous lifecylce 
 * processing within a gradient panel
 * 
 * @author gavinc
 */
public class ErrorsRenderer extends BaseRenderer
{
   private static final String DEFAULT_MESSAGE = "wizard_errors";
   
   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }
      
      Iterator messages = context.getMessages();
      if (messages.hasNext())
      {
         ResponseWriter out = context.getResponseWriter();
         String contextPath = context.getExternalContext().getRequestContextPath();
         String styleClass = (String)component.getAttributes().get("styleClass");
         String message = (String)component.getAttributes().get("message");
         
         if (message == null)
         {
            // because we are using the standard messages component value binding
            // would not be handled for the message attribute so do it here
            ValueBinding vb = component.getValueBinding("message");
            if (vb != null)
            {
               message = (String)vb.getValue(context);
            }
            
            if (message == null)
            {
               message = Application.getMessage(context, DEFAULT_MESSAGE);
            }
         }
         
         PanelGenerator.generatePanelStart(out, contextPath, "yellowInner", "#ffffcc");
         
         out.write("\n<div");
         if (styleClass != null)
         {
            outputAttribute(out, styleClass, "class");
         }
         out.write(">");
         out.write("<img src='");
         out.write(contextPath);
         out.write("/images/icons/info_icon.gif' alt='Error' align='absmiddle'/>&nbsp;&nbsp;");
         out.write(Utils.encode(message));
         out.write("\n<ul style='margin:2px;'>");
         
         while (messages.hasNext())
         {
            FacesMessage fm = (FacesMessage)messages.next();
            out.write("<li>");
            out.write(Utils.encode(fm.getSummary()));
            out.write("</li>\n");
         }
         
         out.write("</ul></div>\n");
         
         PanelGenerator.generatePanelEnd(out, contextPath, "yellowInner");
         
         // TODO: Expose this as a configurable attribute i.e. padding at bottom
         out.write("<div style='padding:2px;'></div>");
      }
   }

   /**
    * @see javax.faces.render.Renderer#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return false;
   }
}
