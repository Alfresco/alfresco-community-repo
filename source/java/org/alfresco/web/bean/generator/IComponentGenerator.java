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
