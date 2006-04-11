package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class TextFieldGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
      FacesHelper.setupComponentId(context, component, id);
      component.getAttributes().put("size", "35");
      component.getAttributes().put("maxlength", "1024");
      
      return component;
   }

   public UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = null;
      
      // get the property definition
      PropertyDefinition propertyDef = getPropertyDefinition(context,
            propertySheet.getNode(), item.getName());
         
      if (propertySheet.inEditMode())
      {
         // use the standard component in edit mode
         component = generate(context, item.getName());
         
         // disable the component if it is read only or protected
         if (item.isReadOnly() || (propertyDef != null && propertyDef.isProtected()))
         {
            component.getAttributes().put("disabled", Boolean.TRUE);
         }
         else
         {
            // if the item is multi valued we need to wrap the standard component
            if (propertyDef != null && propertyDef.isMultiValued())
            {
               component = enableForMultiValue(context, propertySheet, item, component, true);
            }
         }
      }
      else
      {
         // create an output text component in view mode
         component = createOutputTextComponent(context, item.getName());
         
         // if the property is multi-valued and there isn't a custom converter 
         // specified, add the MultiValue converter as a default
         if (propertyDef != null && propertyDef.isMultiValued() && item.getConverter() == null)
         {
            item.setConverter(RepoConstants.ALFRESCO_FACES_MULTIVALUE_CONVERTER);
         }
      }
      
      // setup the converter if one was specified
      setupConverter(context, propertySheet, item, component);
      
      return component;
   }
}
