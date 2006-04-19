package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class TextFieldGenerator extends BaseComponentGenerator
{
   @SuppressWarnings("unchecked")
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

   @Override
   @SuppressWarnings("unchecked")
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking)
   {
      if (component instanceof UIMultiValueEditor)
      {
         // if the text field has multiple values don't allow real time
         // checking of the mandatory status
         
         // TODO: the multi-value editor component needs to use the 
         //       'current_value' hidden field rather than the standard
         //       'value' field as this is always null (it's used internally 
         //       by the component) for now disable mandatory checks completely
         
         //super.setupMandatoryValidation(context, propertySheet, item, component, false);
      }
      else
      {
         // setup the client validation rule with real time validation enabled
         super.setupMandatoryValidation(context, propertySheet, item, component, true);
      
         // add event handler to kick off real time checks
         component.getAttributes().put("onkeyup", "javascript:processButtonState();");
      }
   }
}
