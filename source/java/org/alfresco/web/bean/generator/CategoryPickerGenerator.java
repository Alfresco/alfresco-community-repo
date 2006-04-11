package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UICategorySelector;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class CategoryPickerGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }

   public UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      // create the standard component
      UIComponent component = generate(context, item.getName());
      
      // get the property definition
      PropertyDefinition propertyDef = getPropertyDefinition(context,
               propertySheet.getNode(), item.getName());
      
      if (propertySheet.inEditMode() && propertyDef != null && propertyDef.isMultiValued())
      {
         // if the item is multi valued we need to wrap the standard component
         // but only when the property sheet is in edit mode
         component = enableForMultiValue(context, propertySheet, item, component, false);
      }
      else if (propertySheet.inEditMode() == false || item.isReadOnly() || 
              (propertyDef != null && propertyDef.isProtected())) 
      {
         // disable the component if it is read only or protected
         // or if the property sheet is in view mode
         component.getAttributes().put("disabled", Boolean.TRUE);
      }
      
      // setup the converter if one was specified
      setupConverter(context, propertySheet, item, component);
      
      return component;
   }
}
