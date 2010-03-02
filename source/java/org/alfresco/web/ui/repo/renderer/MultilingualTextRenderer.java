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
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.apache.myfaces.renderkit.html.HtmlTextRenderer;

/**
 * Renders a multilingual text field.
 * <p>
 * Renders the default output followed by an icon
 * to represent multilingual properties.
 * </p>
 * 
 * @author gavinc
 */
public class MultilingualTextRenderer extends HtmlTextRenderer
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
