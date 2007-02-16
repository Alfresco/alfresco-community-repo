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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.repo.renderer;

import java.io.IOException;

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

   @Override
   protected void renderPostWrappedComponent(FacesContext context, ResponseWriter out, 
         UIMultiValueEditor editor) throws IOException
   {
      out.write("&nbsp;<input type='submit' value='");
      out.write(Application.getMessage(context, MSG_ADD_TO_LIST_BUTTON));
      out.write("' onclick=\"");
      out.write(generateFormSubmit(context, editor, Integer.toString(UIMultiValueEditor.ACTION_ADD)));
      out.write("\"/></td></tr>");
   }
}
