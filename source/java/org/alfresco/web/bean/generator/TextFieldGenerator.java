package org.alfresco.web.bean.generator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet.ClientValidation;

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
         // Override the setup of the mandatory validation as the
         // [form][element] approach needs to be used to locate the 
         // field and the <comp-id>_current_value hidden field needs 
         // to be used. We also enable real time so the page load
         // check disables the ok button if necessary, as the user
         // adds or removes items from the multi value list the 
         // page will be refreshed and therefore re-check the status.
         
         List<String> params = new ArrayList<String>(3);
      
         // add the value parameter
         String value = "document.forms['" +
               Utils.getParentForm(context, component).getId() + "']['" + 
               component.getClientId(context) + "_current_value'].value";
         params.add(value);
         
         // add the validation failed message to show (use the value of the 
         // label component of the given item)
         String msg = Application.getMessage(context, "validation_mandatory");
         params.add("'" + MessageFormat.format(msg, new Object[] {item.getResolvedDisplayLabel()}) + "'");
         
         // add the validation case to the property sheet
         propertySheet.addClientValidation(new ClientValidation("validateMandatory",
               params, true));
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
