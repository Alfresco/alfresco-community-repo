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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a category selector component.
 * 
 * @author gavinc
 */
public class CategorySelectorGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      // the category selector component is used whatever mode the property sheet is in
      return generate(context, item.getName());
   }

   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      if (component instanceof UIMultiValueEditor)
      {
         // Override the setup of the mandatory validation 
         // so we can send the _current_value id suffix.
         // We also enable real time so the page load
         // check disables the ok button if necessary, as the user
         // adds or removes items from the multi value list the 
         // page will be refreshed and therefore re-check the status.
         
         super.setupMandatoryValidation(context, propertySheet, item, 
               component, true, "_current_value");
      }
      else
      {
         // Override the setup of the mandatory validation 
         // so we can send the _selected id suffix.
         // We also enable real time so the page load check disabled
         // the ok button if necessary, as the control is used the
         // page will be refreshed and therefore re-check the status.
         
         super.setupMandatoryValidation(context, propertySheet, item, 
               component, true, "_selected");
      }
   }

   @Override
   protected ControlType getControlType()
   {
      return ControlType.SELECTOR;
   }
}
