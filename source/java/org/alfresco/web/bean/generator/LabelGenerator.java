package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component to represent a label.
 * 
 * @author gavinc
 */
public class LabelGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {        
      return createOutputTextComponent(context, id);
   }

   @Override
   @SuppressWarnings("unchecked")
   public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = generate(context, "label_" + item.getName());
      
      // add the component to the property sheet item
      item.getChildren().add(component);
      
      // TODO: setup the 'for' attribute to associate with it the control
      
      return component;
   }
}
