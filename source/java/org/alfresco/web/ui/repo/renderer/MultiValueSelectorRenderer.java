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

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;

/**
 * Renders the MultiValueEditor component for use with picker components.
 * 
 * This renderer shows a "select items" message and a select button. When
 * the select button is pressed the wrapped component will appear and the
 * add to list button will be enabled.
 * 
 * @author gavinc
 */
public class MultiValueSelectorRenderer extends BaseMultiValueRenderer
{
   @Override
   protected void renderPreWrappedComponent(FacesContext context, ResponseWriter out,
         UIMultiValueEditor editor) throws IOException
   {
      // show the select an item message
      out.write("<tr><td>");
      out.write("1. ");
      out.write(editor.getSelectItemMsg());
      out.write("</td></tr>");
      
      if (editor.getAddingNewItem())
      {
         out.write("<tr><td style='padding-left:8px'>");
      }
      else
      {
         out.write("<tr><td style='padding-left:8px;'><input type='submit' value='");
         out.write(Application.getMessage(context, MSG_SELECT_BUTTON));
         out.write("' onclick=\"");
         out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_SELECT)));
         out.write("\"/></td></tr>");
      }
   }
   
   @Override
   protected void renderPostWrappedComponent(FacesContext context, ResponseWriter out,
         UIMultiValueEditor editor) throws IOException
   {
      if (editor.getAddingNewItem())
      {
         out.write("</td></tr>");
      }
      
      // show the add to list button but only if something has been selected
      out.write("<tr><td>2. <input type='submit'");
      if (editor.getAddingNewItem() == false && editor.getLastItemAdded() != null || 
          editor.getLastItemAdded() == null)
      {
         out.write(" disabled='true'");
      }
      out.write(" value='");
      out.write(Application.getMessage(context, MSG_ADD_TO_LIST_BUTTON));
      out.write("' onclick=\"");
      out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
      out.write("\"/></td></tr>");
   }
}
