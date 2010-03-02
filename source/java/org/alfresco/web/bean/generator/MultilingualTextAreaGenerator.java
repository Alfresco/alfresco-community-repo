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
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a multilingual text area component.
 * 
 * @author gavinc
 */
public class MultilingualTextAreaGenerator extends TextAreaGenerator
{
   @Override
   public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet,
            PropertySheetItem item)
   {
      UIComponent component = super.generateAndAdd(context, propertySheet, item);
      
      if ((component instanceof UIInput) && (component instanceof UISelectOne) == false)
      {
         component.setRendererType(RepoConstants.ALFRESCO_FACES_MLTEXTAREA_RENDERER);
      }
      else if ((component instanceof UIOutput))
      {
         component.setRendererType(RepoConstants.ALFRESCO_FACES_MLTEXT_RENDERER);
      }
      
      return component;
   }
}
