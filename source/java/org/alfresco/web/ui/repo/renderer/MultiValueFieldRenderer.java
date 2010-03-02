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
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;

/**
 * Renders the MultiValueEditor component for use with field components
 * i.e. text, checkboxes, lists etc.
 * 
 * This renderer does not show a "select item" message or a select button,
 * the wrapped component is shown immediately with an add to list button
 * after it.
 * 
 * @author gavinc
 */
public class MultiValueFieldRenderer extends BaseMultiValueRenderer
{
   @Override
   protected void renderPreWrappedComponent(FacesContext context, ResponseWriter out, 
         UIMultiValueEditor editor) throws IOException
   {
      out.write("<tr><td>");
   }

   @SuppressWarnings("unchecked")
   @Override
   protected void renderPostWrappedComponent(FacesContext context, ResponseWriter out, 
         UIMultiValueEditor editor) throws IOException
   {
      out.write("&nbsp;<input type='button' value='");
      out.write(Application.getMessage(context, MSG_ADD_TO_LIST_BUTTON));
      out.write("' onclick=\"");
      out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
      out.write("\"/>");
      
      // if the wrapped component is an mltext field add the icon
      if (editor.getAttributes().get("mltext") != null)
      {
          String tooltip = Application.getMessage(context, "marker_tooltip");
          out.write("<img src='");
          out.write(context.getExternalContext().getRequestContextPath());
          out.write("/images/icons/multilingual_marker.gif' title='");
          out.write(tooltip);
          out.write("' style='margin-left:6px; vertical-align:-2px;'>");
      }
      
      out.write("</td></tr>");
   }
}
