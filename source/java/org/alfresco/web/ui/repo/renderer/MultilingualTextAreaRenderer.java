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
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.apache.myfaces.renderkit.html.HtmlTextareaRenderer;

/**
 * Renders a multilingual text area.
 * <p>
 * Renders the default output followed by an icon
 * to represent multilingual properties.
 * </p>
 * 
 * @author gavinc
 */
public class MultilingualTextAreaRenderer extends HtmlTextareaRenderer
{
   @Override
   public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
   {
      super.encodeEnd(facesContext, component);
      
      String tooltip = Application.getMessage(facesContext, "marker_tooltip");
      ResponseWriter out = facesContext.getResponseWriter();
      out.write("<img src='");
      out.write(facesContext.getExternalContext().getRequestContextPath());
      out.write("/images/icons/multilingual_marker.gif' title='");
      out.write(tooltip);
      out.write("' style='margin-left:6px; vertical-align:-2px;'>");
   }
}
