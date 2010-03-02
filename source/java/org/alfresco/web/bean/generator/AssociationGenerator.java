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

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a component to manage associations.
 *
 * @author gavinc
 */
public class AssociationGenerator extends BaseComponentGenerator
{
   protected String optionsSize = null;
   
   public String getAvailableOptionsSize()
   {
      return this.optionsSize;
   }

   public void setAvailableOptionsSize(String optionsSize)
   {
      this.optionsSize = optionsSize;
   }

   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_ASSOC_EDITOR);
      FacesHelper.setupComponentId(context, component, id);
      
      // set the size of the list (if provided)
      if (this.optionsSize != null)
      {
         component.getAttributes().put("availableOptionsSize", this.optionsSize);
      }
      
      return component;
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      // Override the setup of the mandatory validation 
      // so we can send the _current_value id suffix.
      // We also enable real time so the page load
      // check disables the ok button if necessary, as the user
      // adds or removes items from the multi value list the 
      // page will be refreshed and therefore re-check the status.
      // Only so this however id the component is not read-only
      
      if (item.isReadOnly() == false)
      {
         super.setupMandatoryValidation(context, propertySheet, item, 
                  component, true, "_current_value");
      }
   }
}
