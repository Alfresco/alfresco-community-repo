package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a checkbox component.
 * 
 * @author gavinc
 */
public class CheckboxGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = (UISelectBoolean)context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_SELECT_BOOLEAN);
      component.setRendererType(ComponentConstants.JAVAX_FACES_CHECKBOX);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }

   @Override
   protected void setupConverter(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      if (property.getConverter() != null)
      {
         // create and add the custom converter
         createAndSetConverter(context, property.getConverter(), component);
      }
      else
      {
         if (propertySheet.inEditMode() == false)
         {
            if (propertyDef != null && propertyDef.isMultiValued())
            {
               // if there isn't a custom converter and the property is
               // multi-valued add the multi value converter as a default
               createAndSetConverter(context, 
                     RepoConstants.ALFRESCO_FACES_MULTIVALUE_CONVERTER,
                     component);
            }
            else
            {
               // if there isn't a custom converter and the property is
               // not multi-valued add the boolean converter as a default
               createAndSetConverter(context, 
                     RepoConstants.ALFRESCO_FACES_BOOLEAN_CONVERTER,
                     component);
            }
         }
      }
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking)
   {
      // a checkbox will always have one value or another so there
      // is no need to create a mandatory validation rule
   }
}
