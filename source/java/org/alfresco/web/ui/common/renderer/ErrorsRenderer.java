/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
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
         String errorClass = (String)component.getAttributes().get("errorClass");
         String infoClass = (String)component.getAttributes().get("infoClass");
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
         
         // if we have a message to display show it next to the info icon
         if (message.length() > 0)
         {
            out.write("<img src='");
            out.write(contextPath);
            out.write("/images/icons/info_icon.gif' alt='Error' align='absmiddle'/>&nbsp;&nbsp;");
            out.write(Utils.encode(message));
            out.write("\n<ul style='margin:2px;'>");
            
            while (messages.hasNext())
            {
               FacesMessage fm = (FacesMessage)messages.next();
               out.write("<li");
               renderMessageAttrs(fm, out, errorClass, infoClass);
               out.write(">");
               out.write(Utils.encode(fm.getSummary()));
               String detail = fm.getDetail();
               if (detail != null && detail.length() > 0)
               {
                  out.write("&nbsp(");
                  out.write(Utils.encode(detail));
                  out.write(")");
               }
               out.write("</li>\n");
            }
            
            out.write("</ul>");
         }
         else
         {
            // if there is no title message to display use a table to place
            // the info icon on the left and the list of messages on the right
            out.write("<table border='0' cellpadding='3' cellspacing='0'><tr><td valign='top'><img src='");
            out.write(contextPath);
            out.write("/images/icons/info_icon.gif' alt='Error' />");
            out.write("</td><td>");
            
            while (messages.hasNext())
            {
               FacesMessage fm = (FacesMessage)messages.next();
               out.write("<div style='margin-bottom: 3px;'");
               renderMessageAttrs(fm, out, errorClass, infoClass);
               out.write(">");
               out.write(Utils.encode(fm.getSummary()));
               String detail = fm.getDetail();
               if (detail != null && detail.length() > 0)
               {
                  out.write("&nbsp(");
                  out.write(Utils.encode(detail));
                  out.write(")");
               }
               out.write("</div>\n");
            }
            
            out.write("</td></tr></table>");
         }
         
         out.write("</div>\n");
         
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
   
   /**
    * Renders the attributes for the given FacesMessage.
    */
   private void renderMessageAttrs(FacesMessage fm, ResponseWriter out,
         String errorClass, String infoClass) throws IOException
   {
      // use the error class if the message is error level
      if (errorClass != null && 
          fm.getSeverity() == FacesMessage.SEVERITY_ERROR ||
          fm.getSeverity() == FacesMessage.SEVERITY_FATAL)
      {
         out.write(" class='");
         out.write(errorClass);
         out.write("'");
      }
      
      // use the info class if the message is info level
      if (infoClass != null && 
          fm.getSeverity() == FacesMessage.SEVERITY_INFO ||
          fm.getSeverity() == FacesMessage.SEVERITY_WARN)
      {
         out.write(" class='");
         out.write(infoClass);
         out.write("'");
      }
   }
}
