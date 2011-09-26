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

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Interface definition for objects that dynamically generate components.
 * 
 * @author gavinc
 */
public interface IComponentGenerator
{
   /**
    * Dynamically generates a component in a default state
    * 
    * @param context FacesContext
    * @param id Optional id for the newly created component, if null 
    *        is passed a unique id is generated
    * @return The component instance
    */
   UIComponent generate(FacesContext context, String id);
   
   /**
    * Dynamically generates a component for the given property sheet item.
    * The generated component is also setup appropriately for it's model
    * definition and added to the given property sheet.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet component
    * @param item The wrappper component representing the item to generate,
    *        either a property, association or child association
    * @return The component instance
    */
   UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item);

   /**
    * Determines whether the component will be enabled when rendered.
    * 
    * @param context FacesContext
    * @param control The control being rendered
    * @param propDef The definition of the property
    * @return true if the component should be enabled
    */
   boolean isEnabledInEditMode (FacesContext context, UIComponent control, PropertyDefinition propDef);
}
