package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.component.UICharsetSelector;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.alfresco.web.ui.repo.converter.CharsetConverter;

/**
 * Generates a Charset selector component.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class CharsetSelectorGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().createComponent(UICharsetSelector.COMPONENT_TYPE);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }

   @Override
   protected void setupConverter(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      if (propertySheet.inEditMode() == false)
      {
         if (property.getConverter() != null)
         {
            // create and add the custom converter
            createAndSetConverter(context, property.getConverter(), component);
         }
         else
         {
            // if there isn't a custom converter add the mime type converter as a default
            createAndSetConverter(context, CharsetConverter.CONVERTER_ID, component);
         }
      }
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      // a mime type selector will always have one value or another 
      // so there is no need to create a mandatory validation rule.
   }
}
