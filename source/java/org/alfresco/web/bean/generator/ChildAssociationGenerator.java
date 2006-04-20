package org.alfresco.web.bean.generator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet.ClientValidation;

/**
 * Generates a component to manage child associations.
 * 
 * @author gavinc
 */
public class ChildAssociationGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOC_EDITOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }
   
   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking)
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
}
