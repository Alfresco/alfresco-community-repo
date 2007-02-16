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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class TextAreaGenerator extends TextFieldGenerator
{
   private int rows = 3;
   private int columns = 32;
   
   /**
    * @return Returns the number of columns
    */
   public int getColumns()
   {
      return columns;
   }

   /**
    * @param columns Sets the number of columns
    */
   public void setColumns(int columns)
   {
      this.columns = columns;
   }

   /**
    * @return Returns the number of rows
    */
   public int getRows()
   {
      return rows;
   }

   /**
    * @param rows Sets the number of rows
    */
   public void setRows(int rows)
   {
      this.rows = rows;
   }

   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(ComponentConstants.JAVAX_FACES_TEXTAREA);
      FacesHelper.setupComponentId(context, component, id);

      component.getAttributes().put("rows", this.rows);
      component.getAttributes().put("cols", this.columns);
      
      return component;
   }
}
