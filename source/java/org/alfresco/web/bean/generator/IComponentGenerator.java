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
}
