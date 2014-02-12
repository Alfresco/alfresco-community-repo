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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
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
      
      // MNT-10171 Exception thrown if Share metadata is longer than 1024 characters
      if (ContentModel.PROP_DESCRIPTION.getLocalName().equals(id))
      {
          // add 'onfocus' event for adding 'maxlength' attribute
          component.getAttributes().put("onfocus", "addMaxLengthForDescriptionTextArea(this)");
      }
      
      return component;
   }
}
