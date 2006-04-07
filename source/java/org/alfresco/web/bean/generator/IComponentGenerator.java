package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Interface definition for objects that dynamically generate components 
 * on behalf of the PropertySheet component.
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
    * Dynamically generates a component for use in the given property sheet
    * to represent the given property definition.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet component
    * @param item The wrappper component representing the item to generate,
    *        either a property, association or child association
    * @return The component instance
    */
   UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item);
}
